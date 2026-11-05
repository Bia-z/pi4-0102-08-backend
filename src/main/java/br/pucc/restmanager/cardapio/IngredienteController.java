package br.pucc.restmanager.cardapio;

import br.pucc.restmanager.cardapio.dto.IngredienteResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ingredientes")
public class IngredienteController {

    private final IngredienteService ingredientes;

    public IngredienteController(IngredienteService ingredientes) {
        this.ingredientes = ingredientes;
    }

    @GetMapping
    public List<IngredienteResponse> listarDisponiveis() {
        return ingredientes.listarDisponiveis();
    }
}