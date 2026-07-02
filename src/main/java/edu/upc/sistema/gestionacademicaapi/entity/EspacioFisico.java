package edu.upc.sistema.gestionacademicaapi.entity;

import edu.upc.sistema.gestionacademicaapi.enums.TipoEspacio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "espacio_fisico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EspacioFisico {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "codigo", nullable = false, unique = true, length = 30)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_espacio", nullable = false, length = 20)
    private TipoEspacio tipoEspacio;

    @Column(name = "aforo", nullable = false)
    private Integer aforo;

    @Column(name = "permitir_prestamo_individual", nullable = false)
    private Boolean permitirPrestamoIndividual;

    @Column(name = "permitir_reserva_completa", nullable = false)
    private Boolean permitirReservaCompleta;
}
