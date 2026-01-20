package adfdev.erp.demo.services;

import adfdev.erp.demo.Proveedor;
import adfdev.erp.demo.Proveedor.EstadoProveedor;
import adfdev.erp.demo.interfaces.Proveedorrepository;
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
public class Proveedorservice {

    @Autowired
    private Proveedorrepository proveedorRepository;

    /**
     * Crear un nuevo proveedor
     */
    public Proveedor crearProveedor(Proveedor proveedor) {
        if (proveedorRepository.existsByCorreo(proveedor.getCorreo())) {
            throw new RuntimeException("Ya existe un proveedor con el correo: " + proveedor.getCorreo());
        }

        return proveedorRepository.save(proveedor);
    }

    @Transactional(readOnly = true)
    public List<Proveedor> obtenerTodos() {
        return proveedorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Proveedor> obtenerPorId(Long id) {
        return proveedorRepository.findById(id);
    }

    /**
     * Actualizar proveedor existente
     */
    public Proveedor actualizarProveedor(Long id, Proveedor proveedorActualizado) {
        return proveedorRepository.findById(id)
                .map(proveedor -> {
                    if (!proveedor.getCorreo().equals(proveedorActualizado.getCorreo()) &&
                            proveedorRepository.existsByCorreo(proveedorActualizado.getCorreo())) {
                        throw new RuntimeException("Ya existe un proveedor con el correo: " + proveedorActualizado.getCorreo());
                    }

                    proveedor.setNombre(proveedorActualizado.getNombre());
                    proveedor.setCorreo(proveedorActualizado.getCorreo());
                    proveedor.setTelefono(proveedorActualizado.getTelefono());
                    proveedor.setCiudad(proveedorActualizado.getCiudad());
                    proveedor.setMetodoEnvio(proveedorActualizado.getMetodoEnvio());
                    proveedor.setIdTipoProveedor(proveedorActualizado.getIdTipoProveedor());
                    proveedor.setEstado(proveedorActualizado.getEstado());

                    return proveedorRepository.save(proveedor);
                })
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con id: " + id));
    }

    public void eliminarProveedor(Long id) {
        if (!proveedorRepository.existsById(id)) {
            throw new RuntimeException("Proveedor no encontrado con id: " + id);
        }
        proveedorRepository.deleteById(id);
    }

    // ==================== CONSULTAS ESPECIALES ====================

    @Transactional(readOnly = true)
    public Page<Proveedor> obtenerProveedoresPaginados(int pagina, int tamanio, String ordenarPor, String direccion) {
        Sort sort = direccion.equalsIgnoreCase("desc")
                ? Sort.by(ordenarPor).descending()
                : Sort.by(ordenarPor).ascending();
        Pageable pageable = PageRequest.of(pagina, tamanio, sort);
        return proveedorRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Proveedor> buscarProveedores(String busqueda, int pagina, int tamanio) {
        Pageable pageable = PageRequest.of(pagina, tamanio, Sort.by("nombre").ascending());
        return proveedorRepository.buscarPorNombreCorreoOCiudad(busqueda, pageable);
    }

    @Transactional(readOnly = true)
    public List<Proveedor> obtenerPorEstado(EstadoProveedor estado) {
        return proveedorRepository.findByEstado(estado);
    }

    public Proveedor cambiarEstado(Long id, EstadoProveedor nuevoEstado) {
        return proveedorRepository.findById(id)
                .map(proveedor -> {
                    proveedor.setEstado(nuevoEstado);
                    return proveedorRepository.save(proveedor);
                })
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con id: " + id));
    }

    // ==================== ESTADÍSTICAS ====================

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> estadisticas = new HashMap<>();

        long total = proveedorRepository.count();
        long activos = proveedorRepository.countByEstado(EstadoProveedor.ACTIVO);
        long pendientes = proveedorRepository.countByEstado(EstadoProveedor.PENDIENTE);
        long bajas = proveedorRepository.countByEstado(EstadoProveedor.BAJA);

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

        return estadisticas;
    }

    @Transactional(readOnly = true)
    public List<Proveedor> obtenerTopProveedores() {
        return proveedorRepository.findTop5ByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<Proveedor> obtenerPorMetodoEnvio(String metodoEnvio) {
        return proveedorRepository.findByMetodoEnvio(metodoEnvio);
    }
}