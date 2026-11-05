package br.pucc.restmanager.mesa;

import br.pucc.restmanager.mesa.domain.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MesaRepository extends JpaRepository<Mesa, Long> {

    boolean existsByNumero(int numero);
}