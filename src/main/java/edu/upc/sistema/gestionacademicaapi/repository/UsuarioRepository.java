package edu.upc.sistema.gestionacademicaapi.repository;

import edu.upc.sistema.gestionacademicaapi.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByIdentificadorCorporativo(String identificadorCorporativo);

    boolean existsByIdentificadorCorporativo(String identificadorCorporativo);
}
