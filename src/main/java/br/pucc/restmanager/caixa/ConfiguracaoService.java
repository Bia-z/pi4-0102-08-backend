package br.pucc.restmanager.caixa;

import br.pucc.restmanager.caixa.dto.ConfiguracaoRequest;
import br.pucc.restmanager.caixa.dto.ConfiguracaoResponse;
import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import br.pucc.restmanager.usuario.UsuarioRepository;
import br.pucc.restmanager.usuario.domain.Perfil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ConfiguracaoService {

    private final ConfiguracaoRepository configuracoes;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;

    public ConfiguracaoService(
            ConfiguracaoRepository configuracoes,
            UsuarioRepository usuarios,
            PasswordEncoder passwordEncoder) {
        this.configuracoes = configuracoes;
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
    }

    public ConfiguracaoResponse buscar() {
        return configuracoes.buscar();
    }

    public ConfiguracaoResponse atualizar(ConfiguracaoRequest request) {
        if (request == null || request.nomeRestaurante() == null
                || request.nomeRestaurante().isBlank() || request.nomeRestaurante().trim().length() > 120
                || request.taxaServicoPercentual() == null
                || request.taxaServicoPercentual().signum() < 0
                || request.taxaServicoPercentual().compareTo(new BigDecimal("100")) > 0
                || request.taxaServicoAtiva() == null
                || request.imprimirComandaAoAceitar() == null
                || request.comandaUnica() == null
                || request.imprimirComprovanteAoFechar() == null) {
            throw new BusinessException(ErrorCode.CONFIGURACAO_INVALIDA);
        }
        validarCredenciais(request.codigoUsuario(), request.senha());
        try {
            BigDecimal percentual = request.taxaServicoPercentual()
                    .setScale(2, RoundingMode.UNNECESSARY);
            return configuracoes.atualizar(
                    request.nomeRestaurante().trim(), percentual, request.taxaServicoAtiva(),
                    request.imprimirComandaAoAceitar(), request.comandaUnica(),
                    request.imprimirComprovanteAoFechar());
        } catch (ArithmeticException ex) {
            throw new BusinessException(ErrorCode.CONFIGURACAO_INVALIDA);
        }
    }

    private void validarCredenciais(String codigoUsuario, String senha) {
        if (senha == null || senha.isBlank() || codigoUsuario == null || codigoUsuario.isBlank()) {
            throw new BusinessException(ErrorCode.CREDENCIAIS_INVALIDAS);
        }
        boolean credenciaisValidas = usuarios.findByCodigo(codigoUsuario.trim())
                .filter(usuario -> usuario.isAtivo()
                        && (usuario.getPerfil() == Perfil.CAIXA || usuario.getPerfil() == Perfil.ADMIN)
                        && passwordEncoder.matches(senha, usuario.getSenha()))
                .isPresent();
        if (!credenciaisValidas) {
            throw new BusinessException(ErrorCode.CREDENCIAIS_INVALIDAS);
        }
    }
}
