package edu.upc.sistema.gestionacademicaapi.repository;

import edu.upc.sistema.gestionacademicaapi.entity.Recurso;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoRecurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecursoRepository extends JpaRepository<Recurso, Long>, JpaSpecificationExecutor<Recurso> {

    Optional<Recurso> findByCodigoInventario(String codigoInventario);

    boolean existsByCodigoInventario(String codigoInventario);

    boolean existsByCodigoInventarioAndIdNot(String codigoInventario, Long id);

    boolean existsByNumeroSerieIgnoreCase(String numeroSerie);

    boolean existsByNumeroSerieIgnoreCaseAndIdNot(String numeroSerie, Long id);

    long countByEstado(EstadoRecurso estado);

    List<Recurso> findByCategoria_Id(Long categoriaId);

    List<Recurso> findByEspacioActual_Id(Long espacioActualId);

    List<Recurso> findByEspacioActual_IdAndEstado(Long espacioActualId, EstadoRecurso estado);
}
