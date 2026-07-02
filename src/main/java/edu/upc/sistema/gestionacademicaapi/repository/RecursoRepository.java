package edu.upc.sistema.gestionacademicaapi.repository;

import edu.upc.sistema.gestionacademicaapi.entity.Recurso;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoRecurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecursoRepository extends JpaRepository<Recurso, UUID> {

    Optional<Recurso> findByCodigoInventario(String codigoInventario);

    List<Recurso> findByCategoria_Id(UUID categoriaId);

    List<Recurso> findByEspacioActual_Id(UUID espacioActualId);

    List<Recurso> findByEspacioActual_IdAndEstado(UUID espacioActualId, EstadoRecurso estado);
}
