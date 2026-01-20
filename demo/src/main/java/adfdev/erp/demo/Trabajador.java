package adfdev.erp.demo;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "trabajadores")
@Data
public class Trabajador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_trabajador")
    private Long id;

    @Column(name = "salario")
    private Double salario;

    @Column(name = "puesto", length = 50)
    private String puesto;

    @Column(name = "telefono", length = 9)
    private String telefono;

    @Column(name = "nombre", length = 50)
    private String nombre;

    @Column(name = "correo", length = 50)
    private String correo;

    @Column(name = "ciudad", length = 50)
    private String ciudad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_departamento")
    private Departamento departamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoTrabajador estado;

    // Métodos auxiliares para trabajar con el ID del departamento
    @Transient
    public Integer getIdDepartamento() {
        return departamento != null && departamento.getId() != null
                ? departamento.getId().intValue()
                : null;
    }

    @Transient
    public void setIdDepartamento(Integer idDepartamento) {
        if (idDepartamento != null) {
            if (this.departamento == null) {
                this.departamento = new Departamento();
            }
            this.departamento.setId(idDepartamento.longValue());
        } else {
            this.departamento = null;
        }
    }

    public enum EstadoTrabajador {
        ACTIVO, PENDIENTE, BAJA
    }
}