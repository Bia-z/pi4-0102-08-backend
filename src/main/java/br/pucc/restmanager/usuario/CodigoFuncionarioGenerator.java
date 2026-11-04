package br.pucc.restmanager.usuario;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CodigoFuncionarioGenerator {

    private final UsuarioRepository usuarios;

    public CodigoFuncionarioGenerator(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public String gerar() {
        String codigo;
        do {
            codigo = "F" + UUID.randomUUID().toString().replace("-", "").substring(0, 19);
        } while (usuarios.findByCodigo(codigo).isPresent());
        return codigo;
    }
}