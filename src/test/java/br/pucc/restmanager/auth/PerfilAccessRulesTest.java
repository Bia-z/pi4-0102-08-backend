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
        assertTrue(rules.perfilPermitido(
                "GET", "/api/pedidos/42/comprovante-pdf", Perfil.GARCOM));
        assertTrue(rules.perfilPermitido(
                "GET", "/api/pedidos/42/comprovante-pdf", Perfil.CAIXA));
        assertTrue(rules.perfilPermitido(
                "POST", "/api/pedidos/42/transferir-mesa", Perfil.GARCOM));
        assertTrue(rules.perfilPermitido(
                "POST", "/api/pedidos/42/transferir-mesa", Perfil.CAIXA));
    }

    @Test
    void perfilNaoPermitidoERecusado() {
        assertFalse(rules.perfilPermitido("POST", "/api/pedidos", Perfil.COZINHA));
        assertTrue(rules.perfilPermitido(
                "PATCH", "/api/pedidos/42/itens/7/status", Perfil.COZINHA));
        assertTrue(rules.perfilPermitido(
                "PATCH", "/api/pedidos/42/itens/7/status", Perfil.ADMIN));
        assertFalse(rules.perfilPermitido(
                "PATCH", "/api/pedidos/42/itens/7/status", Perfil.GARCOM));
        assertFalse(rules.perfilPermitido("POST", "/api/mesas/42/pagamento/iniciar", Perfil.GARCOM));
        assertFalse(rules.perfilPermitido(
                "POST", "/api/mesas/42/pagamento/processar", Perfil.GARCOM));
        assertFalse(rules.perfilPermitido(
                "GET", "/api/pedidos/42/comprovante-pdf", Perfil.COZINHA));
        assertFalse(rules.perfilPermitido(
                "POST", "/api/pedidos/42/transferir-mesa", Perfil.COZINHA));
    }

    @Test
    void cancelarItemEPermitidoSoParaGarcomEAdmin() {
        assertTrue(rules.perfilPermitido("POST", "/api/pedidos/42/itens/7/cancelar", Perfil.GARCOM));
        assertTrue(rules.perfilPermitido("POST", "/api/pedidos/42/itens/7/cancelar", Perfil.ADMIN));
        assertFalse(rules.perfilPermitido("POST", "/api/pedidos/42/itens/7/cancelar", Perfil.COZINHA));
        assertFalse(rules.perfilPermitido("POST", "/api/pedidos/42/itens/7/cancelar", Perfil.CAIXA));
    }

    @Test
    void gestaoDeMesasESoDoAdministrador() {
        for (String[] rota : new String[][] {
                {"GET", "/api/mesas"}, {"PUT", "/api/mesas/3"}, {"DELETE", "/api/mesas/3"}}) {
            assertTrue(rules.perfilPermitido(rota[0], rota[1], Perfil.ADMIN));
            assertFalse(rules.perfilPermitido(rota[0], rota[1], Perfil.GARCOM));
            assertFalse(rules.perfilPermitido(rota[0], rota[1], Perfil.CAIXA));
            assertFalse(rules.perfilPermitido(rota[0], rota[1], Perfil.COZINHA));
        }
        assertTrue(rules.perfilPermitido("GET", "/api/mesas/mapa", Perfil.GARCOM), "mapa continua aberto");
    }

    @Test
    void metodoEOperacaoPrecisamEstarDeclarados() {
        assertFalse(rules.perfilPermitido("GET", "/api/rota-nao-cadastrada", Perfil.ADMIN));
        assertFalse(rules.perfilPermitido("POST", "/api/rota-nao-cadastrada", Perfil.ADMIN));
    }

    @Test
    void listarPedidosEPermitidoParaEquipeIncluindoCaixa() {
        assertTrue(rules.perfilPermitido("GET", "/api/pedidos", Perfil.ADMIN));
        assertTrue(rules.perfilPermitido("GET", "/api/pedidos", Perfil.GARCOM));
        assertTrue(rules.perfilPermitido("GET", "/api/pedidos", Perfil.COZINHA));
        assertTrue(rules.perfilPermitido("GET", "/api/pedidos", Perfil.CAIXA));
    }

    @Test
    void cadastrarUsuarioEExclusivoDoAdministrador() {
        assertTrue(rules.perfilPermitido("POST", "/api/usuarios", Perfil.ADMIN));
        assertTrue(rules.perfilPermitido("GET", "/api/usuarios", Perfil.ADMIN));
        assertTrue(rules.perfilPermitido("PUT", "/api/usuarios/1", Perfil.ADMIN));
        assertTrue(rules.perfilPermitido("DELETE", "/api/usuarios/1", Perfil.ADMIN));
        assertFalse(rules.perfilPermitido("POST", "/api/usuarios", Perfil.GARCOM));
        assertFalse(rules.perfilPermitido("DELETE", "/api/usuarios/1", Perfil.CAIXA));
    }

    @Test
    void administradorPodeGerenciarProdutos() {
        assertTrue(rules.perfilPermitido("GET", "/api/produtos", Perfil.ADMIN));
        assertTrue(rules.perfilPermitido("PUT", "/api/produtos/1", Perfil.ADMIN));
        assertTrue(rules.perfilPermitido("DELETE", "/api/produtos/1", Perfil.ADMIN));
        assertFalse(rules.perfilPermitido("PUT", "/api/produtos/1", Perfil.GARCOM));
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
    void caixaPodeOperarTurnoEConsultarRelatorios() {
        assertTrue(rules.perfilPermitido("POST", "/api/caixa/abrir", Perfil.CAIXA));
        assertTrue(rules.perfilPermitido("POST", "/api/caixa/fechar", Perfil.CAIXA));
        assertTrue(rules.perfilPermitido("GET", "/api/caixa/relatorios/2026-11-08/pdf", Perfil.CAIXA));
        assertTrue(rules.perfilPermitido("GET", "/api/caixa/configuracao", Perfil.CAIXA));
        assertFalse(rules.perfilPermitido("PUT", "/api/caixa/configuracao", Perfil.CAIXA));
    }

    @Test
    void comandaPodeSerConsultadaPelaEquipeDeAtendimento() {
        assertTrue(rules.perfilPermitido("GET", "/api/pedidos/42/comanda", Perfil.COZINHA));
        assertTrue(rules.perfilPermitido("GET", "/api/pedidos/42/comanda/pdf", Perfil.GARCOM));
        assertTrue(rules.perfilPermitido("GET", "/api/pedidos/42/comanda", Perfil.CAIXA));
        assertTrue(rules.perfilPermitido("GET", "/api/comandas", Perfil.CAIXA));
        assertTrue(rules.perfilPermitido("GET", "/api/caixa/3/resumo", Perfil.CAIXA));
        assertFalse(rules.perfilPermitido("GET", "/api/caixa/3/resumo", Perfil.GARCOM));
    }
}
