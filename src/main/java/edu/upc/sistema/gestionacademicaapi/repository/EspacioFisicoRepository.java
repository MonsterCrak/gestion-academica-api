package edu.upc.sistema.gestionacademicaapi.repository;

import edu.upc.sistema.gestionacademicaapi.entity.EspacioFisico;
import edu.upc.sistema.gestionacademicaapi.enums.TipoEspacio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EspacioFisicoRepository extends JpaRepository<EspacioFisico, Long> {

    Optional<EspacioFisico> findByCodigo(String codigo);

    Page<EspacioFisico> findByTipoEspacio(TipoEspacio tipoEspacio, Pageable pageable);

    /** Usado internamente por TutoriaService para elegir aula al consolidar (sin paginar). */
    List<EspacioFisico> findByPermitirReservaCompletaTrue();

    Page<EspacioFisico> findByPermitirReservaCompletaTrue(Pageable pageable);

    List<EspacioFisico> findByPermitirReservaCompletaTrueAndTipoEspacio(TipoEspacio tipoEspacio);

    List<EspacioFisico> findByPermitirPrestamoIndividualTrueAndTipoEspacio(TipoEspacio tipoEspacio);

    long countByActivoTrue();
}