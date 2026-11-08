package br.pucc.restmanager.auth;

import br.pucc.restmanager.usuario.domain.Perfil;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.util.AntPathMatcher;

public class PerfilAccessRules implements AuthorizationManager<RequestAuthorizationContext> {

    private static final List<Operation> OPERATIONS = List.of(
            operation("POST", "/api/usuarios", Perfil.ADMIN),
            operation("GET", "/api/usuarios", Perfil.ADMIN),
            operation("PUT", "/api/usuarios/*", Perfil.ADMIN),
            operation("DELETE", "/api/usuarios/*", Perfil.ADMIN),
            operation("POST", "/api/produtos", Perfil.ADMIN),
            operation("GET", "/api/produtos", Perfil.ADMIN),
            operation("PUT", "/api/produtos/*", Perfil.ADMIN),
            operation("DELETE", "/api/produtos/*", Perfil.ADMIN),
            operation("GET", "/api/ingredientes", Perfil.ADMIN),
            operation("GET", "/api/ingredientes/gerenciamento", Perfil.ADMIN),
            operation("POST", "/api/ingredientes", Perfil.ADMIN),
            operation("PUT", "/api/ingredientes/*", Perfil.ADMIN),
            operation("DELETE", "/api/ingredientes/*", Perfil.ADMIN),
            operation("GET", "/api/cardapio",
                    Perfil.ADMIN, Perfil.GARCOM, Perfil.COZINHA, Perfil.CAIXA),
            operation("POST", "/api/mesas", Perfil.ADMIN),
            operation("POST", "/api/pedidos", Perfil.ADMIN, Perfil.GARCOM),
            operation("GET", "/api/pedidos",
                    Perfil.ADMIN, Perfil.GARCOM, Perfil.COZINHA, Perfil.CAIXA),
            operation("PATCH", "/api/pedidos/*/itens/*/status", Perfil.ADMIN, Perfil.COZINHA),
            operation("GET", "/api/mesas/mapa", Perfil.ADMIN, Perfil.GARCOM, Perfil.CAIXA),
            operation("POST", "/api/pedidos/*/pagamentos", Perfil.ADMIN, Perfil.CAIXA),
            operation("GET", "/api/pedidos/*/comprovante-pdf",
                    Perfil.ADMIN, Perfil.GARCOM, Perfil.CAIXA),
            operation("POST", "/api/pedidos/*/transferir-mesa",
                    Perfil.ADMIN, Perfil.GARCOM, Perfil.CAIXA),
            operation("POST", "/api/mesas/*/pagamento/iniciar", Perfil.ADMIN, Perfil.CAIXA),
            operation("POST", "/api/mesas/*/pagamento/cancelar", Perfil.ADMIN, Perfil.CAIXA),
            operation("POST", "/api/mesas/*/pagamento/processar", Perfil.ADMIN, Perfil.CAIXA),
            operation("GET", "/api/caixa/aberto", Perfil.ADMIN, Perfil.CAIXA),
            operation("POST", "/api/caixa/abrir", Perfil.ADMIN, Perfil.CAIXA),
            operation("POST", "/api/caixa/fechar", Perfil.ADMIN, Perfil.CAIXA),
            operation("GET", "/api/caixa/historico", Perfil.ADMIN, Perfil.CAIXA),
            operation("POST", "/api/caixa/movimentacoes", Perfil.ADMIN, Perfil.CAIXA),
            operation("GET", "/api/caixa/movimentacoes", Perfil.ADMIN, Perfil.CAIXA),
            operation("GET", "/api/caixa/relatorios/*", Perfil.ADMIN, Perfil.CAIXA),
            operation("GET", "/api/caixa/relatorios/*/pdf", Perfil.ADMIN, Perfil.CAIXA),
            operation("GET", "/api/caixa/configuracao", Perfil.ADMIN, Perfil.CAIXA),
            operation("PUT", "/api/caixa/configuracao", Perfil.ADMIN),
            operation("GET", "/api/pedidos/*/comanda", Perfil.ADMIN, Perfil.GARCOM, Perfil.COZINHA),
            operation("GET", "/api/pedidos/*/comanda/pdf", Perfil.ADMIN, Perfil.GARCOM, Perfil.COZINHA));

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public AuthorizationDecision check(
            Supplier<Authentication> authentication,
            RequestAuthorizationContext context) {
        Authentication current = authentication.get();
        if (current == null || !current.isAuthenticated()
                || current instanceof AnonymousAuthenticationToken) {
            return new AuthorizationDecision(false);
        }
        return new AuthorizationDecision(perfilPermitido(
                context.getRequest().getMethod(),
                caminhoDaRequisicao(context.getRequest()),
                perfilDa(current)));
    }

    private String caminhoDaRequisicao(jakarta.servlet.http.HttpServletRequest request) {
        String servletPath = request.getServletPath();
        if (servletPath != null && !servletPath.isEmpty()) {
            return servletPath;
        }
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        return contextPath != null && !contextPath.isEmpty()
                ? requestUri.substring(contextPath.length())
                : requestUri;
    }

    public boolean perfilPermitido(String metodo, String caminho, Perfil perfil) {
        if (metodo == null || caminho == null || perfil == null) {
            return false;
        }
        return OPERATIONS.stream()
                .filter(operation -> operation.metodo().equalsIgnoreCase(metodo)
                        && PATH_MATCHER.match(operation.caminho(), caminho))
                .anyMatch(operation -> operation.perfis().contains(perfil));
    }

    private Perfil perfilDa(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .map(value -> {
                    try {
                        return Perfil.valueOf(value);
                    } catch (IllegalArgumentException ex) {
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private static Operation operation(String metodo, String caminho, Perfil... perfis) {
        return new Operation(metodo, caminho, Set.of(perfis));
    }

    private record Operation(String metodo, String caminho, Set<Perfil> perfis) {
    }
}
