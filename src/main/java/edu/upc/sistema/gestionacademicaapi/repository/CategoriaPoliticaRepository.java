package edu.upc.sistema.gestionacademicaapi.repository;

import edu.upc.sistema.gestionacademicaapi.entity.CategoriaPolitica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoriaPoliticaRepository extends JpaRepository<CategoriaPolitica, Long> {

    Optional<CategoriaPolitica> findByNombreCategoria(String nombreCategoria);
}