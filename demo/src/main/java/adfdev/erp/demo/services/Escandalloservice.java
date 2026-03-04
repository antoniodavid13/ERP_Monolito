package adfdev.erp.demo.services;

import adfdev.erp.demo.*;
import adfdev.erp.demo.interfaces.Escandallorepository;
import adfdev.erp.demo.interfaces.ProductoClienterepository;
import adfdev.erp.demo.interfaces.ProductoProveedorrepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional
public class Escandalloservice {

    @Autowired
    private Escandallorepository escandalloRepository;

    @Autowired
    private ProductoClienterepository productoClienteRepository;

    @Autowired
    private ProductoProveedorrepository productoProveedorRepository;

    /**
     * Obtener líneas de escandallo de un producto cliente
     */
    @Transactional(readOnly = true)
    public List<Escandallo> obtenerPorProductoCliente(Long idProductoCli) {
        return escandalloRepository.findByProductoClienteId(idProductoCli);
    }

    /**
     * Añadir una línea de escandallo
     */
    public Escandallo agregarLinea(Long idProductoCli, Long idProductoProv, BigDecimal cantidadNecesaria) {
        ProductoCliente pc = productoClienteRepository.findById(idProductoCli)
                .orElseThrow(() -> new RuntimeException("Producto cliente no encontrado con id: " + idProductoCli));

        ProductoProveedor pp = productoProveedorRepository.findById(idProductoProv)
                .orElseThrow(() -> new RuntimeException("Producto proveedor no encontrado con id: " + idProductoProv));

        Escandallo escandallo = new Escandallo();
        escandallo.setProductoCliente(pc);
        escandallo.setProductoProveedor(pp);
        escandallo.setCantidadNecesaria(cantidadNecesaria);

        Escandallo guardado = escandalloRepository.save(escandallo);

        // Recalcular coste y precio del producto cliente
        recalcularCosteProductoCliente(idProductoCli);

        return guardado;
    }

    /**
     * Eliminar una línea de escandallo
     */
    public void eliminarLinea(Long idProductoCli, Long idProductoProv) {
        EscandalloId id = new EscandalloId();
        id.setProductoCliente(idProductoCli);
        id.setProductoProveedor(idProductoProv);

        if (!escandalloRepository.existsById(id)) {
            throw new RuntimeException("Línea de escandallo no encontrada");
        }

        escandalloRepository.deleteById(id);
        recalcularCosteProductoCliente(idProductoCli);
    }

    /**
     * Actualizar cantidad de una línea
     */
    public void eliminarTodosPorProductoCliente(Long idProductoCli) {
        escandalloRepository.deleteByProductoClienteId(idProductoCli);
    }
    /**
     * Calcular cuántas unidades se pueden fabricar según stock de materias primas
     */
    public int calcularStockFabricable(Long idProductoCli) {
        List<Escandallo> lineas = escandalloRepository.findByProductoClienteId(idProductoCli);

        if (lineas.isEmpty()) {
            return 0;
        }

        int stockFabricable = Integer.MAX_VALUE;

        for (Escandallo linea : lineas) {
            BigDecimal stockMateria = BigDecimal.valueOf(linea.getProductoProveedor().getStock());
            BigDecimal cantidadNecesaria = linea.getCantidadNecesaria();

            if (cantidadNecesaria.compareTo(BigDecimal.ZERO) > 0) {
                int unidadesPosibles = stockMateria.divide(cantidadNecesaria, 0, RoundingMode.FLOOR).intValue();
                stockFabricable = Math.min(stockFabricable, unidadesPosibles);
            }
        }

        return stockFabricable == Integer.MAX_VALUE ? 0 : stockFabricable;
    }
    public Escandallo actualizarCantidad(Long idProductoCli, Long idProductoProv, BigDecimal nuevaCantidad) {
        EscandalloId id = new EscandalloId();
        id.setProductoCliente(idProductoCli);
        id.setProductoProveedor(idProductoProv);

        Escandallo escandallo = escandalloRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Línea de escandallo no encontrada"));

        escandallo.setCantidadNecesaria(nuevaCantidad);
        Escandallo guardado = escandalloRepository.save(escandallo);

        recalcularCosteProductoCliente(idProductoCli);

        return guardado;
    }

    /**
     * Recalcular coste_escandallo y precio_unitario del producto cliente
     */
    public void recalcularCosteProductoCliente(Long idProductoCli) {
        ProductoCliente producto = productoClienteRepository.findById(idProductoCli)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        BigDecimal coste = escandalloRepository.calcularCosteEscandallo(idProductoCli);
        if (coste == null) coste = BigDecimal.ZERO;

        producto.setCosteEscandallo(coste);

        BigDecimal margen = producto.getMargenBeneficio() != null ? producto.getMargenBeneficio() : BigDecimal.ZERO;
        BigDecimal precioVenta = coste.multiply(BigDecimal.ONE.add(margen.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)));
        producto.setPrecioUnitario(precioVenta);

        // Calcular stock fabricable automáticamente
        producto.setStock(calcularStockFabricable(idProductoCli));

        productoClienteRepository.save(producto);
    }

    /**
     * Recalcular todos los productos cliente (útil cuando cambia un precio de proveedor)
     */
    public void recalcularTodos() {
        List<ProductoCliente> todos = productoClienteRepository.findAll();
        for (ProductoCliente pc : todos) {
            recalcularCosteProductoCliente(pc.getId());
        }
    }
}