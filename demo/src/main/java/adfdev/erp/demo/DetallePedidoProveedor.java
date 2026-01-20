package adfdev.erp.demo;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "detalles_pedidosprov")
@Data
public class DetallePedidoProveedor {

    @EmbeddedId
    private DetallePedidoProveedorId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idPedidoProv")
    @JoinColumn(name = "id_pedidoprov")
    private PedidoProveedor pedidoProveedor;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("idProductoProv")
    @JoinColumn(name = "id_productoprov")
    private ProductoProveedor productoProveedor;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "total_linea", precision = 10, scale = 3)
    private BigDecimal totalLinea;

    // Constructor vacío
    public DetallePedidoProveedor() {
        this.id = new DetallePedidoProveedorId();
    }

    // Constructor con parámetros
    public DetallePedidoProveedor(PedidoProveedor pedidoProveedor, ProductoProveedor productoProveedor, Integer cantidad) {
        this.id = new DetallePedidoProveedorId(pedidoProveedor.getId(), productoProveedor.getId());
        this.pedidoProveedor = pedidoProveedor;
        this.productoProveedor = productoProveedor;
        this.cantidad = cantidad;
        calcularTotalLinea();
    }

    // Calcular total de la línea
    public void calcularTotalLinea() {
        if (productoProveedor != null && cantidad != null) {
            this.totalLinea = productoProveedor.getPrecioLotes().multiply(BigDecimal.valueOf(cantidad));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DetallePedidoProveedor)) return false;
        return id != null && id.equals(((DetallePedidoProveedor) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}