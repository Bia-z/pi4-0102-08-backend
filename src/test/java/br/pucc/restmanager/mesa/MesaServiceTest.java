package br.pucc.restmanager.mesa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        when(mesas.existsByNumero(11)).thenReturn(false);
        when(mesas.save(any(Mesa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MesaResponse response = service.cadastrar(new CadastrarMesaRequest(11, 4));

        assertEquals(11, response.numero());
        assertEquals(4, response.capacidade());
        assertEquals("disponivel", response.status());
        verify(mesas).save(any(Mesa.class));
    }

    @Test
    void recusaNumeroDuplicadoComErroDaFicha() {
        when(mesas.existsByNumero(11)).thenReturn(true);

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
}