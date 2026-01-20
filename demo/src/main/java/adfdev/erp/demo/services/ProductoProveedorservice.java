package adfdev.erp.demo.services;

import adfdev.erp.demo.ProductoProveedor;
import adfdev.erp.demo.ProductoProveedor.EstadoProducto;
import adfdev.erp.demo.interfaces.ProductoProveedorrepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ProductoProveedorservice {

    @Autowired
    private ProductoProveedorrepository productoProveedorRepository;

    /**
     * Crear un nuevo producto de proveedor
     */
    public ProductoProveedor crearProductoProveedor(ProductoProveedor productoProveedor) {
        if (productoProveedorRepository.existsByNombre(productoProveedor.getNombre())) {
            throw new RuntimeException("Ya existe un producto con el nombre: " + productoProveedor.getNombre());
        }

        return productoProveedorRepository.save(productoProveedor);
    }

    @Transactional(readOnly = true)
    public List<ProductoProveedor> obtenerTodos() {
        return productoProveedorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<ProductoProveedor> obtenerPorId(Long id) {
        return productoProveedorRepository.findById(id);
    }

    /**
     * Actualizar producto existente
     */
    public ProductoProveedor actualizarProductoProveedor(Long id, ProductoProveedor productoActualizado) {
        return productoProveedorRepository.findById(id)
                .map(producto -> {
                    if (!producto.getNombre().equals(productoActualizado.getNombre()) &&
                            productoProveedorRepository.existsByNombre(productoActualizado.getNombre())) {
                        throw new RuntimeException("Ya existe un producto con el nombre: " + productoActualizado.getNombre());
                    }

                    producto.setNombre(productoActualizado.getNombre());
                    producto.setStock(productoActualizado.getStock());
                    producto.setPrecioLotes(productoActualizado.getPrecioLotes());
                    producto.setEstado(productoActualizado.getEstado());

                    return productoProveedorRepository.save(producto);
                })
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));
    }

    public void eliminarProductoProveedor(Long id) {
        if (!productoProveedorRepository.existsById(id)) {
            throw new RuntimeException("Producto no encontrado con id: " + id);
        }
        productoProveedorRepository.deleteById(id);
    }

    // ==================== CONSULTAS ESPECIALES ====================

    @Transactional(readOnly = true)
    public Page<ProductoProveedor> obtenerProductosProveedoresPaginados(int pagina, int tamanio, String ordenarPor, String direccion) {
        Sort sort = direccion.equalsIgnoreCase("desc")
                ? Sort.by(ordenarPor).descending()
                : Sort.by(ordenarPor).ascending();
        Pageable pageable = PageRequest.of(pagina, tamanio, sort);
        return productoProveedorRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<ProductoProveedor> buscarProductosProveedores(String busqueda, int pagina, int tamanio) {
        Pageable pageable = PageRequest.of(pagina, tamanio, Sort.by("nombre").ascending());
        return productoProveedorRepository.buscarPorNombre(busqueda, pageable);
    }

    @Transactional(readOnly = true)
    public List<ProductoProveedor> obtenerPorEstado(EstadoProducto estado) {
        return productoProveedorRepository.findByEstado(estado);
    }

    public ProductoProveedor cambiarEstado(Long id, EstadoProducto nuevoEstado) {
        return productoProveedorRepository.findById(id)
                .map(producto -> {
                    producto.setEstado(nuevoEstado);
                    return productoProveedorRepository.save(producto);
                })
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));
    }

    // ==================== ESTADÍSTICAS ====================

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> estadisticas = new HashMap<>();

        long total = productoProveedorRepository.count();
        long activos = productoProveedorRepository.countByEstado(EstadoProducto.ACTIVO);
        long pendientes = productoProveedorRepository.countByEstado(EstadoProducto.PENDIENTE);
        long bajas = productoProveedorRepository.countByEstado(EstadoProducto.BAJA);

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
        Long stockTotal = productoProveedorRepository.calcularStockTotal(EstadoProducto.ACTIVO);
        estadisticas.put("stockTotal", stockTotal != null ? stockTotal : 0);

        // Calcular valor total del inventario
        BigDecimal valorInventario = productoProveedorRepository.calcularValorInventario(EstadoProducto.ACTIVO);
        estadisticas.put("valorInventario", valorInventario != null ? valorInventario : BigDecimal.ZERO);

        return estadisticas;
    }

    @Transactional(readOnly = true)
    public List<ProductoProveedor> obtenerTopProductos() {
        return productoProveedorRepository.findTop5ByOrderByPrecioLotesDesc();
    }

    @Transactional(readOnly = true)
    public List<ProductoProveedor> obtenerStockBajo(Integer stockMinimo) {
        return productoProveedorRepository.findByStockLessThan(stockMinimo);
    }

    @Transactional(readOnly = true)
    public List<ProductoProveedor> obtenerPorRangoPrecio(BigDecimal precioMin, BigDecimal precioMax) {
        return productoProveedorRepository.findByPrecioLotesBetween(precioMin, precioMax);
    }
}