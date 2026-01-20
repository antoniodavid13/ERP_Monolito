package adfdev.erp.demo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetallePedidoProveedorId implements Serializable {

    @Column(name = "id_pedidoprov")
    private Long idPedidoProv;

    @Column(name = "id_productoprov")
    private Long idProductoProv;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DetallePedidoProveedorId)) return false;
        DetallePedidoProveedorId that = (DetallePedidoProveedorId) o;
        return idPedidoProv.equals(that.idPedidoProv) && idProductoProv.equals(that.idProductoProv);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}