package edu.upc.sistema.gestionacademicaapi.repository;

import edu.upc.sistema.gestionacademicaapi.entity.Reserva;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findBySolicitante_IdOrderByFechaInicioDesc(Long solicitanteId);

    List<Reserva> findByDocenteAvalista_IdAndEstadoOrderByFechaInicioAsc(Long docenteId, EstadoReserva estado);

    List<Reserva> findByEstadoOrderByFechaInicioAsc(EstadoReserva estado);

    /** Choque de horario: existe una reserva del aula en el estado dado que se solapa con [inicio, fin). */
    boolean existsByEspacioFisico_IdAndEstadoAndFechaInicioLessThanAndFechaFinGreaterThan(
            Long espacioId, EstadoReserva estado, LocalDateTime fin, LocalDateTime inicio);

    long countByEstado(EstadoReserva estado);
}
