package edu.upc.sistema.gestionacademicaapi.repository;

import edu.upc.sistema.gestionacademicaapi.entity.BitacoraAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BitacoraAuditoriaRepository
        extends JpaRepository<BitacoraAuditoria, Long>, JpaSpecificationExecutor<BitacoraAuditoria> {
}
