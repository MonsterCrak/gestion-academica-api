package edu.upc.sistema.gestionacademicaapi.entity;

import edu.upc.sistema.gestionacademicaapi.enums.EstadoRecurso;
import edu.upc.sistema.gestionacademicaapi.enums.TipoMovilidad;
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

@Entity
@Table(name = "recurso")
@Getter
@Setter
@NoArgsConstructor
public class Recurso {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaPolitica categoria;

    @Column(name = "numero_serie", length = 80)
    private String numeroSerie;

    @Column(name = "codigo_inventario", nullable = false, unique = true, length = 60)
    private String codigoInventario;

    @Column(name = "nombre", nullable = false, length = 160)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movilidad", nullable = false, length = 20)
    private TipoMovilidad tipoMovilidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "espacio_actual_id")
    private EspacioFisico espacioActual;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoRecurso estado;

    @Column(name = "requiere_ubicacion_fisica", nullable = false)
    private Boolean requiereUbicacionFisica;
}
