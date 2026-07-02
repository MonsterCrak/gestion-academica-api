package edu.upc.sistema.gestionacademicaapi.repository;

import edu.upc.sistema.gestionacademicaapi.entity.EspacioFisico;
import edu.upc.sistema.gestionacademicaapi.enums.TipoEspacio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EspacioFisicoRepository extends JpaRepository<EspacioFisico, UUID> {

    Optional<EspacioFisico> findByCodigo(String codigo);

    List<EspacioFisico> findByTipoEspacio(TipoEspacio tipoEspacio);

    List<EspacioFisico> findByPermitirReservaCompletaTrue();

    List<EspacioFisico> findByPermitirReservaCompletaTrueAndTipoEspacio(TipoEspacio tipoEspacio);

    List<EspacioFisico> findByPermitirPrestamoIndividualTrueAndTipoEspacio(TipoEspacio tipoEspacio);
}
