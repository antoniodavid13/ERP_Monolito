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

    @Column(name = "precio_unitario", precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2)")
    private BigDecimal precioUnitario;

    @Column(name = "descuento")
    private Integer descuento;

    @Column(name = "coste_escandallo", precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2) DEFAULT 0")
    private BigDecimal costeEscandallo;

    @Column(name = "margen_beneficio", precision = 5, scale = 2, columnDefinition = "DECIMAL(5,2) DEFAULT 0")
    private BigDecimal margenBeneficio;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoProducto estado;

    public enum EstadoProducto {
        ACTIVO, PENDIENTE, BAJA
    }
}