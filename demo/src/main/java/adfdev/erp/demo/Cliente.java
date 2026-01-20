package adfdev.erp.demo;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "clientes")
@Data
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long id;

    @Column(name = "limite_credito", nullable = false) // Añadimos nullable=false
    private Double credito;

    @Column(name = "fecha_registro")
    private LocalDate fechaRegistro;

    @Column(name = "nombre", length = 50)
    private String nombre;

    @Column(name = "correo", length = 50)
    private String correo;

    @Column(name = "telefono", length = 9)
    private String telefono;

    @Column(name = "ciudad", length = 50)
    private String ciudad;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoCliente estado;

    public enum EstadoCliente {
        ACTIVO, PENDIENTE, BAJA
    }
}