package edu.upc.sistema.gestionacademicaapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categoria_politica")
@Getter
@Setter
@NoArgsConstructor
public class CategoriaPolitica {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "nombre_categoria", nullable = false, length = 80)
    private String nombreCategoria;

    @Column(name = "max_items_por_alumno", nullable = false)
    private Integer maxItemsPorAlumno;

    @Column(name = "tiempo_maximo_horas", nullable = false)
    private Integer tiempoMaximoHoras;
}
