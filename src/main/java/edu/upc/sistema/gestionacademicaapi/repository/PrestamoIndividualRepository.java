package edu.upc.sistema.gestionacademicaapi.repository;

import edu.upc.sistema.gestionacademicaapi.entity.PrestamoIndividual;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoPrestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PrestamoIndividualRepository extends JpaRepository<PrestamoIndividual, Long> {

    List<PrestamoIndividual> findByUsuarioSolicitante_IdAndEstado(Long usuarioSolicitanteId, EstadoPrestamo estado);

    List<PrestamoIndividual> findByUsuarioSolicitante_IdOrderByFechaInicioDesc(Long usuarioSolicitanteId);

    List<PrestamoIndividual> findByRecurso_IdAndEstado(Long recursoId, EstadoPrestamo estado);

    long countByUsuarioSolicitante_IdAndEstado(Long usuarioSolicitanteId, EstadoPrestamo estado);

    long countByUsuarioSolicitante_IdAndEstadoIn(Long usuarioSolicitanteId, Collection<EstadoPrestamo> estados);

    List<PrestamoIndividual> findByEstado(EstadoPrestamo estado);

    List<PrestamoIndividual> findByEstadoOrderByFechaFinAsc(EstadoPrestamo estado);

    List<PrestamoIndividual> findByEstadoOrderByFechaInicioDesc(EstadoPrestamo estado);
}
