package edu.upc.sistema.gestionacademicaapi.repository;

import edu.upc.sistema.gestionacademicaapi.entity.RegistroHorasTutoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RegistroHorasTutoriaRepository extends JpaRepository<RegistroHorasTutoria, UUID> {

    List<RegistroHorasTutoria> findByDocente_Id(UUID docenteId);

    List<RegistroHorasTutoria> findByDocente_IdAndFechaHoraInicioRealBetween(
            UUID docenteId, LocalDateTime desde, LocalDateTime hasta);

    List<RegistroHorasTutoria> findBySolicitud_Id(UUID solicitudId);
}
