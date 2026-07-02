package edu.upc.sistema.gestionacademicaapi.repository;

import edu.upc.sistema.gestionacademicaapi.entity.PrestamoIndividual;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoPrestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PrestamoIndividualRepository extends JpaRepository<PrestamoIndividual, UUID> {

    List<PrestamoIndividual> findByUsuarioSolicitante_IdAndEstado(UUID usuarioSolicitanteId, EstadoPrestamo estado);

    List<PrestamoIndividual> findByRecurso_IdAndEstado(UUID recursoId, EstadoPrestamo estado);

    long countByUsuarioSolicitante_IdAndEstado(UUID usuarioSolicitanteId, EstadoPrestamo estado);

    List<PrestamoIndividual> findByEstado(EstadoPrestamo estado);
}
