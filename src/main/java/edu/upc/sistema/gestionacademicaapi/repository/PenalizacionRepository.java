package edu.upc.sistema.gestionacademicaapi.repository;

import edu.upc.sistema.gestionacademicaapi.entity.Penalizacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PenalizacionRepository extends JpaRepository<Penalizacion, Long> {

    Page<Penalizacion> findByUsuario_Id(Long usuarioId, Pageable pageable);

    List<Penalizacion> findByUsuario_IdAndActivaTrue(Long usuarioId);

    List<Penalizacion> findByActivaTrueAndFechaFinBefore(LocalDateTime momento);

    boolean existsByUsuario_IdAndActivaTrue(Long usuarioId);

    long countByActivaTrue();
}
