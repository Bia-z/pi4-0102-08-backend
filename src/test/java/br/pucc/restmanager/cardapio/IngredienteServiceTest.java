package br.pucc.restmanager.cardapio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.pucc.restmanager.cardapio.domain.Ingrediente;
import br.pucc.restmanager.cardapio.dto.IngredienteResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class IngredienteServiceTest {

    @Mock
    IngredienteRepository ingredientes;

    @InjectMocks
    IngredienteService service;

    @Test
    void listaSomenteIngredientesAtivosOrdenadosPorNome() {
        Ingrediente alho = new Ingrediente("Alho", true);
        Ingrediente tomate = new Ingrediente("Tomate", true);
        ReflectionTestUtils.setField(alho, "id", 1L);
        ReflectionTestUtils.setField(tomate, "id", 2L);
        when(ingredientes.findByAtivoTrueOrderByNomeAsc()).thenReturn(List.of(alho, tomate));

        List<IngredienteResponse> response = service.listarDisponiveis();

        assertEquals(List.of("Alho", "Tomate"),
                response.stream().map(IngredienteResponse::nome).toList());
        assertEquals(List.of(1L, 2L),
                response.stream().map(IngredienteResponse::id).toList());
        verify(ingredientes).findByAtivoTrueOrderByNomeAsc();
    }
}
