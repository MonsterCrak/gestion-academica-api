package edu.upc.sistema.gestionacademicaapi.repository;

import edu.upc.sistema.gestionacademicaapi.entity.Materia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MateriaRepository extends JpaRepository<Materia, UUID> {

    Optional<Materia> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}
