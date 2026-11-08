package br.pucc.restmanager.cardapio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.pucc.restmanager.cardapio.domain.Ingrediente;
import br.pucc.restmanager.cardapio.dto.IngredienteResponse;
import br.pucc.restmanager.cardapio.dto.CadastrarIngredienteRequest;
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

    @Test
    void cadastraIngredienteAtivo() {
        when(ingredientes.existsByNomeIgnoreCase("Alho")).thenReturn(false);
        when(ingredientes.save(any(Ingrediente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        IngredienteResponse response = service.cadastrar(new CadastrarIngredienteRequest(" Alho "));

        assertEquals("Alho", response.nome());
        assertTrue(response.ativo());
    }

    @Test
    void remocaoDesativaIngredienteSemApagarRegistro() {
        Ingrediente ingrediente = new Ingrediente("Alho", true);
        ReflectionTestUtils.setField(ingrediente, "id", 1L);
        when(ingredientes.findById(1L)).thenReturn(java.util.Optional.of(ingrediente));

        service.desativar(1L);

        assertFalse(ingrediente.isAtivo());
    }
}
