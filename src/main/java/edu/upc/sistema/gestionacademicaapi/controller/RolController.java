package edu.upc.sistema.gestionacademicaapi.controller;

import edu.upc.sistema.gestionacademicaapi.dto.RolPermisosResponse;
import edu.upc.sistema.gestionacademicaapi.enums.TipoUsuario;
import edu.upc.sistema.gestionacademicaapi.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * HU-05: consulta de roles y sus permisos (RBAC). El sistema define tres roles fijos
 * (ADMINISTRATIVO, DOCENTE, ESTUDIANTE) cuya autorizacion se aplica en cada servicio
 * via CurrentUserService.exigirTipo(...) y en el filtro JWT (authority ROLE_&lt;tipo&gt;).
 */
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RolController {

    private final CurrentUserService currentUserService;

    @GetMapping
    public List<RolPermisosResponse> listar() {
        currentUserService.exigirTipo(TipoUsuario.ADMINISTRATIVO);
        return List.of(
                RolPermisosResponse.builder()
                        .rol(TipoUsuario.ADMINISTRATIVO)
                        .descripcion("Administrador del sistema")
                        .permisos(List.of(
                                "usuarios:gestionar", "roles:consultar", "inventario:gestionar",
                                "prestamos:devolver", "reservas:consultar", "tutorias:gestionar",
                                "penalizaciones:aplicar", "deuda:gestionar", "reportes:generar",
                                "auditoria:consultar"))
                        .build(),
                RolPermisosResponse.builder()
                        .rol(TipoUsuario.DOCENTE)
                        .descripcion("Docente / delegado avalista")
                        .permisos(List.of(
                                "catalogo:consultar", "reservas:avalar", "tutorias:dictar",
                                "perfil:gestionar"))
                        .build(),
                RolPermisosResponse.builder()
                        .rol(TipoUsuario.ESTUDIANTE)
                        .descripcion("Estudiante")
                        .permisos(List.of(
                                "catalogo:consultar", "prestamos:solicitar", "reservas:solicitar",
                                "tutorias:inscribirse", "perfil:gestionar"))
                        .build()
        );
    }
}
