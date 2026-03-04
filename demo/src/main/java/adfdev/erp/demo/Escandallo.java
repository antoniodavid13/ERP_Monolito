package adfdev.erp.demo;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "escandallo")
@IdClass(EscandalloId.class)
@Data
public class Escandallo {

    @Id
    @ManyToOne
    @JoinColumn(name = "id_productocli", referencedColumnName = "id_productocli")
    private ProductoCliente productoCliente;

    @Id
    @ManyToOne
    @JoinColumn(name = "id_productoprov", referencedColumnName = "id_productoprov")
    private ProductoProveedor productoProveedor;

    @Column(name = "cantidad_necesaria", precision = 10, scale = 3, nullable = false,
            columnDefinition = "DECIMAL(10,3) DEFAULT 0")
    private BigDecimal cantidadNecesaria;
}