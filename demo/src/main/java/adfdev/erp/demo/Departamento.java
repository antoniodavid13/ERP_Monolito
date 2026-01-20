package adfdev.erp.demo;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "departamentos")
@Data
public class Departamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_departamento")
    private Long id;

    @Column(name = "nombre", length = 50)
    private String nombre;

    @Column(name = "direccion", length = 50)
    private String direccion;

    @Column(name = "id_tipodepartamento")
    private Integer idTipoDepartamento;

    // Enum para estados del departamento
    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoDepartamento estado;

    public enum EstadoDepartamento {
        ACTIVO, PENDIENTE, BAJA
    }
}