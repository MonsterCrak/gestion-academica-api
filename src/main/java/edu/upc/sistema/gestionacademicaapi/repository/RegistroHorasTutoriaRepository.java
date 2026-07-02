package edu.upc.sistema.gestionacademicaapi.repository;

import edu.upc.sistema.gestionacademicaapi.entity.RegistroHorasTutoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RegistroHorasTutoriaRepository extends JpaRepository<RegistroHorasTutoria, Long> {

    List<RegistroHorasTutoria> findByDocente_Id(Long docenteId);

    List<RegistroHorasTutoria> findByDocente_IdAndFechaHoraInicioRealBetween(
            Long docenteId, LocalDateTime desde, LocalDateTime hasta);

    List<RegistroHorasTutoria> findBySolicitud_Id(Long solicitudId);
}