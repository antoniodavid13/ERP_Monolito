package adfdev.erp.demo;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "proveedores")
@Data
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proveedor")
    private Long id;

    @Column(name = "metodo_envio", length = 50)
    private String metodoEnvio;

    @Column(name = "telefono", length = 9)
    private String telefono;

    @Column(name = "nombre", length = 50)
    private String nombre;

    @Column(name = "correo", length = 50)
    private String correo;

    @Column(name = "ciudad", length = 50)
    private String ciudad;

    @Column(name = "id_tipoproveedor")
    private Integer idTipoProveedor;

    // Enum para estados del proveedor (similar a clientes)
    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoProveedor estado;

    public enum EstadoProveedor {
        ACTIVO, PENDIENTE, BAJA
    }
}