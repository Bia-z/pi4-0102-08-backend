package br.pucc.restmanager.cardapio;

import br.pucc.restmanager.cardapio.domain.Produto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    List<Produto> findByCategoriaIdAndAtivoTrueOrderByNomeAsc(Long categoriaId);
}