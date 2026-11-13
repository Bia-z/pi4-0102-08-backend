package br.pucc.restmanager.mesa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import br.pucc.restmanager.mesa.MesaRepository.MesaMapaProjection;
import br.pucc.restmanager.mesa.domain.Mesa;
import br.pucc.restmanager.mesa.domain.StatusMesa;
import br.pucc.restmanager.mesa.dto.CadastrarMesaRequest;
import br.pucc.restmanager.mesa.dto.MapaMesasResponse;
import br.pucc.restmanager.mesa.dto.MesaResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MesaServiceTest {

    @Mock
    MesaRepository mesas;

    private MesaService service;

    @BeforeEach
    void montar() {
        service = new MesaService(mesas);
    }

    @Test
    void cadastraMesaComStatusDisponivel() {
        when(mesas.findByNumero(11)).thenReturn(Optional.empty());
        when(mesas.save(any(Mesa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MesaResponse response = service.cadastrar(new CadastrarMesaRequest(11, 4));

        assertEquals(11, response.numero());
        assertEquals(4, response.capacidade());
        assertEquals("disponivel", response.status());
        verify(mesas).save(any(Mesa.class));
    }

    @Test
    void recusaNumeroDuplicadoComErroDaFicha() {
        when(mesas.findByNumero(11)).thenReturn(Optional.of(new Mesa(11, 4)));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.cadastrar(new CadastrarMesaRequest(11, 4)));

        assertEquals(ErrorCode.MESA_DUPLICADA, exception.getErro());
        verify(mesas, never()).save(any(Mesa.class));
    }

    @Test
    void recusaCapacidadeMenorQueUm() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.cadastrar(new CadastrarMesaRequest(11, 0)));

        assertEquals(ErrorCode.CAPACIDADE_INVALIDA, exception.getErro());
        verify(mesas, never()).save(any(Mesa.class));
    }

    @Test
    void mesaNovaIniciaDisponivel() {
        assertEquals(StatusMesa.DISPONIVEL, new Mesa(11, 4).getStatus());
    }

    @Test
    void retornaMapaComStatusEValorConsumidoDasMesas() {
        MesaMapaProjection mesa = mock(MesaMapaProjection.class);
        when(mesa.getMesaId()).thenReturn(5L);
        when(mesa.getNumero()).thenReturn(10);
        when(mesa.getStatus()).thenReturn("EM_PAGAMENTO");
        when(mesa.getValorConsumido()).thenReturn(new BigDecimal("143.00"));
        when(mesas.obterMapaMesas()).thenReturn(List.of(mesa));

        MapaMesasResponse response = service.obterMapaMesas();

        assertEquals(1, response.mesas().size());
        assertEquals(5L, response.mesas().get(0).mesaId());
        assertEquals(10, response.mesas().get(0).numero());
        assertEquals("em_pagamento", response.mesas().get(0).status());
        assertEquals(new BigDecimal("143.00"), response.mesas().get(0).valorConsumido());
    }

    @Test
    void retornaListaVaziaQuandoNaoHaMesasAtivas() {
        when(mesas.obterMapaMesas()).thenReturn(List.of());

        assertEquals(List.of(), service.obterMapaMesas().mesas());
    }

    @Test
    void mesaCadastradaFicaDisponivelNoMapa() {
        when(mesas.findByNumero(11)).thenReturn(Optional.empty());
        when(mesas.save(any(Mesa.class))).thenAnswer(invocation -> invocation.getArgument(0));
        MesaMapaProjection mesaNoMapa = mock(MesaMapaProjection.class);
        when(mesaNoMapa.getMesaId()).thenReturn(1L);
        when(mesaNoMapa.getNumero()).thenReturn(11);
        when(mesaNoMapa.getStatus()).thenReturn("DISPONIVEL");
        when(mesaNoMapa.getValorConsumido()).thenReturn(BigDecimal.ZERO);
        when(mesas.obterMapaMesas()).thenReturn(List.of(mesaNoMapa));

        MesaResponse cadastrada = service.cadastrar(new CadastrarMesaRequest(11, 4));
        MapaMesasResponse mapa = service.obterMapaMesas();

        assertEquals("disponivel", cadastrada.status());
        assertEquals(1, mapa.mesas().size());
        assertEquals(11, mapa.mesas().get(0).numero());
        assertEquals("disponivel", mapa.mesas().get(0).status());
        assertEquals(BigDecimal.ZERO, mapa.mesas().get(0).valorConsumido());
    }

    // ---- Gestão de mesas pelo administrador ----

    private Mesa mesa(long id, int numero, StatusMesa status) {
        Mesa mesa = new Mesa(numero, 4);
        ReflectionTestUtils.setField(mesa, "id", id);
        ReflectionTestUtils.setField(mesa, "status", status);
        return mesa;
    }

    @Test
    void listaSoMesasAtivasEmOrdemDeNumero() {
        when(mesas.findByAtivoTrueOrderByNumeroAsc()).thenReturn(List.of(
                mesa(1L, 1, StatusMesa.DISPONIVEL), mesa(2L, 2, StatusMesa.OCUPADA)));

        List<MesaResponse> lista = service.listar();

        assertEquals(List.of(1, 2), lista.stream().map(MesaResponse::numero).toList());
        assertEquals("ocupada", lista.get(1).status());
    }

    @Test
    void cadastrarNumeroDeMesaExcluidaReativaAMesa() {
        Mesa excluida = mesa(9L, 11, StatusMesa.DISPONIVEL);
        excluida.desativar();
        when(mesas.findByNumero(11)).thenReturn(Optional.of(excluida));
        when(mesas.save(excluida)).thenReturn(excluida);

        MesaResponse resposta = service.cadastrar(new CadastrarMesaRequest(11, 6));

        assertEquals(9L, resposta.id());
        assertEquals(6, resposta.capacidade());
        assertTrue(excluida.isAtivo());
    }

    @Test
    void editaNumeroECapacidade() {
        Mesa atual = mesa(1L, 5, StatusMesa.OCUPADA);
        when(mesas.buscarAtivaPorIdComBloqueio(1L)).thenReturn(Optional.of(atual));
        when(mesas.findByNumero(15)).thenReturn(Optional.empty());
        when(mesas.save(atual)).thenReturn(atual);

        MesaResponse resposta = service.atualizar(1L, new CadastrarMesaRequest(15, 8));

        assertEquals(15, resposta.numero());
        assertEquals(8, resposta.capacidade());
        assertEquals("ocupada", resposta.status(), "editar não muda o status");
    }

    @Test
    void editarMantendoOMesmoNumeroNaoContaComoDuplicado() {
        Mesa atual = mesa(1L, 5, StatusMesa.DISPONIVEL);
        when(mesas.buscarAtivaPorIdComBloqueio(1L)).thenReturn(Optional.of(atual));
        when(mesas.findByNumero(5)).thenReturn(Optional.of(atual));
        when(mesas.save(atual)).thenReturn(atual);

        assertEquals(6, service.atualizar(1L, new CadastrarMesaRequest(5, 6)).capacidade());
    }

    @Test
    void recusaEditarParaNumeroDeOutraMesa() {
        when(mesas.buscarAtivaPorIdComBloqueio(1L)).thenReturn(Optional.of(mesa(1L, 5, StatusMesa.DISPONIVEL)));
        when(mesas.findByNumero(7)).thenReturn(Optional.of(mesa(2L, 7, StatusMesa.DISPONIVEL)));

        BusinessException erro = assertThrows(BusinessException.class,
                () -> service.atualizar(1L, new CadastrarMesaRequest(7, 4)));

        assertEquals(ErrorCode.MESA_DUPLICADA, erro.getErro());
        verify(mesas, never()).save(any(Mesa.class));
    }

    @Test
    void recusaEditarMesaInexistente() {
        when(mesas.buscarAtivaPorIdComBloqueio(99L)).thenReturn(Optional.empty());

        BusinessException erro = assertThrows(BusinessException.class,
                () -> service.atualizar(99L, new CadastrarMesaRequest(7, 4)));

        assertEquals(ErrorCode.MESA_NAO_ENCONTRADA, erro.getErro());
    }

    @Test
    void excluiMesaLivreDesativandoSemApagar() {
        Mesa livre = mesa(1L, 5, StatusMesa.DISPONIVEL);
        when(mesas.buscarAtivaPorIdComBloqueio(1L)).thenReturn(Optional.of(livre));

        service.excluir(1L);

        assertFalse(livre.isAtivo());
        verify(mesas).save(livre);
        verify(mesas, never()).delete(any(Mesa.class));
    }

    @Test
    void recusaExcluirMesaOcupadaOuEmPagamento() {
        for (StatusMesa status : List.of(StatusMesa.OCUPADA, StatusMesa.EM_PAGAMENTO)) {
            Mesa emUso = mesa(1L, 5, status);
            when(mesas.buscarAtivaPorIdComBloqueio(1L)).thenReturn(Optional.of(emUso));

            BusinessException erro = assertThrows(BusinessException.class, () -> service.excluir(1L));

            assertEquals(ErrorCode.MESA_EM_USO, erro.getErro());
            assertTrue(emUso.isAtivo());
        }
        verify(mesas, never()).save(any(Mesa.class));
    }
}