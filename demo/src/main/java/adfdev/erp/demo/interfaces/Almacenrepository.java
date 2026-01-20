package adfdev.erp.demo.interfaces;

import adfdev.erp.demo.Almacen;
import adfdev.erp.demo.Almacen.EstadoAlmacen;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Almacenrepository extends JpaRepository<Almacen, Long> {

    Optional<Almacen> findByDireccion(String direccion);

    boolean existsByDireccion(String direccion);

    List<Almacen> findByEstado(EstadoAlmacen estado);

    Page<Almacen> findByDireccionContainingIgnoreCase(String direccion, Pageable pageable);

    long countByEstado(EstadoAlmacen estado);

    // Top almacenes por capacidad
    List<Almacen> findTop5ByOrderByCapacidadDesc();

    // Almacenes con capacidad mayor a un valor
    List<Almacen> findByCapacidadGreaterThan(Integer capacidad);

    // Búsqueda por dirección
    @Query("SELECT a FROM Almacen a WHERE " +
            "LOWER(a.direccion) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    Page<Almacen> buscarPorDireccion(@Param("busqueda") String busqueda, Pageable pageable);

    @Query("SELECT a.estado, COUNT(a) FROM Almacen a GROUP BY a.estado")
    List<Object[]> contarPorEstado();

    // Calcular capacidad total
    @Query("SELECT SUM(a.capacidad) FROM Almacen a WHERE a.estado = :estado")
    Long calcularCapacidadTotal(@Param("estado") EstadoAlmacen estado);
}