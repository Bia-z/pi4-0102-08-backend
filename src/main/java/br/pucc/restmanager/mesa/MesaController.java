package br.pucc.restmanager.mesa;

import br.pucc.restmanager.mesa.dto.CadastrarMesaRequest;
import br.pucc.restmanager.mesa.dto.MapaMesasResponse;
import br.pucc.restmanager.mesa.dto.MesaResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mesas")
public class MesaController {

    private final MesaService mesas;

    public MesaController(MesaService mesas) {
        this.mesas = mesas;
    }

    @GetMapping("/mapa")
    public MapaMesasResponse obterMapaMesas() {
        return mesas.obterMapaMesas();
    }

    @GetMapping
    public List<MesaResponse> listar() {
        return mesas.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MesaResponse cadastrar(@Valid @RequestBody CadastrarMesaRequest request) {
        return mesas.cadastrar(request);
    }

    @PutMapping("/{id}")
    public MesaResponse atualizar(@PathVariable Long id, @Valid @RequestBody CadastrarMesaRequest request) {
        return mesas.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        mesas.excluir(id);
    }
}
