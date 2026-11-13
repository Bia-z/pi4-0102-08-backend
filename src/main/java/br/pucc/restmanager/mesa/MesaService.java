package br.pucc.restmanager.mesa;

import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import br.pucc.restmanager.mesa.domain.Mesa;
import br.pucc.restmanager.mesa.domain.StatusMesa;
import br.pucc.restmanager.mesa.dto.CadastrarMesaRequest;
import br.pucc.restmanager.mesa.dto.MapaMesasResponse;
import br.pucc.restmanager.mesa.dto.MesaResponse;
import br.pucc.restmanager.mesa.dto.MesaResumoResponse;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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
        validarCapacidade(request.capacidade());
        Optional<Mesa> existente = mesas.findByNumero(request.numero());
        if (existente.isPresent()) {
            Mesa mesa = existente.get();
            if (mesa.isAtivo()) {
                throw new BusinessException(ErrorCode.MESA_DUPLICADA);
            }
            // Número de uma mesa excluída: reativa em vez de recusar
            mesa.reativar(request.capacidade());
            return MesaResponse.from(mesas.save(mesa));
        }
        return MesaResponse.from(mesas.save(new Mesa(request.numero(), request.capacidade())));
    }

    @Transactional(readOnly = true)
    public List<MesaResponse> listar() {
        return mesas.findByAtivoTrueOrderByNumeroAsc().stream()
                .map(MesaResponse::from)
                .toList();
    }

    @Transactional
    public MesaResponse atualizar(Long id, CadastrarMesaRequest request) {
        validarCapacidade(request.capacidade());
        Mesa mesa = buscarAtiva(id);
        mesas.findByNumero(request.numero())
                .filter(outra -> !outra.getId().equals(id))
                .ifPresent(outra -> {
                    throw new BusinessException(ErrorCode.MESA_DUPLICADA);
                });
        mesa.atualizar(request.numero(), request.capacidade());
        return MesaResponse.from(mesas.save(mesa));
    }

    @Transactional
    public void excluir(Long id) {
        Mesa mesa = buscarAtiva(id);
        if (mesa.getStatus() != StatusMesa.DISPONIVEL) {
            throw new BusinessException(ErrorCode.MESA_EM_USO);
        }
        mesa.desativar();
        mesas.save(mesa);
    }

    private Mesa buscarAtiva(Long id) {
        return mesas.buscarAtivaPorIdComBloqueio(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MESA_NAO_ENCONTRADA));
    }

    private void validarCapacidade(Integer capacidade) {
        if (capacidade == null || capacidade < 1) {
            throw new BusinessException(ErrorCode.CAPACIDADE_INVALIDA);
        }
    }
}