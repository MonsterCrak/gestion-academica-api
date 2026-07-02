package edu.upc.sistema.gestionacademicaapi.repository;

import edu.upc.sistema.gestionacademicaapi.entity.DocenteMateria;
import edu.upc.sistema.gestionacademicaapi.enums.TipoAula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocenteMateriaRepository extends JpaRepository<DocenteMateria, UUID> {

    List<DocenteMateria> findByDocente_IdAndActivoTrue(UUID docenteId);

    Optional<DocenteMateria> findByDocente_IdAndMateria_IdAndActivoTrue(UUID docenteId, UUID materiaId);

    List<DocenteMateria> findByMateria_IdAndActivoTrue(UUID materiaId);

    List<DocenteMateria> findByMateria_IdAndTipoAulaRequeridaAndActivoTrue(UUID materiaId, TipoAula tipoAula);
}
