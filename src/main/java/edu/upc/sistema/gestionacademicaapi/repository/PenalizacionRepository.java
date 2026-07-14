package edu.upc.sistema.gestionacademicaapi.repository;

import edu.upc.sistema.gestionacademicaapi.entity.Penalizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PenalizacionRepository extends JpaRepository<Penalizacion, Long> {

    List<Penalizacion> findByUsuario_IdOrderByFechaInicioDesc(Long usuarioId);

    List<Penalizacion> findByUsuario_IdAndActivaTrue(Long usuarioId);

    List<Penalizacion> findByActivaTrueAndFechaFinBefore(LocalDateTime momento);

    boolean existsByUsuario_IdAndActivaTrue(Long usuarioId);

    long countByActivaTrue();
}
