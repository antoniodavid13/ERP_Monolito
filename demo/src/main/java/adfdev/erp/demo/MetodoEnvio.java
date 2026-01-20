package adfdev.erp.demo;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "metodo_envio")
@Data
public class MetodoEnvio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_metodoenvio")
    private Long id;

    @Column(name = "nombre", length = 50)
    private String nombre;
}