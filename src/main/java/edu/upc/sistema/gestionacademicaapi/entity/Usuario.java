package edu.upc.sistema.gestionacademicaapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.upc.sistema.gestionacademicaapi.enums.EstadoUsuario;
import edu.upc.sistema.gestionacademicaapi.enums.TipoUsuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_usuario", nullable = false, length = 20)
    private TipoUsuario tipoUsuario;

    @Column(name = "identificador_corporativo", nullable = false, unique = true, length = 80)
    private String identificadorCorporativo;

    @Column(name = "email", unique = true, length = 160)
    private String email;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "apellidos", nullable = false, length = 160)
    private String apellidos;

    @Column(name = "carrera_id")
    private Long carreraId;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    /** Estado operativo (HU-19/20). ACTIVO por defecto; BLOQUEADO ante deuda o penalizacion. */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoUsuario estado;

    /** RN-01: deuda financiera pendiente que bloquea prestamos y reservas. */
    @Column(name = "tiene_deuda", nullable = false)
    private Boolean tieneDeuda;

    /** Fin de la penalizacion temporal vigente (RN-09), null si no aplica. */
    @Column(name = "penalizado_hasta")
    private LocalDateTime penalizadoHasta;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    /** Contador de intentos fallidos de login para bloqueo por seguridad (HU-01). */
    @Column(name = "intentos_fallidos", nullable = false)
    private Integer intentosFallidos;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Version
    @Column(name = "version")
    private Long version;

    @JsonIgnore
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * True si el usuario no puede realizar operaciones por deuda o penalizacion vigente (RN-01/RN-09).
     * Se calcula sobre {@code tieneDeuda} y {@code penalizadoHasta} (fuente de verdad); el campo
     * {@code estado} es solo una vista denormalizada que sincroniza el servicio de penalizaciones.
     */
    public boolean isBloqueadoParaOperar() {
        boolean penalizado = penalizadoHasta != null && penalizadoHasta.isAfter(LocalDateTime.now());
        return Boolean.TRUE.equals(tieneDeuda) || penalizado;
    }

    @PrePersist
    void prePersist() {
        if (activo == null) activo = true;
        if (estado == null) estado = EstadoUsuario.ACTIVO;
        if (tieneDeuda == null) tieneDeuda = false;
        if (intentosFallidos == null) intentosFallidos = 0;
        if (fechaRegistro == null) fechaRegistro = LocalDateTime.now();
    }
}
