package edu.upc.sistema.gestionacademicaapi.repository;

import edu.upc.sistema.gestionacademicaapi.entity.ConfirmacionTutoriaAlumno;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoConfirmacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConfirmacionTutoriaAlumnoRepository extends JpaRepository<ConfirmacionTutoriaAlumno, UUID> {

    List<ConfirmacionTutoriaAlumno> findBySolicitud_Id(UUID solicitudId);

    Optional<ConfirmacionTutoriaAlumno> findBySolicitud_IdAndAlumno_Id(UUID solicitudId, UUID alumnoId);

    long countBySolicitud_IdAndEstadoConfirmacion(UUID solicitudId, EstadoConfirmacion estadoConfirmacion);

    long countBySolicitud_IdAndApeloTrue(UUID solicitudId);
}
