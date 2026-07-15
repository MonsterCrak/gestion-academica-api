package edu.upc.sistema.gestionacademicaapi.repository;

import edu.upc.sistema.gestionacademicaapi.entity.TokenRevocado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRevocadoRepository extends JpaRepository<TokenRevocado, Long> {

    boolean existsByJti(String jti);
}
