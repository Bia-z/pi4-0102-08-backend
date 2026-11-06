package br.pucc.restmanager.comprovante;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ComprovanteServiceTest {

    @Mock
    private ComprovanteRepository comprovantes;

    @Mock
    private ComprovantePdfGenerator pdf;

    @InjectMocks
    private ComprovanteService service;

    @Test
    void pedidoInexistenteRetornaErroDaFicha() {
        when(comprovantes.buscarPedido(42L)).thenReturn(Optional.empty());

        BusinessException erro = assertThrows(
                BusinessException.class,
                () -> service.gerarComprovantePDF(42L));

        assertEquals(ErrorCode.PEDIDO_NAO_ENCONTRADO, erro.getErro());
        verifyNoInteractions(pdf);
    }

    @Test
    void contaAindaNaoPagaRetornaErroEnaoGeraPdf() {
        when(comprovantes.buscarPedido(42L)).thenReturn(Optional.of(
                new ComprovanteRepository.PedidoReferencia(42L, "AGUARDANDO", 7L, 10)));

        BusinessException erro = assertThrows(
                BusinessException.class,
                () -> service.gerarComprovantePDF(42L));

        assertEquals(ErrorCode.CONTA_NAO_PAGA, erro.getErro());
        verifyNoInteractions(pdf);
    }

    @Test
    void falhaDeRenderizacaoRetornaErroDaFicha() {
        ComprovanteRepository.PedidoReferencia pedido =
                new ComprovanteRepository.PedidoReferencia(42L, "PAGO", 7L, 10);
        when(comprovantes.buscarPedido(42L)).thenReturn(Optional.of(pedido));
        when(comprovantes.buscarDados(pedido)).thenReturn(dados());
        when(pdf.gerar(dados())).thenThrow(new PdfGenerationException(
                new IllegalStateException("render failure")));

        BusinessException erro = assertThrows(
                BusinessException.class,
                () -> service.gerarComprovantePDF(42L));

        assertEquals(ErrorCode.ERRO_GERACAO_PDF, erro.getErro());
    }

    @Test
    void pedidoPagoRetornaPdfGerado() {
        ComprovanteRepository.PedidoReferencia pedido =
                new ComprovanteRepository.PedidoReferencia(42L, "PAGO", 7L, 10);
        byte[] esperado = "%PDF-1.4".getBytes();
        when(comprovantes.buscarPedido(42L)).thenReturn(Optional.of(pedido));
        when(comprovantes.buscarDados(pedido)).thenReturn(dados());
        when(pdf.gerar(dados())).thenReturn(esperado);

        assertArrayEquals(esperado, service.gerarComprovantePDF(42L));
        verify(pdf).gerar(dados());
    }

    private ComprovanteDados dados() {
        return new ComprovanteDados(
                10,
                LocalDateTime.of(2026, 11, 6, 12, 0),
                new BigDecimal("10.00"),
                new BigDecimal("20.00"),
                new BigDecimal("2.00"),
                new BigDecimal("22.00"),
                List.of(new ComprovanteDados.Item(
                        "Produto", 1, new BigDecimal("20.00"))),
                List.of(new ComprovanteDados.Pagamento(
                        "PIX", new BigDecimal("22.00"), new BigDecimal("0.00"))));
    }
}