package edu.upc.sistema.gestionacademicaapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Inscripción de un alumno en la demanda de tutoría de una asignatura (HU-16).
 * Mientras {@code sesion} es null, cuenta para el quórum; al consolidarse se asocia a la sesión.
 */
@Entity
@Table(name = "demanda_tutoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandaTutoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "materia_id", nullable = false)
    private Materia materia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alumno_id", nullable = false)
    private Usuario alumno;

    @Column(name = "fecha_inscripcion", nullable = false)
    private LocalDateTime fechaInscripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sesion_id")
    private SesionTutoria sesion;

    /** HU-18: true si el alumno quedó en sala de espera por exceder el aforo. */
    @Column(name = "en_lista_espera", nullable = false)
    private Boolean enListaEspera;

    @Version
    @Column(name = "version")
    private Long version;
}
