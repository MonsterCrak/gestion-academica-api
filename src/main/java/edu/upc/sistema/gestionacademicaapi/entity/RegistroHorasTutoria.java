package edu.upc.sistema.gestionacademicaapi.entity;

import edu.upc.sistema.gestionacademicaapi.enums.CerradoPor;
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
@Table(name = "registro_horas_tutoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroHorasTutoria {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "docente_id", nullable = false)
    private Usuario docente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "materia_id", nullable = false)
    private Materia materia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private SolicitudTutoria solicitud;

    @Column(name = "fecha_hora_inicio_real", nullable = false)
    private LocalDateTime fechaHoraInicioReal;

    @Column(name = "fecha_hora_fin_real", nullable = false)
    private LocalDateTime fechaHoraFinReal;

    @Column(name = "horas_efectivas", nullable = false, precision = 4, scale = 2)
    private BigDecimal horasEfectivas;

    @Column(name = "horas_rectificadas", precision = 4, scale = 2)
    private BigDecimal horasRectificadas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rectificado_por_id")
    private Usuario rectificadoPor;

    @Column(name = "rectificado_en")
    private LocalDateTime rectificadoEn;

    @Column(name = "rectificado_motivo", columnDefinition = "TEXT")
    private String rectificadoMotivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "cerrado_por", nullable = false, length = 20)
    private CerradoPor cerradoPor;

    @Column(name = "cerrado_en", nullable = false)
    private LocalDateTime cerradoEn;

    @Column(name = "anulado", nullable = false)
    private Boolean anulado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anulado_por_id")
    private Usuario anuladoPor;

    @Column(name = "anulado_en")
    private LocalDateTime anuladoEn;

    @Column(name = "anulado_motivo", columnDefinition = "TEXT")
    private String anuladoMotivo;
}
