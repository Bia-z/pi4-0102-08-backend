package br.pucc.restmanager.cardapio;

import br.pucc.restmanager.cardapio.dto.AtualizarIngredienteRequest;
import br.pucc.restmanager.cardapio.dto.CadastrarIngredienteRequest;
import br.pucc.restmanager.cardapio.dto.IngredienteResponse;
import br.pucc.restmanager.cardapio.domain.Ingrediente;
import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IngredienteService {

    private final IngredienteRepository ingredientes;

    public IngredienteService(IngredienteRepository ingredientes) {
        this.ingredientes = ingredientes;
    }

    @Transactional(readOnly = true)
    public List<IngredienteResponse> listarDisponiveis() {
        return ingredientes.findByAtivoTrueOrderByNomeAsc().stream()
                .map(IngredienteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IngredienteResponse> listarTodos() {
        return ingredientes.findAllByOrderByNomeAsc().stream()
                .map(IngredienteResponse::from)
                .toList();
    }

    @Transactional
    public IngredienteResponse cadastrar(CadastrarIngredienteRequest request) {
        String nome = request.nome().trim();
        if (ingredientes.existsByNomeIgnoreCase(nome)) {
            throw new BusinessException(ErrorCode.INGREDIENTE_DUPLICADO);
        }
        return IngredienteResponse.from(ingredientes.save(new Ingrediente(nome, true)));
    }

    @Transactional
    public IngredienteResponse atualizar(Long id, AtualizarIngredienteRequest request) {
        Ingrediente ingrediente = ingredientes.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.INGREDIENTE_NAO_ENCONTRADO));
        String nome = request.nome().trim();
        if (ingredientes.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new BusinessException(ErrorCode.INGREDIENTE_DUPLICADO);
        }
        ingrediente.atualizar(nome, request.ativo());
        return IngredienteResponse.from(ingrediente);
    }

    @Transactional
    public void desativar(Long id) {
        Ingrediente ingrediente = ingredientes.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.INGREDIENTE_NAO_ENCONTRADO));
        ingrediente.desativar();
    }
}