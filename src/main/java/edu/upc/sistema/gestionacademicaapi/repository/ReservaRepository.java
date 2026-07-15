package edu.upc.sistema.gestionacademicaapi.repository;

import edu.upc.sistema.gestionacademicaapi.entity.Reserva;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoReserva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    Page<Reserva> findBySolicitante_Id(Long solicitanteId, Pageable pageable);

    Page<Reserva> findByDocenteAvalista_IdAndEstado(Long docenteId, EstadoReserva estado, Pageable pageable);

    Page<Reserva> findByEstado(EstadoReserva estado, Pageable pageable);

    /** Choque de horario: existe una reserva del aula en el estado dado que se solapa con [inicio, fin). */
    boolean existsByEspacioFisico_IdAndEstadoAndFechaInicioLessThanAndFechaFinGreaterThan(
            Long espacioId, EstadoReserva estado, LocalDateTime fin, LocalDateTime inicio);

    long countByEstado(EstadoReserva estado);
}
