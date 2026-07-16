package edu.upc.sistema.gestionacademicaapi.repository;

import edu.upc.sistema.gestionacademicaapi.entity.SesionTutoria;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoSesionTutoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SesionTutoriaRepository extends JpaRepository<SesionTutoria, Long> {

    List<SesionTutoria> findAllByOrderByFechaCreacionDesc();

    List<SesionTutoria> findByDocente_IdOrderByFechaHoraInicioAsc(Long docenteId);

    /** Sesiones abiertas a inscripción libre de alumnos (creadas por docente/admin). */
    List<SesionTutoria> findByAbiertaTrueAndEstadoOrderByFechaHoraInicioAsc(EstadoSesionTutoria estado);

    long countByEstado(EstadoSesionTutoria estado);
}
