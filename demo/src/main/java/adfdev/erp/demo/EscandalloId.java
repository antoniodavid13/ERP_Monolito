package adfdev.erp.demo;

import lombok.Data;
import java.io.Serializable;

@Data
public class EscandalloId implements Serializable {
    private Long productoCliente;
    private Long productoProveedor;
}