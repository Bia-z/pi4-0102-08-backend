package br.pucc.restmanager.auth;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.pucc.restmanager.config.SecurityConfig;
import br.pucc.restmanager.usuario.CodigoFuncionarioGenerator;
import br.pucc.restmanager.usuario.UsuarioController;
import br.pucc.restmanager.usuario.UsuarioRepository;
import br.pucc.restmanager.usuario.UsuarioService;
import br.pucc.restmanager.usuario.domain.Perfil;
import br.pucc.restmanager.usuario.domain.Usuario;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = {
        AuthController.class,
        UsuarioController.class,
        GarcomAccessIntegrationTest.TestEndpoint.class
})
@Import({
        SecurityConfig.class,
        JwtService.class,
        AuthService.class,
        UsuarioService.class,
        CodigoFuncionarioGenerator.class
})
@TestPropertySource(properties = {
        "app.jwt.secret=segredo-de-teste-com-mais-de-32-caracteres-ok",
        "app.jwt.expiracao-minutos=60"
})
class GarcomAccessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioRepository usuarios;

    @Test
    void administradorCadastraGarcomQueEntraERecebeAcessoApenasDeGarcom() throws Exception {
        AtomicReference<Usuario> usuarioCadastrado = new AtomicReference<>();
        when(usuarios.existsByCpf("52998224725")).thenReturn(false);
        when(usuarios.findByCodigo(anyString())).thenAnswer(invocation -> {
            Usuario usuario = usuarioCadastrado.get();
            String codigo = invocation.getArgument(0);
            return usuario != null && usuario.getCodigo().equals(codigo)
                    ? Optional.of(usuario)
                    : Optional.empty();
        });
        when(usuarios.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuarioCadastrado.set(usuario);
            return usuario;
        });

        String tokenAdmin = jwtService.gerarToken(
                new Usuario("ADMIN001", "Administrador", null, "hash", Perfil.ADMIN, true));
        String cadastro = """
                {
                  "nome": "Carlos Mendes",
                  "cpf": "529.982.247-25",
                  "telefone": "11999999999",
                  "perfil": "garcom",
                  "senha": "senhaSegura123"
                }
                """;

        String respostaCadastro = mockMvc.perform(post("/api/usuarios")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cadastro))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode usuarioJson = objectMapper.readTree(respostaCadastro);
        assertNotNull(usuarioJson.get("codigo"));
        String codigoFuncionario = usuarioJson.get("codigo").asText();

        String login = objectMapper.writeValueAsString(
                new LoginRequest(codigoFuncionario, "senhaSegura123"));
        String respostaLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"perfil\":\"garcom\"}", false))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String tokenGarcom = objectMapper.readTree(respostaLogin).get("token").asText();

        mockMvc.perform(post("/api/pedidos")
                        .header("Authorization", "Bearer " + tokenGarcom))
                .andExpect(status().isOk())
                .andExpect(content().string("pedido criado"));

        mockMvc.perform(post("/api/mesas/42/pagamento/iniciar")
                        .header("Authorization", "Bearer " + tokenGarcom))
                .andExpect(status().isForbidden())
                .andExpect(content().json("{\"erro\":\"ACESSO_NEGADO\"}"));

        mockMvc.perform(patch("/api/pedidos/42/status")
                        .header("Authorization", "Bearer " + tokenGarcom))
                .andExpect(status().isForbidden())
                .andExpect(content().json("{\"erro\":\"ACESSO_NEGADO\"}"));
        assertNotNull(usuarioCadastrado.get());
    }

    @RestController
    static class TestEndpoint {
        @PostMapping("/api/pedidos")
        String criarPedido() {
            return "pedido criado";
        }

        @PostMapping("/api/mesas/{mesaId}/pagamento/iniciar")
        String iniciarPagamento(@PathVariable String mesaId) {
            return "pagamento iniciado";
        }

        @PatchMapping("/api/pedidos/{pedidoId}/status")
        String atualizarStatus(@PathVariable String pedidoId) {
            return "status atualizado";
        }
    }
}
