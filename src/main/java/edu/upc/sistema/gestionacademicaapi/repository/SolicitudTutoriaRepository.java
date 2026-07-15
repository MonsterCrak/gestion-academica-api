package edu.upc.sistema.gestionacademicaapi.repository;

import edu.upc.sistema.gestionacademicaapi.entity.SolicitudTutoria;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoSolicitud;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudTutoriaRepository extends JpaRepository<SolicitudTutoria, Long>,
        JpaSpecificationExecutor<SolicitudTutoria> {

    Optional<SolicitudTutoria> findByTokenInvitacion(String tokenInvitacion);

    Page<SolicitudTutoria> findByCreador_Id(Long creadorId, Pageable pageable);

    Page<SolicitudTutoria> findByDocenteAsignado_Id(Long docenteAsignadoId, Pageable pageable);

    List<SolicitudTutoria> findByEstado(EstadoSolicitud estado);

    List<SolicitudTutoria> findByEstadoAndFechaHoraFinBefore(EstadoSolicitud estado, LocalDateTime fechaHoraFin);
}