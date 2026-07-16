package edu.upc.sistema.gestionacademicaapi.entity;

import edu.upc.sistema.gestionacademicaapi.enums.ModoResolucion;
import edu.upc.sistema.gestionacademicaapi.enums.OrigenPenalizacion;
import edu.upc.sistema.gestionacademicaapi.enums.TipoPenalizacion;
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
 * Penalizacion temporal aplicada a un usuario (HU-20, RN-09).
 */
@Entity
@Table(name = "penalizacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Penalizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoPenalizacion tipo;

    /** Módulo que originó la penalización (préstamo, reserva, tutoría o general). */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "origen", nullable = false, length = 20)
    private OrigenPenalizacion origen = OrigenPenalizacion.GENERAL;

    @Column(name = "motivo", nullable = false, length = 300)
    private String motivo;

    /** Cómo resolver/levantar la penalización (monto en pensión, descuento a docente o solo suspensión). */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "modo_resolucion", nullable = false, length = 25)
    private ModoResolucion modoResolucion = ModoResolucion.SUSPENSION_RECURSOS;

    /** Monto a pagar/descontar, cuando la resolución es económica. */
    @Column(name = "monto", precision = 10, scale = 2)
    private java.math.BigDecimal monto;

    /** Detalle de instrucciones para que el usuario resuelva la penalización. */
    @Column(name = "instrucciones_resolucion", length = 500)
    private String instruccionesResolucion;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Column(name = "activa", nullable = false)
    private Boolean activa;

    /** Prestamo que origino la penalizacion, si aplica (HU-12/HU-20). */
    @Column(name = "prestamo_id")
    private Long prestamoId;

    @Version
    @Column(name = "version")
    private Long version;
}
