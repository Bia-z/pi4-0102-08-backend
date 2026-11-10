package br.pucc.restmanager.usuario;

import br.pucc.restmanager.common.BusinessException;
import br.pucc.restmanager.common.ErrorCode;
import br.pucc.restmanager.usuario.domain.Perfil;
import br.pucc.restmanager.usuario.domain.Usuario;
import br.pucc.restmanager.usuario.dto.AtualizarUsuarioRequest;
import br.pucc.restmanager.usuario.dto.CadastrarUsuarioRequest;
import br.pucc.restmanager.usuario.dto.UsuarioResponse;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+"
                    + "(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*@"
                    + "(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+"
                    + "[A-Za-z]{2,63}$");
    private static final Pattern PHONE_FORMAT_PATTERN =
            Pattern.compile("^[+0-9() .-]+$");
    private static final Set<String> BRAZILIAN_AREA_CODES = Set.of(
            "11", "12", "13", "14", "15", "16", "17", "18", "19",
            "21", "22", "24", "27", "28",
            "31", "32", "33", "34", "35", "37", "38",
            "41", "42", "43", "44", "45", "46", "47", "48", "49",
            "51", "53", "54", "55",
            "61", "62", "63", "64", "65", "66", "67", "68", "69",
            "71", "73", "74", "75", "77", "79",
            "81", "82", "83", "84", "85", "86", "87", "88", "89",
            "91", "92", "93", "94", "95", "96", "97", "98", "99");

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
        String cpf = normalizarCpf(request.cpf());
        if (!cpfValido(cpf)) {
            throw new BusinessException(ErrorCode.CPF_INVALIDO);
        }
        if (usuarios.existsByCpf(cpf)) {
            throw new BusinessException(ErrorCode.CPF_DUPLICADO);
        }

        String telefone = normalizarTelefone(request.telefone());
        String email = normalizarEmail(request.email());

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
                email,
                cpf,
                telefone,
                passwordEncoder.encode(request.senha()),
                perfil,
                true);
        return UsuarioResponse.from(usuarios.save(usuario));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarios.findAllByOrderByNomeAsc().stream()
                .map(UsuarioResponse::from)
                .toList();
    }

    @Transactional
    public UsuarioResponse atualizar(
            Long id, AtualizarUsuarioRequest request, String codigoAdministrador) {
        Usuario usuario = usuarios.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USUARIO_NAO_ENCONTRADO));
        if (!request.ativo() && usuario.getCodigo().equals(codigoAdministrador)) {
            throw new BusinessException(ErrorCode.ADMINISTRADOR_NAO_PODE_SE_DESATIVAR);
        }
        String cpf = normalizarCpf(request.cpf());
        if (!cpfValido(cpf)) {
            throw new BusinessException(ErrorCode.CPF_INVALIDO);
        }
        if (usuarios.existsByCpfAndIdNot(cpf, id)) {
            throw new BusinessException(ErrorCode.CPF_DUPLICADO);
        }
        String codigo = request.codigo().trim();
        if (usuarios.existsByCodigoAndIdNot(codigo, id)) {
            throw new BusinessException(ErrorCode.CODIGO_DUPLICADO);
        }

        String telefone = normalizarTelefone(request.telefone());
        String email = normalizarEmail(request.email(), id);
        Perfil perfil;
        try {
            perfil = Perfil.valueOf(request.perfil().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new BusinessException(ErrorCode.PERFIL_INVALIDO);
        }
        if (usuario.isAtivo() && usuario.getPerfil() == Perfil.ADMIN
                && (perfil != Perfil.ADMIN || !request.ativo())
                && usuarios.countByPerfilAndAtivoTrue(Perfil.ADMIN) <= 1) {
            throw new BusinessException(ErrorCode.ULTIMO_ADMINISTRADOR_ATIVO);
        }
        String senha = request.senha() == null || request.senha().isBlank()
                ? null
                : passwordEncoder.encode(request.senha());
        usuario.atualizar(
                codigo, request.nome().trim(), email, cpf, telefone, senha, perfil, request.ativo());
        return UsuarioResponse.from(usuario);
    }

    @Transactional
    public void desativar(Long id, String codigoAdministrador) {
        Usuario usuario = usuarios.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USUARIO_NAO_ENCONTRADO));
        if (usuario.getCodigo().equals(codigoAdministrador)) {
            throw new BusinessException(ErrorCode.ADMINISTRADOR_NAO_PODE_SE_DESATIVAR);
        }
        if (usuario.isAtivo() && usuario.getPerfil() == Perfil.ADMIN
                && usuarios.countByPerfilAndAtivoTrue(Perfil.ADMIN) <= 1) {
            throw new BusinessException(ErrorCode.ULTIMO_ADMINISTRADOR_ATIVO);
        }
        usuario.desativar();
    }

    private String normalizarCpf(String cpf) {
        if (cpf == null || (!cpf.matches("\\d{11}")
                && !cpf.matches("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}"))) {
            return "";
        }
        return cpf.replaceAll("[.\\-]", "");
    }

    private boolean cpfValido(String cpf) {
        if (!cpf.matches("\\d{11}") || cpf.chars().distinct().count() == 1) {
            return false;
        }
        int primeiroDigito = calcularDigitoCpf(cpf, 9);
        int segundoDigito = calcularDigitoCpf(cpf, 10);
        return cpf.charAt(9) - '0' == primeiroDigito
                && cpf.charAt(10) - '0' == segundoDigito;
    }

    private int calcularDigitoCpf(String cpf, int quantidadeDigitosBase) {
        int soma = 0;
        for (int i = 0; i < quantidadeDigitosBase; i++) {
            soma += (cpf.charAt(i) - '0') * (quantidadeDigitosBase + 1 - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private String normalizarTelefone(String telefone) {
        if (telefone == null || !PHONE_FORMAT_PATTERN.matcher(telefone).matches()) {
            throw new BusinessException(ErrorCode.TELEFONE_INVALIDO);
        }
        String digitos = telefone.replaceAll("\\D", "");
        if (digitos.startsWith("55") && (digitos.length() == 12 || digitos.length() == 13)) {
            digitos = digitos.substring(2);
        }
        if (digitos.length() != 10 && digitos.length() != 11) {
            throw new BusinessException(ErrorCode.TELEFONE_INVALIDO);
        }
        String ddd = digitos.substring(0, 2);
        char primeiroDigitoAssinante = digitos.charAt(2);
        boolean formatoValido = BRAZILIAN_AREA_CODES.contains(ddd)
                && (digitos.length() == 11
                ? primeiroDigitoAssinante == '9'
                : primeiroDigitoAssinante >= '2' && primeiroDigitoAssinante <= '5');
        if (!formatoValido) {
            throw new BusinessException(ErrorCode.TELEFONE_INVALIDO);
        }
        return digitos;
    }

    private String normalizarEmail(String email) {
        return normalizarEmail(email, null);
    }

    private String normalizarEmail(String email, Long usuarioId) {
        if (email == null || email.isBlank()) {
            return null;
        }
        String normalizado = email.trim().toLowerCase(Locale.ROOT);
        if (normalizado.length() > 160 || !EMAIL_PATTERN.matcher(normalizado).matches()) {
            throw new BusinessException(ErrorCode.EMAIL_INVALIDO);
        }
        boolean duplicado = usuarioId == null
                ? usuarios.existsByEmailIgnoreCase(normalizado)
                : usuarios.existsByEmailIgnoreCaseAndIdNot(normalizado, usuarioId);
        if (duplicado) {
            throw new BusinessException(ErrorCode.EMAIL_DUPLICADO);
        }
        return normalizado;
    }
}