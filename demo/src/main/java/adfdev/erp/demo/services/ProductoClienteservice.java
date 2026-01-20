package adfdev.erp.demo.services;

import adfdev.erp.demo.ProductoCliente;
import adfdev.erp.demo.ProductoCliente.EstadoProducto;
import adfdev.erp.demo.interfaces.ProductoClienterepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ProductoClienteservice {

    @Autowired
    private ProductoClienterepository productoClienteRepository;

    /**
     * Crear un nuevo producto de cliente
     */
    public ProductoCliente crearProductoCliente(ProductoCliente productoCliente) {
        if (productoClienteRepository.existsByNombre(productoCliente.getNombre())) {
            throw new RuntimeException("Ya existe un producto con el nombre: " + productoCliente.getNombre());
        }

        return productoClienteRepository.save(productoCliente);
    }

    @Transactional(readOnly = true)
    public List<ProductoCliente> obtenerTodos() {
        return productoClienteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<ProductoCliente> obtenerPorId(Long id) {
        return productoClienteRepository.findById(id);
    }

    /**
     * Actualizar producto existente
     */
    public ProductoCliente actualizarProductoCliente(Long id, ProductoCliente productoActualizado) {
        return productoClienteRepository.findById(id)
                .map(producto -> {
                    if (!producto.getNombre().equals(productoActualizado.getNombre()) &&
                            productoClienteRepository.existsByNombre(productoActualizado.getNombre())) {
                        throw new RuntimeException("Ya existe un producto con el nombre: " + productoActualizado.getNombre());
                    }

                    producto.setNombre(productoActualizado.getNombre());
                    producto.setStock(productoActualizado.getStock());
                    producto.setPrecioUnitario(productoActualizado.getPrecioUnitario());
                    producto.setDescuento(productoActualizado.getDescuento());
                    producto.setEstado(productoActualizado.getEstado());

                    return productoClienteRepository.save(producto);
                })
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));
    }

    public void eliminarProductoCliente(Long id) {
        if (!productoClienteRepository.existsById(id)) {
            throw new RuntimeException("Producto no encontrado con id: " + id);
        }
        productoClienteRepository.deleteById(id);
    }

    // ==================== CONSULTAS ESPECIALES ====================

    @Transactional(readOnly = true)
    public Page<ProductoCliente> obtenerProductosClientesPaginados(int pagina, int tamanio, String ordenarPor, String direccion) {
        Sort sort = direccion.equalsIgnoreCase("desc")
                ? Sort.by(ordenarPor).descending()
                : Sort.by(ordenarPor).ascending();
        Pageable pageable = PageRequest.of(pagina, tamanio, sort);
        return productoClienteRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<ProductoCliente> buscarProductosClientes(String busqueda, int pagina, int tamanio) {
        Pageable pageable = PageRequest.of(pagina, tamanio, Sort.by("nombre").ascending());
        return productoClienteRepository.buscarPorNombre(busqueda, pageable);
    }

    @Transactional(readOnly = true)
    public List<ProductoCliente> obtenerPorEstado(EstadoProducto estado) {
        return productoClienteRepository.findByEstado(estado);
    }

    public ProductoCliente cambiarEstado(Long id, EstadoProducto nuevoEstado) {
        return productoClienteRepository.findById(id)
                .map(producto -> {
                    producto.setEstado(nuevoEstado);
                    return productoClienteRepository.save(producto);
                })
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));
    }

    // ==================== ESTADÍSTICAS ====================

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> estadisticas = new HashMap<>();

        long total = productoClienteRepository.count();
        long activos = productoClienteRepository.countByEstado(EstadoProducto.ACTIVO);
        long pendientes = productoClienteRepository.countByEstado(EstadoProducto.PENDIENTE);
        long bajas = productoClienteRepository.countByEstado(EstadoProducto.BAJA);

        estadisticas.put("total", total);
        estadisticas.put("activos", activos);
        estadisticas.put("pendientes", pendientes);
        estadisticas.put("bajas", bajas);

        if (total > 0) {
            estadisticas.put("porcentajeActivos", Math.round((activos * 100.0) / total));
            estadisticas.put("porcentajePendientes", Math.round((pendientes * 100.0) / total));
            estadisticas.put("porcentajeBajas", Math.round((bajas * 100.0) / total));
        } else {
            estadisticas.put("porcentajeActivos", 0);
            estadisticas.put("porcentajePendientes", 0);
            estadisticas.put("porcentajeBajas", 0);
        }

        // Calcular stock total
        Long stockTotal = productoClienteRepository.calcularStockTotal(EstadoProducto.ACTIVO);
        estadisticas.put("stockTotal", stockTotal != null ? stockTotal : 0);

        return estadisticas;
    }

    @Transactional(readOnly = true)
    public List<ProductoCliente> obtenerTopProductos() {
        return productoClienteRepository.findTop5ByOrderByPrecioUnitarioDesc();
    }

    @Transactional(readOnly = true)
    public List<ProductoCliente> obtenerConDescuento(Integer descuentoMinimo) {
        return productoClienteRepository.findByDescuentoGreaterThan(descuentoMinimo);
    }

    @Transactional(readOnly = true)
    public List<ProductoCliente> obtenerStockBajo(Integer stockMinimo) {
        return productoClienteRepository.findByStockLessThan(stockMinimo);
    }
}