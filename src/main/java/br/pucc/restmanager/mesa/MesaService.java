package br.pucc.restmanager.mesa;

import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import br.pucc.restmanager.mesa.domain.Mesa;
import br.pucc.restmanager.mesa.dto.CadastrarMesaRequest;
import br.pucc.restmanager.mesa.dto.MapaMesasResponse;
import br.pucc.restmanager.mesa.dto.MesaResponse;
import br.pucc.restmanager.mesa.dto.MesaResumoResponse;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MesaService {

    private final MesaRepository mesas;

    public MesaService(MesaRepository mesas) {
        this.mesas = mesas;
    }

    @Transactional(readOnly = true)
    public MapaMesasResponse obterMapaMesas() {
        List<MesaResumoResponse> resumo = mesas.obterMapaMesas().stream()
                .map(mesa -> new MesaResumoResponse(
                        mesa.getMesaId(),
                        mesa.getNumero(),
                        mesa.getStatus().toLowerCase(Locale.ROOT),
                        mesa.getValorConsumido()))
                .toList();
        return new MapaMesasResponse(resumo);
    }

    @Transactional
    public MesaResponse cadastrar(CadastrarMesaRequest request) {
        if (request.capacidade() == null || request.capacidade() < 1) {
            throw new BusinessException(ErrorCode.CAPACIDADE_INVALIDA);
        }
        if (mesas.existsByNumero(request.numero())) {
            throw new BusinessException(ErrorCode.MESA_DUPLICADA);
        }
        return MesaResponse.from(mesas.save(new Mesa(request.numero(), request.capacidade())));
    }
}