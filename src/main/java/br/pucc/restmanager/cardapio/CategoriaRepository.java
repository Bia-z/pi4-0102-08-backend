package br.pucc.restmanager.cardapio;

import br.pucc.restmanager.cardapio.domain.Categoria;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findByAtivoTrueOrderByOrdemAscNomeAsc();
}