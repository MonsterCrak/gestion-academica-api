package edu.upc.sistema.gestionacademicaapi.service;

import edu.upc.sistema.gestionacademicaapi.dto.EspacioFisicoCreateRequest;
import edu.upc.sistema.gestionacademicaapi.dto.EspacioFisicoResponse;
import edu.upc.sistema.gestionacademicaapi.entity.EspacioFisico;
import edu.upc.sistema.gestionacademicaapi.enums.TipoEspacio;
import edu.upc.sistema.gestionacademicaapi.enums.TipoUsuario;
import edu.upc.sistema.gestionacademicaapi.exception.RecursoNoEncontradoException;
import edu.upc.sistema.gestionacademicaapi.repository.EspacioFisicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EspacioFisicoService {

    private final EspacioFisicoRepository repository;
    private final CurrentUserService currentUser;

    @Transactional(readOnly = true)
    public List<EspacioFisicoResponse> listar() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<EspacioFisicoResponse> listarPorTipo(TipoEspacio tipo) {
        return repository.findByTipoEspacio(tipo).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<EspacioFisicoResponse> listarReservables() {
        return repository.findByPermitirReservaCompletaTrue().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public EspacioFisicoResponse obtener(Long id) {
        return toResponse(buscarPorId(id));
    }

    @Transactional
    public EspacioFisicoResponse crear(EspacioFisicoCreateRequest req) {
        currentUser.exigirTipo(TipoUsuario.ADMINISTRATIVO);
        if (repository.findByCodigo(req.getCodigo()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un espacio con codigo " + req.getCodigo());
        }
        EspacioFisico e = EspacioFisico.builder()
                .codigo(req.getCodigo())
                .tipoEspacio(req.getTipoEspacio())
                .aforo(req.getAforo())
                .permitirPrestamoIndividual(req.getPermitirPrestamoIndividual())
                .permitirReservaCompleta(req.getPermitirReservaCompleta())
                .build();
        return toResponse(repository.save(e));
    }

    public EspacioFisico buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("EspacioFisico", id));
    }

    private EspacioFisicoResponse toResponse(EspacioFisico e) {
        return EspacioFisicoResponse.builder()
                .id(e.getId())
                .codigo(e.getCodigo())
                .tipoEspacio(e.getTipoEspacio())
                .aforo(e.getAforo())
                .permitirPrestamoIndividual(e.getPermitirPrestamoIndividual())
                .permitirReservaCompleta(e.getPermitirReservaCompleta())
                .build();
    }
}
