package edu.upc.sistema.gestionacademicaapi.service;

import edu.upc.sistema.gestionacademicaapi.dto.AuditoriaResponse;
import edu.upc.sistema.gestionacademicaapi.entity.BitacoraAuditoria;
import edu.upc.sistema.gestionacademicaapi.entity.Usuario;
import edu.upc.sistema.gestionacademicaapi.enums.TipoUsuario;
import edu.upc.sistema.gestionacademicaapi.repository.BitacoraAuditoriaRepository;
import edu.upc.sistema.gestionacademicaapi.repository.UsuarioRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Registro de la bitacora de auditoria (HU-22, Anexo D).
 * Los eventos se persisten con REQUIRES_NEW para sobrevivir aunque la transaccion
 * de negocio haga rollback (p. ej. una operacion DENEGADA por regla de negocio).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditoriaService {

    // --- Acciones (Anexo D) ---
    public static final String LOGIN = "LOGIN";
    public static final String LOGIN_FALLIDO = "LOGIN_FALLIDO";
    public static final String CREA_USUARIO = "CREA_USUARIO";
    public static final String EDITA_USUARIO = "EDITA_USUARIO";
    public static final String ELIMINA_USUARIO = "ELIMINA_USUARIO";
    public static final String CAMBIO_PASSWORD = "CAMBIO_PASSWORD";
    public static final String RESET_PASSWORD = "RESET_PASSWORD";
    public static final String SOLICITA_PRESTAMO = "SOLICITA_PRESTAMO";
    public static final String DEVUELVE_PRESTAMO = "DEVUELVE_PRESTAMO";
    public static final String CREA_RESERVA = "CREA_RESERVA";
    public static final String AVALA_RESERVA = "AVALA_RESERVA";
    public static final String RECHAZA_RESERVA = "RECHAZA_RESERVA";
    public static final String BLOQUEO_POR_REGLA = "BLOQUEO_POR_REGLA";
    public static final String INSCRIBE_TUTORIA = "INSCRIBE_TUTORIA";
    public static final String CONSOLIDA_TUTORIA = "CONSOLIDA_TUTORIA";
    public static final String APLICA_PENALIZACION = "APLICA_PENALIZACION";
    public static final String LEVANTA_PENALIZACION = "LEVANTA_PENALIZACION";
    public static final String GENERA_REPORTE = "GENERA_REPORTE";

    // --- Resultados ---
    public static final String OK = "OK";
    public static final String DENEGADO = "DENEGADO";
    public static final String ERROR = "ERROR";

    private final BitacoraAuditoriaRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final CurrentUserService currentUserService;

    /** Registra un evento resolviendo el usuario desde el contexto de seguridad. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(String accion, String entidad, String entidadId, String resultado, String detalle) {
        String identificador = identificadorSeguro();
        registrarInterno(identificador, accion, entidad, entidadId, resultado, detalle);
    }

    /** Registra un evento con identificador explicito (p. ej. login, donde aun no hay contexto). */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarComo(String identificador, String accion, String entidad, String entidadId,
                              String resultado, String detalle) {
        registrarInterno(identificador, accion, entidad, entidadId, resultado, detalle);
    }

    private void registrarInterno(String identificador, String accion, String entidad, String entidadId,
                                  String resultado, String detalle) {
        try {
            Long usuarioId = identificador == null ? null
                    : usuarioRepository.findByIdentificadorCorporativo(identificador)
                    .map(Usuario::getId).orElse(null);

            HttpServletRequest req = requestActual();
            String ip = req != null ? clientIp(req) : null;
            String ua = req != null ? truncar(req.getHeader("User-Agent"), 300) : null;

            repository.save(BitacoraAuditoria.builder()
                    .usuarioId(usuarioId)
                    .usuarioIdentificador(identificador)
                    .accion(accion)
                    .entidad(entidad)
                    .entidadId(entidadId)
                    .resultado(resultado)
                    .ip(ip)
                    .userAgent(ua)
                    .detalle(truncar(detalle, 500))
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception ex) {
            // La auditoria nunca debe romper el flujo de negocio.
            log.warn("No se pudo registrar evento de auditoria [{}]: {}", accion, ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Page<AuditoriaResponse> consultar(String identificador, String accion, String entidad,
                                             LocalDateTime desde, LocalDateTime hasta, Pageable pageable) {
        currentUserService.exigirTipo(TipoUsuario.ADMINISTRATIVO);

        Specification<BitacoraAuditoria> spec = (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();
            if (identificador != null && !identificador.isBlank()) {
                predicados.add(cb.like(cb.lower(root.get("usuarioIdentificador")),
                        "%" + identificador.toLowerCase() + "%"));
            }
            if (accion != null && !accion.isBlank()) {
                predicados.add(cb.equal(root.get("accion"), accion));
            }
            if (entidad != null && !entidad.isBlank()) {
                predicados.add(cb.equal(root.get("entidad"), entidad));
            }
            if (desde != null) {
                predicados.add(cb.greaterThanOrEqualTo(root.get("timestamp"), desde));
            }
            if (hasta != null) {
                predicados.add(cb.lessThanOrEqualTo(root.get("timestamp"), hasta));
            }
            return cb.and(predicados.toArray(new Predicate[0]));
        };

        Pageable ordenado = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "timestamp"));
        return repository.findAll(spec, ordenado).map(this::toResponse);
    }

    private String identificadorSeguro() {
        try {
            return currentUserService.identificadorAutenticado();
        } catch (Exception ex) {
            return null;
        }
    }

    private HttpServletRequest requestActual() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attrs != null ? attrs.getRequest() : null;
        } catch (Exception ex) {
            return null;
        }
    }

    private String clientIp(HttpServletRequest req) {
        String fwd = req.getHeader("X-Forwarded-For");
        if (fwd != null && !fwd.isBlank()) {
            return fwd.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }

    private String truncar(String valor, int max) {
        if (valor == null) return null;
        return valor.length() <= max ? valor : valor.substring(0, max);
    }

    private AuditoriaResponse toResponse(BitacoraAuditoria b) {
        return AuditoriaResponse.builder()
                .id(b.getId())
                .usuarioId(b.getUsuarioId())
                .usuarioIdentificador(b.getUsuarioIdentificador())
                .accion(b.getAccion())
                .entidad(b.getEntidad())
                .entidadId(b.getEntidadId())
                .resultado(b.getResultado())
                .ip(b.getIp())
                .userAgent(b.getUserAgent())
                .detalle(b.getDetalle())
                .timestamp(b.getTimestamp())
                .build();
    }
}
