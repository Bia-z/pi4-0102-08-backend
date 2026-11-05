package br.pucc.restmanager.cardapio;

import br.pucc.restmanager.cardapio.dto.CadastrarProdutoRequest;
import br.pucc.restmanager.cardapio.dto.CardapioCategoriaResponse;
import br.pucc.restmanager.cardapio.dto.ProdutoResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

    @GetMapping("/api/cardapio")
    public List<CardapioCategoriaResponse> listarCardapio() {
        return produtos.listarCardapio();
    }
}