package adfdev.erp.demo.interfaces;

import adfdev.erp.demo.Proveedor;
import adfdev.erp.demo.Proveedor.EstadoProveedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Proveedorrepository extends JpaRepository<Proveedor, Long> {

    Optional<Proveedor> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    List<Proveedor> findByEstado(EstadoProveedor estado);

    Page<Proveedor> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    long countByEstado(EstadoProveedor estado);

    // Top proveedores por tipo o alfabéticamente
    List<Proveedor> findTop5ByOrderByNombreAsc();

    // Proveedores por método de envío
    List<Proveedor> findByMetodoEnvio(String metodoEnvio);

    // Búsqueda por nombre, correo o ciudad
    @Query("SELECT p FROM Proveedor p WHERE " +
            "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            "LOWER(p.correo) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            "LOWER(p.ciudad) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    Page<Proveedor> buscarPorNombreCorreoOCiudad(@Param("busqueda") String busqueda, Pageable pageable);

    @Query("SELECT p.estado, COUNT(p) FROM Proveedor p GROUP BY p.estado")
    List<Object[]> contarPorEstado();

    // Contar proveedores por tipo
    long countByIdTipoProveedor(Integer idTipoProveedor);
}