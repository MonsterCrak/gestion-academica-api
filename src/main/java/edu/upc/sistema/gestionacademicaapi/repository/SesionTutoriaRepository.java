package edu.upc.sistema.gestionacademicaapi.repository;

import edu.upc.sistema.gestionacademicaapi.entity.SesionTutoria;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoSesionTutoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SesionTutoriaRepository extends JpaRepository<SesionTutoria, Long> {

    Page<SesionTutoria> findByDocente_Id(Long docenteId, Pageable pageable);

    long countByEstado(EstadoSesionTutoria estado);
}
