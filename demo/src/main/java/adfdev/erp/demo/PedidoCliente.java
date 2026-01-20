package adfdev.erp.demo;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos_clientes")
@Data
public class PedidoCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedidocli")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoPedido estado;

    @Column(name = "direccion", length = 50)
    private String direccion;

    @Column(name = "total", precision = 8, scale = 2)
    private BigDecimal total;

    @Column(name = "fecha_pedido")
    private LocalDate fechaPedido;

    @Column(name = "descuento")
    private Integer descuento;

    @Column(name = "id_cliente", nullable = false)
    private Long idCliente;

    @Column(name = "id_trabajador", nullable = false)
    private Long idTrabajador;

    @Column(name = "id_almacen", nullable = false)
    private Long idAlmacen;

    @Column(name = "id_metodoenvio", nullable = false)
    private Long idMetodoEnvio;

    // Relación con detalles del pedido
    @OneToMany(mappedBy = "pedidoCliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedidoCliente> detalles = new ArrayList<>();

    // Enum para estados del pedido
    public enum EstadoPedido {
        ENTREGADO("Entregado"),
        ENVIADO("Enviado"),
        PREPARANDOLO("Preparandolo"),
        EN_ESPERA("En espera");

        private final String displayName;

        EstadoPedido(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    // Métodos helper para manejar detalles
    public void addDetalle(DetallePedidoCliente detalle) {
        detalles.add(detalle);
        detalle.setPedidoCliente(this);
    }

    public void removeDetalle(DetallePedidoCliente detalle) {
        detalles.remove(detalle);
        detalle.setPedidoCliente(null);
    }

    // Calcular total del pedido
    public void calcularTotal() {
        BigDecimal subtotal = detalles.stream()
                .map(DetallePedidoCliente::getTotalLinea)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (descuento != null && descuento > 0) {
            BigDecimal descuentoDecimal = BigDecimal.valueOf(descuento).divide(BigDecimal.valueOf(100));
            BigDecimal montoDescuento = subtotal.multiply(descuentoDecimal);
            this.total = subtotal.subtract(montoDescuento);
        } else {
            this.total = subtotal;
        }
    }
}