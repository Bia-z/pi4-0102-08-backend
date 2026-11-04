package br.pucc.restmanager.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.pucc.restmanager.config.SecurityConfig;
import br.pucc.restmanager.usuario.domain.Perfil;
import br.pucc.restmanager.usuario.domain.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = SecurityAuthorizationTest.TestEndpoint.class)
@Import({SecurityConfig.class, JwtService.class})
@TestPropertySource(properties = {
        "app.jwt.secret=segredo-de-teste-com-mais-de-32-caracteres-ok",
        "app.jwt.expiracao-minutos=60"
})
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

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

    private String bearer(Perfil perfil) {
        Usuario usuario = new Usuario("T001", "Teste", "teste@restmanager.com",
                "hash", perfil, true);
        return "Bearer " + jwtService.gerarToken(usuario);
    }

    @RestController
    static class TestEndpoint {
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
