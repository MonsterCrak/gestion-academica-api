package edu.upc.sistema.gestionacademicaapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categoria_politica")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaPolitica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_categoria", nullable = false, length = 80)
    private String nombreCategoria;

    @Column(name = "max_items_por_alumno", nullable = false)
    private Integer maxItemsPorAlumno;

    @Column(name = "tiempo_maximo_horas", nullable = false)
    private Integer tiempoMaximoHoras;

    /** Si la categoria permite que el estudiante extienda la fecha fin del prestamo. */
    @Column(name = "permite_extension", nullable = false)
    private Boolean permiteExtension;

    /** Horas que se suman a fechaFin al extender (requerido cuando permiteExtension=true). */
    @Column(name = "horas_extension")
    private Integer horasExtension;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @PrePersist
    void prePersist() {
        if (activo == null) activo = true;
        if (permiteExtension == null) permiteExtension = false;
    }
}
