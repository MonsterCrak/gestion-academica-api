package edu.upc.sistema.gestionacademicaapi.repository;

import edu.upc.sistema.gestionacademicaapi.entity.DemandaTutoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandaTutoriaRepository extends JpaRepository<DemandaTutoria, Long> {

    long countByMateria_IdAndSesionIsNull(Long materiaId);

    List<DemandaTutoria> findByMateria_IdAndSesionIsNullOrderByFechaInscripcionAsc(Long materiaId);

    boolean existsByMateria_IdAndAlumno_IdAndSesionIsNull(Long materiaId, Long alumnoId);

    List<DemandaTutoria> findByAlumno_IdOrderByFechaInscripcionDesc(Long alumnoId);

    List<DemandaTutoria> findBySesion_Id(Long sesionId);

    boolean existsBySesion_IdAndAlumno_Id(Long sesionId, Long alumnoId);

    List<DemandaTutoria> findBySesion_IdAndAlumno_Id(Long sesionId, Long alumnoId);

    List<DemandaTutoria> findByMateria_IdAndAlumno_IdAndSesionIsNull(Long materiaId, Long alumnoId);

    long countBySesion_IdAndEnListaEsperaFalse(Long sesionId);

    long countBySesion_IdAndEnListaEsperaTrue(Long sesionId);

    long countBySesionIsNull();
}
