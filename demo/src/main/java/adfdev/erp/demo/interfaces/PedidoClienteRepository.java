package adfdev.erp.demo.interfaces;

import adfdev.erp.demo.PedidoCliente;
import adfdev.erp.demo.PedidoCliente.EstadoPedido;
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
public interface PedidoClienteRepository extends JpaRepository<PedidoCliente, Long> {

    // Buscar por estado
    List<PedidoCliente> findByEstado(EstadoPedido estado);

    // Contar por estado
    long countByEstado(EstadoPedido estado);

    // Buscar por cliente
    List<PedidoCliente> findByIdCliente(Long idCliente);
    Page<PedidoCliente> findByIdCliente(Long idCliente, Pageable pageable);

    // Buscar por trabajador
    List<PedidoCliente> findByIdTrabajador(Long idTrabajador);

    // Buscar por almacén
    List<PedidoCliente> findByIdAlmacen(Long idAlmacen);

    // Buscar por rango de fechas
    List<PedidoCliente> findByFechaPedidoBetween(LocalDate fechaInicio, LocalDate fechaFin);

    // Buscar pedidos recientes
    List<PedidoCliente> findTop10ByOrderByFechaPedidoDesc();

    // Búsqueda por dirección
    @Query("SELECT p FROM PedidoCliente p WHERE LOWER(p.direccion) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    Page<PedidoCliente> buscarPorDireccion(@Param("busqueda") String busqueda, Pageable pageable);

    // Calcular total de ventas
    @Query("SELECT SUM(p.total) FROM PedidoCliente p WHERE p.estado = :estado")
    BigDecimal calcularTotalVentas(@Param("estado") EstadoPedido estado);

    // Calcular total de ventas por rango de fechas
    @Query("SELECT SUM(p.total) FROM PedidoCliente p WHERE p.fechaPedido BETWEEN :fechaInicio AND :fechaFin")
    BigDecimal calcularTotalVentasPorFecha(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);

    // Pedidos por estado y fecha
    List<PedidoCliente> findByEstadoAndFechaPedidoBetween(EstadoPedido estado, LocalDate fechaInicio, LocalDate fechaFin);

    // Pedidos con descuento
    List<PedidoCliente> findByDescuentoGreaterThan(Integer descuento);
}