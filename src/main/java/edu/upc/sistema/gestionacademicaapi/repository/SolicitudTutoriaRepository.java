package edu.upc.sistema.gestionacademicaapi.repository;

import edu.upc.sistema.gestionacademicaapi.entity.SolicitudTutoria;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudTutoriaRepository extends JpaRepository<SolicitudTutoria, Long> {

    Optional<SolicitudTutoria> findByTokenInvitacion(String tokenInvitacion);

    List<SolicitudTutoria> findByCreador_Id(Long creadorId);

    List<SolicitudTutoria> findByDocenteAsignado_Id(Long docenteAsignadoId);

    List<SolicitudTutoria> findByEstado(EstadoSolicitud estado);

    List<SolicitudTutoria> findByEstadoAndFechaHoraFinBefore(EstadoSolicitud estado, LocalDateTime fechaHoraFin);
}