package edu.upc.sistema.gestionacademicaapi.config;

import edu.upc.sistema.gestionacademicaapi.entity.Usuario;
import edu.upc.sistema.gestionacademicaapi.enums.TipoUsuario;
import edu.upc.sistema.gestionacademicaapi.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            log.info("DevDataInitializer: ya existen usuarios, se omite el sembrado");
            return;
        }

        String adminPwd = envOrDefault("DEV_ADMIN_PASSWORD", "admin123");
        String docentePwd = envOrDefault("DEV_DOCENTE_PASSWORD", "docente123");
        String estudiantePwd = envOrDefault("DEV_ESTUDIANTE_PASSWORD", "estudiante123");

        usuarioRepository.save(Usuario.builder()
                .tipoUsuario(TipoUsuario.ADMINISTRATIVO)
                .identificadorCorporativo("admin")
                .nombre("Admin")
                .apellidos("Sistema")
                .activo(true)
                .passwordHash(passwordEncoder.encode(adminPwd))
                .build());

        usuarioRepository.save(Usuario.builder()
                .tipoUsuario(TipoUsuario.DOCENTE)
                .identificadorCorporativo("docente")
                .nombre("Docente")
                .apellidos("Demo")
                .activo(true)
                .passwordHash(passwordEncoder.encode(docentePwd))
                .build());

        usuarioRepository.save(Usuario.builder()
                .tipoUsuario(TipoUsuario.ESTUDIANTE)
                .identificadorCorporativo("estudiante")
                .nombre("Estudiante")
                .apellidos("Demo")
                .activo(true)
                .passwordHash(passwordEncoder.encode(estudiantePwd))
                .build());

        log.info("============================================================");
        log.info("DevDataInitializer: 3 usuarios sembrados (perfil dev)");
        log.info("  ADMIN       -> identificador=admin       password={}", adminPwd);
        log.info("  DOCENTE     -> identificador=docente     password={}", docentePwd);
        log.info("  ESTUDIANTE  -> identificador=estudiante  password={}", estudiantePwd);
        log.info("============================================================");
    }

    private String envOrDefault(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v;
    }
}