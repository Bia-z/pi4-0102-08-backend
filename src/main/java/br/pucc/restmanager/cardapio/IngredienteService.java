package br.pucc.restmanager.cardapio;

import br.pucc.restmanager.cardapio.dto.IngredienteResponse;
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
}