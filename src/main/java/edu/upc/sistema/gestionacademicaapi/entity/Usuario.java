package edu.upc.sistema.gestionacademicaapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.upc.sistema.gestionacademicaapi.enums.TipoUsuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "apellidos", nullable = false, length = 160)
    private String apellidos;

    @Column(name = "carrera_id")
    private Long carreraId;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @JsonIgnore
    public String getPasswordHash() {
        return passwordHash;
    }
}
