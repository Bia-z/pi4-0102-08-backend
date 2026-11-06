package br.pucc.restmanager.comprovante;

import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class ComprovanteService {

    private final ComprovanteRepository comprovantes;
    private final ComprovantePdfGenerator pdf;

    public ComprovanteService(ComprovanteRepository comprovantes, ComprovantePdfGenerator pdf) {
        this.comprovantes = comprovantes;
        this.pdf = pdf;
    }

    public byte[] gerarComprovantePDF(Long pedidoId) {
        ComprovanteRepository.PedidoReferencia pedido = comprovantes.buscarPedido(pedidoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PEDIDO_NAO_ENCONTRADO));
        if (!"PAGO".equals(pedido.status())) {
            throw new BusinessException(ErrorCode.CONTA_NAO_PAGA);
        }

        try {
            return pdf.gerar(comprovantes.buscarDados(pedido));
        } catch (PdfGenerationException ex) {
            throw new BusinessException(ErrorCode.ERRO_GERACAO_PDF);
        }
    }
}