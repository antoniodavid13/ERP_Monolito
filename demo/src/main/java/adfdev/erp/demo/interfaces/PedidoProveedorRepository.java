package adfdev.erp.demo.interfaces;

import adfdev.erp.demo.PedidoProveedor;
import adfdev.erp.demo.PedidoProveedor.EstadoPedido;
import adfdev.erp.demo.PedidoProveedor.Prioridad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PedidoProveedorRepository extends JpaRepository<PedidoProveedor, Long> {

    // Buscar por estado
    List<PedidoProveedor> findByEstado(EstadoPedido estado);

    // Contar por estado
    long countByEstado(EstadoPedido estado);

    // Buscar por prioridad
    List<PedidoProveedor> findByPrioridad(Prioridad prioridad);

    // Contar por prioridad
    long countByPrioridad(Prioridad prioridad);

    // Buscar por proveedor
    List<PedidoProveedor> findByIdProveedor(Long idProveedor);
    Page<PedidoProveedor> findByIdProveedor(Long idProveedor, Pageable pageable);

    // Buscar por trabajador
    List<PedidoProveedor> findByIdTrabajador(Long idTrabajador);

    // Buscar por almacén
    List<PedidoProveedor> findByIdAlmacen(Long idAlmacen);

    // Buscar por rango de fechas
    List<PedidoProveedor> findByFechaPedidoBetween(LocalDate fechaInicio, LocalDate fechaFin);

    // Buscar pedidos recientes
    List<PedidoProveedor> findTop10ByOrderByFechaPedidoDesc();

    // Búsqueda por dirección
    @Query("SELECT p FROM PedidoProveedor p WHERE LOWER(p.direccion) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    Page<PedidoProveedor> buscarPorDireccion(@Param("busqueda") String busqueda, Pageable pageable);

    // Búsqueda por comentarios
    @Query("SELECT p FROM PedidoProveedor p WHERE LOWER(p.comentarios) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    Page<PedidoProveedor> buscarPorComentarios(@Param("busqueda") String busqueda, Pageable pageable);

    // Calcular total de compras
    @Query("SELECT SUM(p.total) FROM PedidoProveedor p WHERE p.estado = :estado")
    BigDecimal calcularTotalCompras(@Param("estado") EstadoPedido estado);

    // Calcular total de compras por rango de fechas
    @Query("SELECT SUM(p.total) FROM PedidoProveedor p WHERE p.fechaPedido BETWEEN :fechaInicio AND :fechaFin")
    BigDecimal calcularTotalComprasPorFecha(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);

    // Pedidos por estado y prioridad
    List<PedidoProveedor> findByEstadoAndPrioridad(EstadoPedido estado, Prioridad prioridad);

    // Pedidos urgentes pendientes
    @Query("SELECT p FROM PedidoProveedor p WHERE p.prioridad = 'ALTA' AND p.estado IN ('EN_ESPERA', 'PREPARANDOLO')")
    List<PedidoProveedor> obtenerPedidosUrgentes();
}