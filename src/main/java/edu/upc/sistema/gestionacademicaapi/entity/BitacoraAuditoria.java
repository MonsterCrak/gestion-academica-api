package edu.upc.sistema.gestionacademicaapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Bitacora de auditoria inalterable (HU-22, RN-10, Anexo D).
 * Registro append-only: sin @Setter y sin endpoints de modificacion/borrado.
 */
@Entity
@Table(name = "bitacora_auditoria", indexes = {
        @Index(name = "idx_bitacora_timestamp", columnList = "timestamp"),
        @Index(name = "idx_bitacora_accion", columnList = "accion")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BitacoraAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "usuario_identificador", length = 80)
    private String usuarioIdentificador;

    @Column(name = "accion", nullable = false, length = 60)
    private String accion;

    @Column(name = "entidad", length = 60)
    private String entidad;

    @Column(name = "entidad_id", length = 60)
    private String entidadId;

    @Column(name = "resultado", length = 20)
    private String resultado;

    @Column(name = "ip", length = 60)
    private String ip;

    @Column(name = "user_agent", length = 300)
    private String userAgent;

    @Column(name = "detalle", length = 500)
    private String detalle;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
}
