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
public class DetallePedidoClienteId implements Serializable {

    @Column(name = "id_pedidocli")
    private Long idPedidoCli;

    @Column(name = "id_productocli")
    private Long idProductoCli;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DetallePedidoClienteId)) return false;
        DetallePedidoClienteId that = (DetallePedidoClienteId) o;
        return idPedidoCli.equals(that.idPedidoCli) && idProductoCli.equals(that.idProductoCli);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}