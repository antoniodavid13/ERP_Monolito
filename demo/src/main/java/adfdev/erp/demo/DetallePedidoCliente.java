package adfdev.erp.demo;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "detalles_pedidoscli")
@Data
public class DetallePedidoCliente {

    @EmbeddedId
    private DetallePedidoClienteId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idPedidoCli")
    @JoinColumn(name = "id_pedidocli")
    private PedidoCliente pedidoCliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("idProductoCli")
    @JoinColumn(name = "id_productocli")
    private ProductoCliente productoCliente;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "total_linea", precision = 10, scale = 3)
    private BigDecimal totalLinea;

    // Constructor vacío
    public DetallePedidoCliente() {
        this.id = new DetallePedidoClienteId();
    }

    // Constructor con parámetros
    public DetallePedidoCliente(PedidoCliente pedidoCliente, ProductoCliente productoCliente, Integer cantidad) {
        this.id = new DetallePedidoClienteId(pedidoCliente.getId(), productoCliente.getId());
        this.pedidoCliente = pedidoCliente;
        this.productoCliente = productoCliente;
        this.cantidad = cantidad;
        calcularTotalLinea();
    }

    // Calcular total de la línea
    public void calcularTotalLinea() {
        if (productoCliente != null && cantidad != null) {
            BigDecimal precioBase = productoCliente.getPrecioUnitario().multiply(BigDecimal.valueOf(cantidad));

            if (productoCliente.getDescuento() != null && productoCliente.getDescuento() > 0) {
                BigDecimal descuentoDecimal = BigDecimal.valueOf(productoCliente.getDescuento()).divide(BigDecimal.valueOf(100));
                BigDecimal montoDescuento = precioBase.multiply(descuentoDecimal);
                this.totalLinea = precioBase.subtract(montoDescuento);
            } else {
                this.totalLinea = precioBase;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DetallePedidoCliente)) return false;
        return id != null && id.equals(((DetallePedidoCliente) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}