package br.pucc.restmanager.usuario;

import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import br.pucc.restmanager.usuario.domain.Perfil;
import br.pucc.restmanager.usuario.domain.Usuario;
import br.pucc.restmanager.usuario.dto.CadastrarUsuarioRequest;
import br.pucc.restmanager.usuario.dto.UsuarioResponse;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final CodigoFuncionarioGenerator codigoGenerator;

    public UsuarioService(
            UsuarioRepository usuarios,
            PasswordEncoder passwordEncoder,
            CodigoFuncionarioGenerator codigoGenerator) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.codigoGenerator = codigoGenerator;
    }

    @Transactional
    public UsuarioResponse cadastrar(CadastrarUsuarioRequest request) {
        Perfil perfil;
        try {
            perfil = Perfil.valueOf(request.perfil().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new BusinessException(ErrorCode.PERFIL_INVALIDO);
        }

        String codigo = request.codigo() == null || request.codigo().isBlank()
                ? codigoGenerator.gerar()
                : request.codigo().trim();
        if (usuarios.findByCodigo(codigo).isPresent()) {
            throw new BusinessException(ErrorCode.CODIGO_DUPLICADO);
        }

        Usuario usuario = new Usuario(
                codigo,
                request.nome().trim(),
                null,
                request.cpf().trim(),
                request.telefone().trim(),
                passwordEncoder.encode(request.senha()),
                perfil,
                true);
        return UsuarioResponse.from(usuarios.save(usuario));
    }
}