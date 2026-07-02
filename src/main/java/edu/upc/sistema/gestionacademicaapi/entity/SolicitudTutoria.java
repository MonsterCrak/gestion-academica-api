package edu.upc.sistema.gestionacademicaapi.entity;

import edu.upc.sistema.gestionacademicaapi.enums.EstadoSolicitud;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitud_tutoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudTutoria {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creador_id", nullable = false)
    private Usuario creador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "materia_id", nullable = false)
    private Materia materia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "espacio_asignado_id")
    private EspacioFisico espacioAsignado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docente_asignado_id")
    private Usuario docenteAsignado;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_aula_solicitada", nullable = false, length = 20)
    private TipoAula tipoAulaSolicitada;

    @Column(name = "fecha_hora_inicio", nullable = false)
    private LocalDateTime fechaHoraInicio;

    @Column(name = "fecha_hora_fin", nullable = false)
    private LocalDateTime fechaHoraFin;

    @Column(name = "duracion_horas", nullable = false, precision = 3, scale = 2)
    private BigDecimal duracionHoras;

    @Column(name = "token_invitacion", nullable = false, unique = true, length = 64)
    private String tokenInvitacion;

    @Column(name = "fecha_expiracion_token", nullable = false)
    private LocalDateTime fechaExpiracionToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoSolicitud estado;

    @Column(name = "docente_confirmo_realizacion", nullable = false)
    private Boolean docenteConfirmoRealizacion;

    @Column(name = "docente_confirmo_realizacion_en")
    private LocalDateTime docenteConfirmoRealizacionEn;

    @Column(name = "total_confirmados", nullable = false)
    private Integer totalConfirmados;
}
