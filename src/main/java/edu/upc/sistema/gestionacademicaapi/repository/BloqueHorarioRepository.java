package edu.upc.sistema.gestionacademicaapi.repository;

import edu.upc.sistema.gestionacademicaapi.entity.BloqueHorario;
import edu.upc.sistema.gestionacademicaapi.enums.TipoBloqueo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BloqueHorarioRepository extends JpaRepository<BloqueHorario, Long>,
        JpaSpecificationExecutor<BloqueHorario> {

    List<BloqueHorario> findByEspacioFisico_IdAndTipoBloqueoAndActivoTrue(Long espacioFisicoId, TipoBloqueo tipoBloqueo);

    List<BloqueHorario> findByRecurso_IdAndTipoBloqueoAndActivoTrue(Long recursoId, TipoBloqueo tipoBloqueo);
}