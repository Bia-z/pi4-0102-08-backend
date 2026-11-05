package br.pucc.restmanager.cardapio;

import br.pucc.restmanager.cardapio.domain.Ingrediente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredienteRepository extends JpaRepository<Ingrediente, Long> {
}