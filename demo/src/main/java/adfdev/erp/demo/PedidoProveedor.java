package adfdev.erp.demo;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos_proveedores")
@Data
public class PedidoProveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedidoprov")
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

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad")
    private Prioridad prioridad;

    @Column(name = "comentarios", length = 50)
    private String comentarios;

    @Column(name = "id_proveedor", nullable = false)
    private Long idProveedor;

    @Column(name = "id_trabajador", nullable = false)
    private Long idTrabajador;

    @Column(name = "id_almacen", nullable = false)
    private Long idAlmacen;

    @Column(name = "id_metodoenvio", nullable = false)
    private Long idMetodoEnvio;

    // Relación con detalles del pedido
    @OneToMany(mappedBy = "pedidoProveedor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedidoProveedor> detalles = new ArrayList<>();

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

    // Enum para prioridad
    public enum Prioridad {
        ALTA("Alta"),
        BAJA("Baja");

        private final String displayName;

        Prioridad(String displayName) {
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
    public void addDetalle(DetallePedidoProveedor detalle) {
        detalles.add(detalle);
        detalle.setPedidoProveedor(this);
    }

    public void removeDetalle(DetallePedidoProveedor detalle) {
        detalles.remove(detalle);
        detalle.setPedidoProveedor(null);
    }

    // Calcular total del pedido
    public void calcularTotal() {
        this.total = detalles.stream()
                .map(DetallePedidoProveedor::getTotalLinea)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}