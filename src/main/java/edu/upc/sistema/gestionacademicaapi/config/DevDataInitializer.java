package edu.upc.sistema.gestionacademicaapi.config;

import edu.upc.sistema.gestionacademicaapi.entity.CategoriaPolitica;
import edu.upc.sistema.gestionacademicaapi.entity.DocenteMateria;
import edu.upc.sistema.gestionacademicaapi.entity.EspacioFisico;
import edu.upc.sistema.gestionacademicaapi.entity.Materia;
import edu.upc.sistema.gestionacademicaapi.entity.Recurso;
import edu.upc.sistema.gestionacademicaapi.entity.Usuario;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoRecurso;
import edu.upc.sistema.gestionacademicaapi.enums.ModalidadAsignacion;
import edu.upc.sistema.gestionacademicaapi.enums.TipoAula;
import edu.upc.sistema.gestionacademicaapi.enums.TipoEspacio;
import edu.upc.sistema.gestionacademicaapi.enums.TipoMovilidad;
import edu.upc.sistema.gestionacademicaapi.enums.TipoUsuario;
import edu.upc.sistema.gestionacademicaapi.repository.CategoriaPoliticaRepository;
import edu.upc.sistema.gestionacademicaapi.repository.DocenteMateriaRepository;
import edu.upc.sistema.gestionacademicaapi.repository.EspacioFisicoRepository;
import edu.upc.sistema.gestionacademicaapi.repository.MateriaRepository;
import edu.upc.sistema.gestionacademicaapi.repository.RecursoRepository;
import edu.upc.sistema.gestionacademicaapi.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Sembrado de datos para el perfil dev: usuarios de cada rol, categorías, aulas,
 * equipos y asignaturas con docentes, para poder ejercitar todos los módulos.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final CategoriaPoliticaRepository categoriaRepository;
    private final EspacioFisicoRepository espacioRepository;
    private final RecursoRepository recursoRepository;
    private final MateriaRepository materiaRepository;
    private final DocenteMateriaRepository docenteMateriaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            log.info("DevDataInitializer: ya existen datos, se omite el sembrado");
            return;
        }
        sembrarUsuarios();
        sembrarInventario();
        sembrarTutorias();
        log.info("DevDataInitializer: sembrado dev completo (usuarios, inventario y asignaturas)");
    }

    private void sembrarUsuarios() {
        String adminPwd = envOrDefault("DEV_ADMIN_PASSWORD", "admin123");
        String docentePwd = envOrDefault("DEV_DOCENTE_PASSWORD", "docente123");
        String estudiantePwd = envOrDefault("DEV_ESTUDIANTE_PASSWORD", "estudiante123");

        usuarioRepository.save(usuario(TipoUsuario.ADMINISTRATIVO, "admin", "admin@upc.edu.pe", "Admin", "Sistema", adminPwd));
        usuarioRepository.save(usuario(TipoUsuario.DOCENTE, "docente", "docente@upc.edu.pe", "Rosa", "Quispe", docentePwd));
        usuarioRepository.save(usuario(TipoUsuario.DOCENTE, "docente2", "docente2@upc.edu.pe", "Luis", "Herrera", docentePwd));

        usuarioRepository.save(usuario(TipoUsuario.ESTUDIANTE, "estudiante", "estudiante@upc.edu.pe", "Estudiante", "Demo", estudiantePwd));
        usuarioRepository.save(usuario(TipoUsuario.ESTUDIANTE, "estudiante2", "estudiante2@upc.edu.pe", "Ana", "Pérez", estudiantePwd));
        usuarioRepository.save(usuario(TipoUsuario.ESTUDIANTE, "estudiante3", "estudiante3@upc.edu.pe", "Diego", "Ramírez", estudiantePwd));
        usuarioRepository.save(usuario(TipoUsuario.ESTUDIANTE, "estudiante4", "estudiante4@upc.edu.pe", "Valeria", "Castro", estudiantePwd));
        usuarioRepository.save(usuario(TipoUsuario.ESTUDIANTE, "estudiante5", "estudiante5@upc.edu.pe", "Miguel", "Santos", estudiantePwd));

        log.info("============================================================");
        log.info("Usuarios dev: admin/{}  docente/{}  estudiante/{}", adminPwd, docentePwd, estudiantePwd);
        log.info("  (además docente2, estudiante2..estudiante5 con la misma clave)");
        log.info("============================================================");
    }

    private void sembrarInventario() {
        CategoriaPolitica computo = categoriaRepository.save(categoria("Cómputo", 1, 8));
        CategoriaPolitica audiovisual = categoriaRepository.save(categoria("Audiovisual", 2, 4));

        EspacioFisico aulaA201 = espacioRepository.save(espacio("AU-201", TipoEspacio.AULA_NORMAL, 40, false, true));
        EspacioFisico labB04 = espacioRepository.save(espacio("LAB-B04", TipoEspacio.COMPUTO, 24, true, true));
        espacioRepository.save(espacio("SALA-C1", TipoEspacio.LABORATORIO, 16, false, true));
        espacioRepository.save(espacio("LAB-E02", TipoEspacio.COMPUTO, 30, true, true));

        recursoRepository.save(recurso("EQ-001", "HP-2024-0183", "Laptop HP EliteBook 840", computo, TipoMovilidad.PORTATIL_ALMACEN, null));
        recursoRepository.save(recurso("EQ-002", "EP-2023-5521", "Proyector Epson EB-X51", audiovisual, TipoMovilidad.FIJO_EN_AULA, aulaA201));
        recursoRepository.save(recurso("EQ-003", "LN-2024-7781", "Tablet Lenovo Tab P12", computo, TipoMovilidad.PORTATIL_ALMACEN, null));
        recursoRepository.save(recurso("EQ-004", "CN-2023-0091", "Cámara Canon EOS R50", audiovisual, TipoMovilidad.PORTATIL_ALMACEN, null));
        recursoRepository.save(recurso("EQ-005", "DL-2024-3320", "Laptop Dell Latitude 5540", computo, TipoMovilidad.PORTATIL_ALMACEN, labB04));
        recursoRepository.save(recurso("EQ-006", "SH-2023-4410", "Micrófono Shure MV7", audiovisual, TipoMovilidad.PORTATIL_ALMACEN, null));
    }

    private void sembrarTutorias() {
        Usuario docente = usuarioRepository.findByIdentificadorCorporativo("docente").orElseThrow();
        Materia calculo = materiaRepository.save(materia("MAT101", "Cálculo I", "Ciencias"));
        Materia fisica = materiaRepository.save(materia("FIS201", "Física II", "Ciencias"));
        Materia quimica = materiaRepository.save(materia("QUI101", "Química General", "Ciencias"));

        docenteMateriaRepository.save(docenteMateria(docente, calculo));
        docenteMateriaRepository.save(docenteMateria(docente, fisica));
        docenteMateriaRepository.save(docenteMateria(docente, quimica));
    }

    private Usuario usuario(TipoUsuario tipo, String id, String email, String nombre, String apellidos, String pwd) {
        return Usuario.builder()
                .tipoUsuario(tipo)
                .identificadorCorporativo(id)
                .email(email)
                .nombre(nombre)
                .apellidos(apellidos)
                .activo(true)
                .passwordHash(passwordEncoder.encode(pwd))
                .build();
    }

    private CategoriaPolitica categoria(String nombre, int maxItems, int horas) {
        return CategoriaPolitica.builder()
                .nombreCategoria(nombre)
                .maxItemsPorAlumno(maxItems)
                .tiempoMaximoHoras(horas)
                .build();
    }

    private EspacioFisico espacio(String codigo, TipoEspacio tipo, int aforo, boolean prestamo, boolean reserva) {
        return EspacioFisico.builder()
                .codigo(codigo)
                .tipoEspacio(tipo)
                .aforo(aforo)
                .permitirPrestamoIndividual(prestamo)
                .permitirReservaCompleta(reserva)
                .activo(true)
                .build();
    }

    private Recurso recurso(String codigo, String serie, String nombre, CategoriaPolitica cat,
                            TipoMovilidad movilidad, EspacioFisico espacio) {
        return Recurso.builder()
                .categoria(cat)
                .codigoInventario(codigo)
                .numeroSerie(serie)
                .nombre(nombre)
                .tipoMovilidad(movilidad)
                .espacioActual(espacio)
                .estado(EstadoRecurso.DISPONIBLE)
                .requiereUbicacionFisica(movilidad == TipoMovilidad.FIJO_EN_AULA)
                .build();
    }

    private Materia materia(String codigo, String nombre, String departamento) {
        return Materia.builder().codigo(codigo).nombre(nombre).departamento(departamento).build();
    }

    private DocenteMateria docenteMateria(Usuario docente, Materia materia) {
        return DocenteMateria.builder()
                .docente(docente)
                .materia(materia)
                .tipoAulaRequerida(TipoAula.AULA_NORMAL)
                .modalidadAsignacion(ModalidadAsignacion.AUTOMATICA)
                .fechaAlta(LocalDateTime.now())
                .activo(true)
                .build();
    }

    private String envOrDefault(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v;
    }
}
