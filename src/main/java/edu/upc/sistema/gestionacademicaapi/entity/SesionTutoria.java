package edu.upc.sistema.gestionacademicaapi.entity;

import edu.upc.sistema.gestionacademicaapi.enums.EstadoSesionTutoria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Sesión de tutoría consolidada al alcanzar el quórum (HU-17).
 */
@Entity
@Table(name = "sesion_tutoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SesionTutoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "materia_id", nullable = false)
    private Materia materia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docente_id")
    private Usuario docente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aula_id")
    private EspacioFisico aula;

    @Column(name = "fecha_hora_inicio")
    private LocalDateTime fechaHoraInicio;

    @Column(name = "fecha_hora_fin")
    private LocalDateTime fechaHoraFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 25)
    private EstadoSesionTutoria estado;

    /** Cupo (aforo del aula asignada o el definido al crear una sesión abierta). */
    @Column(name = "cupo")
    private Integer cupo;

    /**
     * True si la sesión fue creada directamente por un docente/administrador y admite
     * inscripción libre de alumnos hasta llenar el cupo. False para las sesiones
     * consolidadas por maduración de demanda (quórum).
     */
    @Builder.Default
    @Column(name = "abierta", nullable = false)
    private Boolean abierta = false;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Version
    @Column(name = "version")
    private Long version;
}
