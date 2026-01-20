package adfdev.erp.demo.services;

import adfdev.erp.demo.DetallePedidoProveedor;
import adfdev.erp.demo.PedidoProveedor;
import adfdev.erp.demo.PedidoProveedor.EstadoPedido;
import adfdev.erp.demo.PedidoProveedor.Prioridad;
import adfdev.erp.demo.ProductoProveedor;
import adfdev.erp.demo.interfaces.PedidoProveedorRepository;
import adfdev.erp.demo.interfaces.ProductoProveedorrepository;
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
public class PedidoProveedorService {

    @Autowired
    private PedidoProveedorRepository pedidoProveedorRepository;

    @Autowired
    private ProductoProveedorrepository productoProveedorRepository;

    /**
     * Crear un nuevo pedido de proveedor
     */
    public PedidoProveedor crearPedidoProveedor(PedidoProveedor pedido, List<Map<String, Object>> productosSeleccionados) {
        // Establecer fecha si no existe
        if (pedido.getFechaPedido() == null) {
            pedido.setFechaPedido(LocalDate.now());
        }

        // Establecer estado por defecto
        if (pedido.getEstado() == null) {
            pedido.setEstado(EstadoPedido.EN_ESPERA);
        }

        // Establecer prioridad por defecto
        if (pedido.getPrioridad() == null) {
            pedido.setPrioridad(Prioridad.BAJA);
        }

        // Agregar productos al pedido
        for (Map<String, Object> productoData : productosSeleccionados) {
            Long idProducto = Long.valueOf(productoData.get("idProducto").toString());
            Integer cantidad = Integer.valueOf(productoData.get("cantidad").toString());

            ProductoProveedor producto = productoProveedorRepository.findById(idProducto)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + idProducto));

            DetallePedidoProveedor detalle = new DetallePedidoProveedor(pedido, producto, cantidad);
            pedido.addDetalle(detalle);
        }

        // Calcular total del pedido
        pedido.calcularTotal();

        // Guardar pedido
        return pedidoProveedorRepository.save(pedido);
    }

    /**
     * Actualizar pedido existente
     */
    public PedidoProveedor actualizarPedidoProveedor(Long id, PedidoProveedor pedidoActualizado,
                                                     List<Map<String, Object>> productosSeleccionados) {
        return pedidoProveedorRepository.findById(id)
                .map(pedido -> {
                    // Actualizar campos básicos
                    pedido.setDireccion(pedidoActualizado.getDireccion());
                    pedido.setEstado(pedidoActualizado.getEstado());
                    pedido.setPrioridad(pedidoActualizado.getPrioridad());
                    pedido.setComentarios(pedidoActualizado.getComentarios());
                    pedido.setIdProveedor(pedidoActualizado.getIdProveedor());
                    pedido.setIdTrabajador(pedidoActualizado.getIdTrabajador());
                    pedido.setIdAlmacen(pedidoActualizado.getIdAlmacen());
                    pedido.setIdMetodoEnvio(pedidoActualizado.getIdMetodoEnvio());

                    // Limpiar detalles antiguos
                    pedido.getDetalles().clear();

                    // Agregar nuevos productos
                    for (Map<String, Object> productoData : productosSeleccionados) {
                        Long idProducto = Long.valueOf(productoData.get("idProducto").toString());
                        Integer cantidad = Integer.valueOf(productoData.get("cantidad").toString());

                        ProductoProveedor producto = productoProveedorRepository.findById(idProducto)
                                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + idProducto));

                        DetallePedidoProveedor detalle = new DetallePedidoProveedor(pedido, producto, cantidad);
                        pedido.addDetalle(detalle);
                    }

                    // Recalcular total
                    pedido.calcularTotal();

                    return pedidoProveedorRepository.save(pedido);
                })
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con id: " + id));
    }

    @Transactional(readOnly = true)
    public List<PedidoProveedor> obtenerTodos() {
        return pedidoProveedorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<PedidoProveedor> obtenerPorId(Long id) {
        return pedidoProveedorRepository.findById(id);
    }

    public void eliminarPedidoProveedor(Long id) {
        if (!pedidoProveedorRepository.existsById(id)) {
            throw new RuntimeException("Pedido no encontrado con id: " + id);
        }
        pedidoProveedorRepository.deleteById(id);
    }

    // ==================== CONSULTAS ESPECIALES ====================

    @Transactional(readOnly = true)
    public Page<PedidoProveedor> obtenerPedidosProveedoresPaginados(int pagina, int tamanio, String ordenarPor, String direccion) {
        Sort sort = direccion.equalsIgnoreCase("desc")
                ? Sort.by(ordenarPor).descending()
                : Sort.by(ordenarPor).ascending();
        Pageable pageable = PageRequest.of(pagina, tamanio, sort);
        return pedidoProveedorRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<PedidoProveedor> obtenerPorEstado(EstadoPedido estado) {
        return pedidoProveedorRepository.findByEstado(estado);
    }

    @Transactional(readOnly = true)
    public List<PedidoProveedor> obtenerPorPrioridad(Prioridad prioridad) {
        return pedidoProveedorRepository.findByPrioridad(prioridad);
    }

    public PedidoProveedor cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        return pedidoProveedorRepository.findById(id)
                .map(pedido -> {
                    pedido.setEstado(nuevoEstado);
                    return pedidoProveedorRepository.save(pedido);
                })
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con id: " + id));
    }

    @Transactional(readOnly = true)
    public List<PedidoProveedor> obtenerPedidosRecientes() {
        return pedidoProveedorRepository.findTop10ByOrderByFechaPedidoDesc();
    }

    @Transactional(readOnly = true)
    public List<PedidoProveedor> obtenerPedidosUrgentes() {
        return pedidoProveedorRepository.obtenerPedidosUrgentes();
    }

    // ==================== ESTADÍSTICAS ====================

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> estadisticas = new HashMap<>();

        long total = pedidoProveedorRepository.count();
        long entregados = pedidoProveedorRepository.countByEstado(EstadoPedido.ENTREGADO);
        long enviados = pedidoProveedorRepository.countByEstado(EstadoPedido.ENVIADO);
        long preparando = pedidoProveedorRepository.countByEstado(EstadoPedido.PREPARANDOLO);
        long enEspera = pedidoProveedorRepository.countByEstado(EstadoPedido.EN_ESPERA);

        long prioridadAlta = pedidoProveedorRepository.countByPrioridad(Prioridad.ALTA);
        long prioridadBaja = pedidoProveedorRepository.countByPrioridad(Prioridad.BAJA);

        estadisticas.put("total", total);
        estadisticas.put("entregados", entregados);
        estadisticas.put("enviados", enviados);
        estadisticas.put("preparando", preparando);
        estadisticas.put("enEspera", enEspera);
        estadisticas.put("prioridadAlta", prioridadAlta);
        estadisticas.put("prioridadBaja", prioridadBaja);

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

        // Calcular total de compras
        BigDecimal totalCompras = pedidoProveedorRepository.calcularTotalCompras(EstadoPedido.ENTREGADO);
        estadisticas.put("totalCompras", totalCompras != null ? totalCompras : BigDecimal.ZERO);

        return estadisticas;
    }
}