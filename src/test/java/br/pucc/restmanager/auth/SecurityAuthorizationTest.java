package br.pucc.restmanager.auth;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.pucc.restmanager.config.SecurityConfig;
import br.pucc.restmanager.usuario.UsuarioRepository;
import br.pucc.restmanager.usuario.domain.Perfil;
import br.pucc.restmanager.usuario.domain.Usuario;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = SecurityAuthorizationTest.TestEndpoint.class)
@Import({SecurityConfig.class, JwtService.class, SecurityAuthorizationTest.TestEndpoint.class})
@TestPropertySource(properties = {
        "app.jwt.secret=segredo-de-teste-com-mais-de-32-caracteres-ok",
        "app.jwt.expiracao-minutos=60"
})
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private UsuarioRepository usuarios;

    @Test
    void recusaOperacaoSemTokenComErroDaFicha() throws Exception {
        mockMvc.perform(post("/api/pedidos"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"erro\":\"TOKEN_AUSENTE\"}"));
    }

    @Test
    void recusaPerfilNaoPermitidoComErroDaFicha() throws Exception {
        mockMvc.perform(post("/api/pedidos")
                        .header("Authorization", bearer(Perfil.CAIXA)))
                .andExpect(status().isForbidden())
                .andExpect(content().json("{\"erro\":\"ACESSO_NEGADO\"}"));
    }

    @Test
    void recusaTokenInvalido() throws Exception {
        mockMvc.perform(post("/api/pedidos")
                        .header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"erro\":\"TOKEN_INVALIDO\"}"));
    }

    @Test
    void permitePerfilDeclaradoParaOperacao() throws Exception {
        mockMvc.perform(post("/api/pedidos")
                        .header("Authorization", bearer(Perfil.GARCOM)))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    void somenteAdministradorPodeCadastrarUsuario() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .header("Authorization", bearer(Perfil.GARCOM)))
                .andExpect(status().isForbidden())
                .andExpect(content().json("{\"erro\":\"ACESSO_NEGADO\"}"));

        mockMvc.perform(post("/api/usuarios")
                        .header("Authorization", bearer(Perfil.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(content().string("cadastrado"));
    }

    @Test
    void recusaTokenDeUsuarioDesativadoDepoisDeLogar() throws Exception {
        String token = bearer(Perfil.GARCOM);
        when(usuarios.findByCodigo("T001")).thenReturn(Optional.of(
                new Usuario("T001", "Teste", "teste@restmanager.com", "hash", Perfil.GARCOM, false)));

        mockMvc.perform(post("/api/pedidos").header("Authorization", token))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"erro\":\"TOKEN_INVALIDO\"}"));
    }

    @Test
    void recusaTokenDeUsuarioQueTeveOPerfilAlterado() throws Exception {
        String token = bearer(Perfil.GARCOM);
        when(usuarios.findByCodigo("T001")).thenReturn(Optional.of(
                new Usuario("T001", "Teste", "teste@restmanager.com", "hash", Perfil.CAIXA, true)));

        mockMvc.perform(post("/api/pedidos").header("Authorization", token))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"erro\":\"TOKEN_INVALIDO\"}"));
    }

    @Test
    void recusaTokenDeUsuarioQueNaoExisteMais() throws Exception {
        String token = bearer(Perfil.GARCOM);
        when(usuarios.findByCodigo("T001")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/pedidos").header("Authorization", token))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"erro\":\"TOKEN_INVALIDO\"}"));
    }

    private String bearer(Perfil perfil) {
        Usuario usuario = new Usuario("T001", "Teste", "teste@restmanager.com",
                "hash", perfil, true);
        when(usuarios.findByCodigo("T001")).thenReturn(Optional.of(usuario));
        return "Bearer " + jwtService.gerarToken(usuario);
    }

    @RestController
    public static class TestEndpoint {
        @PostMapping("/api/pedidos")
        String criarPedido() {
            return "ok";
        }

        @PostMapping("/api/usuarios")
        String cadastrarUsuario() {
            return "cadastrado";
        }
    }
}