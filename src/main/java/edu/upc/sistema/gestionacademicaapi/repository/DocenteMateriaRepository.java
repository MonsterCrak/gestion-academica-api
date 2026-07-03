package edu.upc.sistema.gestionacademicaapi.repository;

import edu.upc.sistema.gestionacademicaapi.entity.DocenteMateria;
import edu.upc.sistema.gestionacademicaapi.enums.TipoAula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocenteMateriaRepository extends JpaRepository<DocenteMateria, Long> {

    List<DocenteMateria> findByDocente_IdAndActivoTrue(Long docenteId);

    Optional<DocenteMateria> findByDocente_IdAndMateria_IdAndActivoTrue(Long docenteId, Long materiaId);

    List<DocenteMateria> findByMateria_IdAndActivoTrue(Long materiaId);

    List<DocenteMateria> findByMateria_IdAndTipoAulaRequeridaAndActivoTrue(Long materiaId, TipoAula tipoAula);
}