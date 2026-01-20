package adfdev.erp.demo.interfaces;

import adfdev.erp.demo.ProductoProveedor;
import adfdev.erp.demo.ProductoProveedor.EstadoProducto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoProveedorrepository extends JpaRepository<ProductoProveedor, Long> {

    Optional<ProductoProveedor> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    List<ProductoProveedor> findByEstado(EstadoProducto estado);

    Page<ProductoProveedor> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    long countByEstado(EstadoProducto estado);

    // Top productos por precio de lotes
    List<ProductoProveedor> findTop5ByOrderByPrecioLotesDesc();

    // Productos con stock bajo
    List<ProductoProveedor> findByStockLessThan(Integer stock);

    // Productos por rango de precio
    List<ProductoProveedor> findByPrecioLotesBetween(BigDecimal precioMin, BigDecimal precioMax);

    // Búsqueda por nombre
    @Query("SELECT p FROM ProductoProveedor p WHERE " +
            "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    Page<ProductoProveedor> buscarPorNombre(@Param("busqueda") String busqueda, Pageable pageable);

    @Query("SELECT p.estado, COUNT(p) FROM ProductoProveedor p GROUP BY p.estado")
    List<Object[]> contarPorEstado();

    // Calcular stock total
    @Query("SELECT SUM(p.stock) FROM ProductoProveedor p WHERE p.estado = :estado")
    Long calcularStockTotal(@Param("estado") EstadoProducto estado);

    // Calcular valor total de inventario
    @Query("SELECT SUM(p.stock * p.precioLotes) FROM ProductoProveedor p WHERE p.estado = :estado")
    BigDecimal calcularValorInventario(@Param("estado") EstadoProducto estado);
}