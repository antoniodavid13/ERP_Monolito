package adfdev.erp.demo.services;

import adfdev.erp.demo.DetallePedidoCliente;
import adfdev.erp.demo.PedidoCliente;
import adfdev.erp.demo.PedidoCliente.EstadoPedido;
import adfdev.erp.demo.ProductoCliente;
import adfdev.erp.demo.interfaces.PedidoClienteRepository;
import adfdev.erp.demo.interfaces.ProductoClienterepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class PedidoClienteService {

    @Autowired
    private PedidoClienteRepository pedidoClienteRepository;

    @Autowired
    private ProductoClienterepository productoClienteRepository;

    /**
     * Crear un nuevo pedido de cliente
     */
    public PedidoCliente crearPedidoCliente(PedidoCliente pedido, List<Map<String, Object>> productosSeleccionados) {
        // Establecer fecha si no existe
        if (pedido.getFechaPedido() == null) {
            pedido.setFechaPedido(LocalDate.now());
        }

        // Establecer estado por defecto
        if (pedido.getEstado() == null) {
            pedido.setEstado(EstadoPedido.EN_ESPERA);
        }

        // Agregar productos al pedido
        for (Map<String, Object> productoData : productosSeleccionados) {
            Long idProducto = Long.valueOf(productoData.get("idProducto").toString());
            Integer cantidad = Integer.valueOf(productoData.get("cantidad").toString());

            ProductoCliente producto = productoClienteRepository.findById(idProducto)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + idProducto));

            // Verificar stock disponible
            if (producto.getStock() < cantidad) {
                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre());
            }

            DetallePedidoCliente detalle = new DetallePedidoCliente(pedido, producto, cantidad);
            pedido.addDetalle(detalle);
        }

        // Calcular total del pedido
        pedido.calcularTotal();

        // Guardar pedido
        PedidoCliente pedidoGuardado = pedidoClienteRepository.save(pedido);

        // Restar stock de los productos (venta a cliente)
        for (DetallePedidoCliente detalle : pedidoGuardado.getDetalles()) {
            ProductoCliente producto = detalle.getProductoCliente();
            producto.setStock(producto.getStock() - detalle.getCantidad());
            productoClienteRepository.save(producto);
        }

        return pedidoGuardado;
    }

/**
 * Actualizar pedido existente
 */

    /**
     * Actualizar pedido existente
     */
    public PedidoCliente actualizarPedidoCliente(Long id, PedidoCliente pedidoActualizado,
                                                 List<Map<String, Object>> productosSeleccionados) {
        return pedidoClienteRepository.findById(id)
                .map(pedido -> {
                    // Actualizar campos básicos
                    pedido.setDireccion(pedidoActualizado.getDireccion());
                    pedido.setEstado(pedidoActualizado.getEstado());
                    pedido.setDescuento(pedidoActualizado.getDescuento());
                    pedido.setIdCliente(pedidoActualizado.getIdCliente());
                    pedido.setIdTrabajador(pedidoActualizado.getIdTrabajador());
                    pedido.setIdAlmacen(pedidoActualizado.getIdAlmacen());
                    pedido.setIdMetodoEnvio(pedidoActualizado.getIdMetodoEnvio());

                    // Devolver stock de los detalles antiguos (sumar)
                    for (DetallePedidoCliente detalleAntiguo : pedido.getDetalles()) {
                        ProductoCliente producto = detalleAntiguo.getProductoCliente();
                        producto.setStock(producto.getStock() + detalleAntiguo.getCantidad());
                        productoClienteRepository.save(producto);
                    }

                    // Limpiar detalles antiguos
                    pedido.getDetalles().clear();

                    // Agregar nuevos productos
                    for (Map<String, Object> productoData : productosSeleccionados) {
                        Long idProducto = Long.valueOf(productoData.get("idProducto").toString());
                        Integer cantidad = Integer.valueOf(productoData.get("cantidad").toString());

                        ProductoCliente producto = productoClienteRepository.findById(idProducto)
                                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + idProducto));

                        if (producto.getStock() < cantidad) {
                            throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre());
                        }

                        DetallePedidoCliente detalle = new DetallePedidoCliente(pedido, producto, cantidad);
                        pedido.addDetalle(detalle);
                    }

                    // Restar stock de los nuevos detalles
                    for (DetallePedidoCliente detalleNuevo : pedido.getDetalles()) {
                        ProductoCliente producto = detalleNuevo.getProductoCliente();
                        producto.setStock(producto.getStock() - detalleNuevo.getCantidad());
                        productoClienteRepository.save(producto);
                    }

                    // Recalcular total
                    pedido.calcularTotal();

                    return pedidoClienteRepository.save(pedido);
                })
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con id: " + id));
    }

    @Transactional(readOnly = true)
    public List<PedidoCliente> obtenerTodos() {
        return pedidoClienteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<PedidoCliente> obtenerPorId(Long id) {
        return pedidoClienteRepository.findById(id);
    }

    public void eliminarPedidoCliente(Long id) {
        PedidoCliente pedido = pedidoClienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con id: " + id));

        // Devolver stock de los productos al eliminar el pedido
        for (DetallePedidoCliente detalle : pedido.getDetalles()) {
            ProductoCliente producto = detalle.getProductoCliente();
            producto.setStock(producto.getStock() + detalle.getCantidad());
            productoClienteRepository.save(producto);
        }

        pedidoClienteRepository.deleteById(id);
    }

    // ==================== CONSULTAS ESPECIALES ====================

    @Transactional(readOnly = true)
    public Page<PedidoCliente> obtenerPedidosClientesPaginados(int pagina, int tamanio, String ordenarPor, String direccion) {
        Sort sort = direccion.equalsIgnoreCase("desc")
                ? Sort.by(ordenarPor).descending()
                : Sort.by(ordenarPor).ascending();
        Pageable pageable = PageRequest.of(pagina, tamanio, sort);
        return pedidoClienteRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<PedidoCliente> obtenerPorEstado(EstadoPedido estado) {
        return pedidoClienteRepository.findByEstado(estado);
    }

    public PedidoCliente cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        return pedidoClienteRepository.findById(id)
                .map(pedido -> {
                    pedido.setEstado(nuevoEstado);
                    return pedidoClienteRepository.save(pedido);
                })
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con id: " + id));
    }

    @Transactional(readOnly = true)
    public List<PedidoCliente> obtenerPedidosRecientes() {
        return pedidoClienteRepository.findTop10ByOrderByFechaPedidoDesc();
    }

    // ==================== ESTADÍSTICAS ====================

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> estadisticas = new HashMap<>();

        long total = pedidoClienteRepository.count();
        long entregados = pedidoClienteRepository.countByEstado(EstadoPedido.ENTREGADO);
        long enviados = pedidoClienteRepository.countByEstado(EstadoPedido.ENVIADO);
        long preparando = pedidoClienteRepository.countByEstado(EstadoPedido.PREPARANDOLO);
        long enEspera = pedidoClienteRepository.countByEstado(EstadoPedido.EN_ESPERA);

        estadisticas.put("total", total);
        estadisticas.put("entregados", entregados);
        estadisticas.put("enviados", enviados);
        estadisticas.put("preparando", preparando);
        estadisticas.put("enEspera", enEspera);

        // Siempre calcular porcentajes (0 si no hay total)
        if (total > 0) {
            estadisticas.put("porcentajeEntregados", Math.round((entregados * 100.0) / total));
            estadisticas.put("porcentajeEnviados", Math.round((enviados * 100.0) / total));
            estadisticas.put("porcentajePreparando", Math.round((preparando * 100.0) / total));
            estadisticas.put("porcentajeEnEspera", Math.round((enEspera * 100.0) / total));
        } else {
            estadisticas.put("porcentajeEntregados", 0L);
            estadisticas.put("porcentajeEnviados", 0L);
            estadisticas.put("porcentajePreparando", 0L);
            estadisticas.put("porcentajeEnEspera", 0L);
        }

        // Calcular total de ventas
        BigDecimal totalVentas = pedidoClienteRepository.calcularTotalVentas(EstadoPedido.ENTREGADO);
        estadisticas.put("totalVentas", totalVentas != null ? totalVentas : BigDecimal.ZERO);

        return estadisticas;
    }
}