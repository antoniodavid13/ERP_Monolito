package adfdev.erp.demo.interfaces;

import adfdev.erp.demo.ProductoCliente;
import adfdev.erp.demo.ProductoCliente.EstadoProducto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoClienterepository extends JpaRepository<ProductoCliente, Long> {

    Optional<ProductoCliente> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    List<ProductoCliente> findByEstado(EstadoProducto estado);

    Page<ProductoCliente> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    long countByEstado(EstadoProducto estado);

    // Top productos por precio
    List<ProductoCliente> findTop5ByOrderByPrecioUnitarioDesc();

    // Productos por descuento
    List<ProductoCliente> findByDescuentoGreaterThan(Integer descuento);

    // Productos con stock bajo
    List<ProductoCliente> findByStockLessThan(Integer stock);

    // Búsqueda por nombre
    @Query("SELECT p FROM ProductoCliente p WHERE " +
            "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    Page<ProductoCliente> buscarPorNombre(@Param("busqueda") String busqueda, Pageable pageable);

    @Query("SELECT p.estado, COUNT(p) FROM ProductoCliente p GROUP BY p.estado")
    List<Object[]> contarPorEstado();

    // Calcular stock total
    @Query("SELECT SUM(p.stock) FROM ProductoCliente p WHERE p.estado = :estado")
    Long calcularStockTotal(@Param("estado") EstadoProducto estado);
}