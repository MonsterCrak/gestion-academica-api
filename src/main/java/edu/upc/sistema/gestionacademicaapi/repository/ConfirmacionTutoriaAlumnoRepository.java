package edu.upc.sistema.gestionacademicaapi.repository;

import edu.upc.sistema.gestionacademicaapi.entity.ConfirmacionTutoriaAlumno;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoConfirmacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfirmacionTutoriaAlumnoRepository extends JpaRepository<ConfirmacionTutoriaAlumno, Long> {

    List<ConfirmacionTutoriaAlumno> findBySolicitud_Id(Long solicitudId);

    Optional<ConfirmacionTutoriaAlumno> findBySolicitud_IdAndAlumno_Id(Long solicitudId, Long alumnoId);

    long countBySolicitud_IdAndEstadoConfirmacion(Long solicitudId, EstadoConfirmacion estadoConfirmacion);

    long countBySolicitud_IdAndApeloTrue(Long solicitudId);
}