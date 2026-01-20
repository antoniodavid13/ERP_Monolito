package adfdev.erp.demo;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "producto_proveedores")
@Data
public class ProductoProveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_productoprov")
    private Long id;

    @Column(name = "nombre", length = 50)
    private String nombre;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Column(name = "precio_lotes", precision = 5, scale = 2, columnDefinition = "DECIMAL(5,2)")
    private BigDecimal precioLotes;

    // Enum para estados del producto
    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoProducto estado;

    public enum EstadoProducto {
        ACTIVO, PENDIENTE, BAJA
    }
}