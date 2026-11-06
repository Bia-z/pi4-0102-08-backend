package br.pucc.restmanager.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.pucc.restmanager.usuario.domain.Perfil;
import org.junit.jupiter.api.Test;

class PerfilAccessRulesTest {

    private final PerfilAccessRules rules = new PerfilAccessRules();

    @Test
    void perfilPermitidoAcessaOperacaoCorrespondente() {
        assertTrue(rules.perfilPermitido("POST", "/api/pedidos", Perfil.GARCOM));
        assertTrue(rules.perfilPermitido("GET", "/api/pedidos", Perfil.COZINHA));
        assertTrue(rules.perfilPermitido("GET", "/api/pedidos", Perfil.GARCOM));
        assertTrue(rules.perfilPermitido("POST", "/api/mesas/42/pagamento/iniciar", Perfil.CAIXA));
        assertTrue(rules.perfilPermitido("POST", "/api/mesas/42/pagamento/processar", Perfil.CAIXA));
    }

    @Test
    void perfilNaoPermitidoERecusado() {
        assertFalse(rules.perfilPermitido("POST", "/api/pedidos", Perfil.COZINHA));
        assertFalse(rules.perfilPermitido("POST", "/api/mesas/42/pagamento/iniciar", Perfil.GARCOM));
        assertFalse(rules.perfilPermitido(
                "POST", "/api/mesas/42/pagamento/processar", Perfil.GARCOM));
    }

    @Test
    void metodoEOperacaoPrecisamEstarDeclarados() {
        assertFalse(rules.perfilPermitido("GET", "/api/rota-nao-cadastrada", Perfil.ADMIN));
        assertFalse(rules.perfilPermitido("POST", "/api/rota-nao-cadastrada", Perfil.ADMIN));
    }

    @Test
    void listarPedidosEPermitidoParaAdminGarcomECozinha() {
        assertTrue(rules.perfilPermitido("GET", "/api/pedidos", Perfil.ADMIN));
        assertTrue(rules.perfilPermitido("GET", "/api/pedidos", Perfil.GARCOM));
        assertTrue(rules.perfilPermitido("GET", "/api/pedidos", Perfil.COZINHA));
        assertFalse(rules.perfilPermitido("GET", "/api/pedidos", Perfil.CAIXA));
    }

    @Test
    void cadastrarUsuarioEExclusivoDoAdministrador() {
        assertTrue(rules.perfilPermitido("POST", "/api/usuarios", Perfil.ADMIN));
        assertFalse(rules.perfilPermitido("POST", "/api/usuarios", Perfil.GARCOM));
    }

    @Test
    void cadastrarProdutoEExclusivoDoAdministradorECardapioAcessivelAEquipe() {
        assertTrue(rules.perfilPermitido("POST", "/api/produtos", Perfil.ADMIN));
        assertFalse(rules.perfilPermitido("POST", "/api/produtos", Perfil.GARCOM));
        assertTrue(rules.perfilPermitido("GET", "/api/cardapio", Perfil.GARCOM));
        assertTrue(rules.perfilPermitido("GET", "/api/cardapio", Perfil.CAIXA));
    }

    @Test
    void cadastrarMesaEExclusivoDoAdministrador() {
        assertTrue(rules.perfilPermitido("POST", "/api/mesas", Perfil.ADMIN));
        assertFalse(rules.perfilPermitido("POST", "/api/mesas", Perfil.GARCOM));
    }

    @Test
    void obterMapaMesasEPermitidoParaGarcomEcaixa() {
        assertTrue(rules.perfilPermitido("GET", "/api/mesas/mapa", Perfil.GARCOM));
        assertTrue(rules.perfilPermitido("GET", "/api/mesas/mapa", Perfil.CAIXA));
        assertFalse(rules.perfilPermitido("GET", "/api/mesas/mapa", Perfil.COZINHA));
    }

    @Test
    void listarIngredientesDisponiveisEExclusivoDoAdministrador() {
        assertTrue(rules.perfilPermitido("GET", "/api/ingredientes", Perfil.ADMIN));
        assertFalse(rules.perfilPermitido("GET", "/api/ingredientes", Perfil.GARCOM));
    }
}
