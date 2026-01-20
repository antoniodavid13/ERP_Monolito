package adfdev.erp.demo;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "almacenes")
@Data
public class Almacen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_almacen")
    private Long id;

    @Column(name = "direccion", length = 50)
    private String direccion;

    @Column(name = "capacidad", nullable = false)
    private Integer capacidad;

    // Enum para estados del almacén
    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoAlmacen estado;

    public enum EstadoAlmacen {
        ACTIVO, PENDIENTE, BAJA
    }
}