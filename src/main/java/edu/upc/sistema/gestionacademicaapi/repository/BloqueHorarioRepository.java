package edu.upc.sistema.gestionacademicaapi.repository;

import edu.upc.sistema.gestionacademicaapi.entity.BloqueHorario;
import edu.upc.sistema.gestionacademicaapi.enums.TipoBloqueo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BloqueHorarioRepository extends JpaRepository<BloqueHorario, UUID> {

    List<BloqueHorario> findByEspacioFisico_IdAndTipoBloqueoAndActivoTrue(UUID espacioFisicoId, TipoBloqueo tipoBloqueo);

    List<BloqueHorario> findByRecurso_IdAndTipoBloqueoAndActivoTrue(UUID recursoId, TipoBloqueo tipoBloqueo);

    List<BloqueHorario> findByActivoTrue();
}
