package br.pucc.restmanager.cardapio;

import br.pucc.restmanager.cardapio.domain.Ingrediente;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredienteRepository extends JpaRepository<Ingrediente, Long> {

    List<Ingrediente> findByAtivoTrueOrderByNomeAsc();

    List<Ingrediente> findByIdInAndAtivoTrue(Collection<Long> ids);
}