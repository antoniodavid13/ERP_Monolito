package adfdev.erp.demo;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "producto_cliente")
@Data
public class ProductoCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_productocli")
    private Long id;

    @Column(name = "nombre", length = 50)
    private String nombre;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Column(name = "precio_unitario", precision = 4, scale = 2, columnDefinition = "DECIMAL(4,2)")
    private BigDecimal precioUnitario;

    @Column(name = "descuento")
    private Integer descuento;

    // Enum para estados del producto
    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoProducto estado;

    public enum EstadoProducto {
        ACTIVO, PENDIENTE, BAJA
    }
}