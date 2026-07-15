package edu.upc.sistema.gestionacademicaapi.repository;

import edu.upc.sistema.gestionacademicaapi.entity.PrestamoIndividual;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoPrestamo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrestamoIndividualRepository extends JpaRepository<PrestamoIndividual, Long>,
        JpaSpecificationExecutor<PrestamoIndividual> {

    List<PrestamoIndividual> findByUsuarioSolicitante_IdAndEstado(Long usuarioSolicitanteId, EstadoPrestamo estado);

    Page<PrestamoIndividual> findByUsuarioSolicitante_Id(Long usuarioSolicitanteId, Pageable pageable);

    List<PrestamoIndividual> findByRecurso_IdAndEstado(Long recursoId, EstadoPrestamo estado);

    long countByUsuarioSolicitante_IdAndEstado(Long usuarioSolicitanteId, EstadoPrestamo estado);

    List<PrestamoIndividual> findByEstado(EstadoPrestamo estado);

    Page<PrestamoIndividual> findByEstado(EstadoPrestamo estado, Pageable pageable);
}
