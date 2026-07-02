package edu.upc.sistema.gestionacademicaapi.entity;

import edu.upc.sistema.gestionacademicaapi.enums.ModalidadAsignacion;
import edu.upc.sistema.gestionacademicaapi.enums.TipoAula;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "docente_materia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocenteMateria {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "docente_id", nullable = false)
    private Usuario docente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "materia_id", nullable = false)
    private Materia materia;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_aula_requerida", nullable = false, length = 20)
    private TipoAula tipoAulaRequerida;

    @Enumerated(EnumType.STRING)
    @Column(name = "modalidad_asignacion", nullable = false, length = 20)
    private ModalidadAsignacion modalidadAsignacion;

    @Column(name = "fecha_alta", nullable = false)
    private LocalDateTime fechaAlta;

    @Column(name = "activo", nullable = false)
    private Boolean activo;
}
