package br.pucc.restmanager.cardapio;

import br.pucc.restmanager.cardapio.dto.CadastrarProdutoRequest;
import br.pucc.restmanager.cardapio.dto.CardapioCategoriaResponse;
import br.pucc.restmanager.cardapio.dto.ProdutoResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProdutoController {

    private final ProdutoService produtos;

    public ProdutoController(ProdutoService produtos) {
        this.produtos = produtos;
    }

    @PostMapping("/api/produtos")
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutoResponse cadastrar(@Valid @RequestBody CadastrarProdutoRequest request) {
        return produtos.cadastrar(request);
    }

    @GetMapping("/api/produtos")
    public List<ProdutoResponse> listar() {
        return produtos.listar();
    }

    @PutMapping("/api/produtos/{id}")
    public ProdutoResponse atualizar(
            @PathVariable Long id, @Valid @RequestBody CadastrarProdutoRequest request) {
        return produtos.atualizar(id, request);
    }

    @DeleteMapping("/api/produtos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativar(@PathVariable Long id) {
        produtos.desativar(id);
    }

    @GetMapping("/api/cardapio")
    public List<CardapioCategoriaResponse> listarCardapio() {
        return produtos.listarCardapio();
    }
}