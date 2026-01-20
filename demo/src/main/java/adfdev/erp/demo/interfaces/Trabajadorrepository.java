package adfdev.erp.demo.interfaces;

import adfdev.erp.demo.Trabajador;
import adfdev.erp.demo.Trabajador.EstadoTrabajador;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Trabajadorrepository extends JpaRepository<Trabajador, Long> {

    Optional<Trabajador> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    List<Trabajador> findByEstado(EstadoTrabajador estado);

    Page<Trabajador> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    long countByEstado(EstadoTrabajador estado);

    // Top trabajadores por salario
    List<Trabajador> findTop5ByOrderBySalarioDesc();

    // Trabajadores por puesto
    List<Trabajador> findByPuesto(String puesto);

    // Trabajadores por departamento
    List<Trabajador> findByDepartamentoId(Long departamentoId);

    // Búsqueda por nombre, correo, puesto o ciudad
    @Query("SELECT t FROM Trabajador t WHERE " +
            "LOWER(t.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            "LOWER(t.correo) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            "LOWER(t.puesto) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            "LOWER(t.ciudad) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    Page<Trabajador> buscarPorNombreCorreoPuestoOCiudad(@Param("busqueda") String busqueda, Pageable pageable);

    @Query("SELECT t.estado, COUNT(t) FROM Trabajador t GROUP BY t.estado")
    List<Object[]> contarPorEstado();

    // Contar trabajadores por departamento
    long countByDepartamentoId(Long departamentoId);
}