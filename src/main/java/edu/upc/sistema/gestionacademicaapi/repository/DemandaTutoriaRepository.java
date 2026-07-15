package edu.upc.sistema.gestionacademicaapi.repository;

import edu.upc.sistema.gestionacademicaapi.entity.DemandaTutoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandaTutoriaRepository extends JpaRepository<DemandaTutoria, Long> {

    long countByMateria_IdAndSesionIsNull(Long materiaId);

    List<DemandaTutoria> findByMateria_IdAndSesionIsNullOrderByFechaInscripcionAsc(Long materiaId);

    boolean existsByMateria_IdAndAlumno_IdAndSesionIsNull(Long materiaId, Long alumnoId);

    /** Usado internamente por TutoriaService.listarSesiones() (rama ESTUDIANTE) para derivar sesiones distintas. */
    List<DemandaTutoria> findByAlumno_IdOrderByFechaInscripcionDesc(Long alumnoId);

    Page<DemandaTutoria> findByAlumno_Id(Long alumnoId, Pageable pageable);

    List<DemandaTutoria> findBySesion_Id(Long sesionId);

    long countBySesion_IdAndEnListaEsperaFalse(Long sesionId);

    long countBySesion_IdAndEnListaEsperaTrue(Long sesionId);

    long countBySesionIsNull();
}
