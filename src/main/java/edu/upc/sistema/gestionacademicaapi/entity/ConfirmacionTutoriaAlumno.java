package edu.upc.sistema.gestionacademicaapi.entity;

import edu.upc.sistema.gestionacademicaapi.enums.EstadoConfirmacion;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "confirmacion_tutoria_alumno")
@Getter
@Setter
@NoArgsConstructor
public class ConfirmacionTutoriaAlumno {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private SolicitudTutoria solicitud;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alumno_id", nullable = false)
    private Usuario alumno;

    @Column(name = "fecha_confirmacion", nullable = false)
    private LocalDateTime fechaConfirmacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_confirmacion", nullable = false, length = 20)
    private EstadoConfirmacion estadoConfirmacion;

    @Column(name = "asistio")
    private Boolean asistio;

    @Column(name = "fecha_registro_asistencia")
    private LocalDateTime fechaRegistroAsistencia;

    @Column(name = "apelo", nullable = false)
    private Boolean apelo;

    @Column(name = "motivo_apelacion", columnDefinition = "TEXT")
    private String motivoApelacion;

    @Column(name = "fecha_apelacion")
    private LocalDateTime fechaApelacion;
}
