package adfdev.erp.demo.services;

import adfdev.erp.demo.*;
import adfdev.erp.demo.PedidoCliente.EstadoPedido;
import adfdev.erp.demo.interfaces.PedidoClienteRepository;
import adfdev.erp.demo.interfaces.ProductoClienterepository;
import adfdev.erp.demo.interfaces.ProductoProveedorrepository;
import adfdev.erp.demo.interfaces.Escandallorepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import adfdev.erp.demo.services.Escandalloservice;


@Service
@Transactional
public class PedidoClienteService {

    @Autowired
    private PedidoClienteRepository pedidoClienteRepository;

    @Autowired
    private ProductoClienterepository productoClienteRepository;

    @Autowired
    private ProductoProveedorrepository productoProveedorRepository;

    @Autowired
    private Escandallorepository escandalloRepository;

    @Autowired
    private Escandalloservice escandalloService;

    /**
     * Crear un nuevo pedido de cliente
     */
    public PedidoCliente crearPedidoCliente(PedidoCliente pedido, List<Map<String, Object>> productosSeleccionados) {
        if (pedido.getFechaPedido() == null) {
            pedido.setFechaPedido(LocalDate.now());
        }
        if (pedido.getEstado() == null) {
            pedido.setEstado(EstadoPedido.EN_ESPERA);
        }

        for (Map<String, Object> productoData : productosSeleccionados) {
            Long idProducto = Long.valueOf(productoData.get("idProducto").toString());
            Integer cantidad = Integer.valueOf(productoData.get("cantidad").toString());

            ProductoCliente producto = productoClienteRepository.findById(idProducto)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + idProducto));

            // Ya NO verificar stock de producto cliente
            // Solo verificar stock de materias primas
            verificarStockMateriasPrimas(idProducto, cantidad);

            DetallePedidoCliente detalle = new DetallePedidoCliente(pedido, producto, cantidad);
            pedido.addDetalle(detalle);
        }

        pedido.calcularTotal();
        PedidoCliente pedidoGuardado = pedidoClienteRepository.save(pedido);

        for (DetallePedidoCliente detalle : pedidoGuardado.getDetalles()) {
            ProductoCliente producto = detalle.getProductoCliente();
            int cantidad = detalle.getCantidad();

            descontarMateriasPrimas(producto.getId(), cantidad);

            // Recalcular stock fabricable
            producto.setStock(escandalloService.calcularStockFabricable(producto.getId()));
            productoClienteRepository.save(producto);
        }

        return pedidoGuardado;
    }

    /**
     * Actualizar pedido existente
     */
    public PedidoCliente actualizarPedidoCliente(Long id, PedidoCliente pedidoActualizado) {
        return pedidoClienteRepository.findById(id)
                .map(pedido -> {
                    pedido.setDireccion(pedidoActualizado.getDireccion());
                    pedido.setEstado(pedidoActualizado.getEstado());
                    pedido.setDescuento(pedidoActualizado.getDescuento());
                    pedido.setIdCliente(pedidoActualizado.getIdCliente());
                    pedido.setIdTrabajador(pedidoActualizado.getIdTrabajador());
                    pedido.setIdAlmacen(pedidoActualizado.getIdAlmacen());
                    pedido.setIdMetodoEnvio(pedidoActualizado.getIdMetodoEnvio());

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

        // Devolver stock de producto cliente Y materias primas
        // En eliminarPedidoCliente, DESPUÉS de devolver materias primas:
        for (DetallePedidoCliente detalle : pedido.getDetalles()) {
            ProductoCliente producto = detalle.getProductoCliente();
            int cantidad = detalle.getCantidad();

            // Ya NO hacer: producto.setStock(producto.getStock() + cantidad);

            devolverMateriasPrimas(producto.getId(), cantidad);

            // Recalcular stock fabricable
            producto.setStock(escandalloService.calcularStockFabricable(producto.getId()));
            productoClienteRepository.save(producto);
        }

        pedidoClienteRepository.deleteById(id);
    }

    // ==================== LÓGICA DE ESCANDALLO ====================

    /**
     * Verificar que hay stock suficiente de materias primas para fabricar X unidades
     */
    private void verificarStockMateriasPrimas(Long idProductoCli, int cantidadPedida) {
        List<Escandallo> lineas = escandalloRepository.findByProductoClienteId(idProductoCli);

        for (Escandallo linea : lineas) {
            ProductoProveedor materiaPrima = linea.getProductoProveedor();
            BigDecimal cantidadNecesaria = linea.getCantidadNecesaria()
                    .multiply(BigDecimal.valueOf(cantidadPedida));

            // Comparar con stock disponible
            if (BigDecimal.valueOf(materiaPrima.getStock()).compareTo(cantidadNecesaria) < 0) {
                throw new RuntimeException(
                        "Stock insuficiente de materia prima '" + materiaPrima.getNombre() +
                                "'. Necesario: " + cantidadNecesaria.setScale(2, RoundingMode.HALF_UP) +
                                ", Disponible: " + materiaPrima.getStock());
            }
        }
    }

    /**
     * Descontar stock de materias primas según escandallo
     */
    private void descontarMateriasPrimas(Long idProductoCli, int cantidadPedida) {
        List<Escandallo> lineas = escandalloRepository.findByProductoClienteId(idProductoCli);

        for (Escandallo linea : lineas) {
            ProductoProveedor materiaPrima = linea.getProductoProveedor();
            BigDecimal cantidadDescontar = linea.getCantidadNecesaria()
                    .multiply(BigDecimal.valueOf(cantidadPedida));

            int nuevoStock = BigDecimal.valueOf(materiaPrima.getStock())
                    .subtract(cantidadDescontar)
                    .intValue();

            materiaPrima.setStock(Math.max(nuevoStock, 0));
            productoProveedorRepository.save(materiaPrima);
        }
    }

    /**
     * Devolver stock de materias primas (al eliminar/actualizar pedido)
     */
    private void devolverMateriasPrimas(Long idProductoCli, int cantidadDevuelta) {
        List<Escandallo> lineas = escandalloRepository.findByProductoClienteId(idProductoCli);

        for (Escandallo linea : lineas) {
            ProductoProveedor materiaPrima = linea.getProductoProveedor();
            BigDecimal cantidadDevolver = linea.getCantidadNecesaria()
                    .multiply(BigDecimal.valueOf(cantidadDevuelta));

            int nuevoStock = BigDecimal.valueOf(materiaPrima.getStock())
                    .add(cantidadDevolver)
                    .intValue();

            materiaPrima.setStock(nuevoStock);
            productoProveedorRepository.save(materiaPrima);
        }
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

        BigDecimal totalVentas = pedidoClienteRepository.calcularTotalVentas(EstadoPedido.ENTREGADO);
        estadisticas.put("totalVentas", totalVentas != null ? totalVentas : BigDecimal.ZERO);

        return estadisticas;
    }
}