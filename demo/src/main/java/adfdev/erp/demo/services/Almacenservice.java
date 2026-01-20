package adfdev.erp.demo.services;

import adfdev.erp.demo.Almacen;
import adfdev.erp.demo.Almacen.EstadoAlmacen;
import adfdev.erp.demo.interfaces.Almacenrepository;
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
public class Almacenservice {

    @Autowired
    private Almacenrepository almacenRepository;

    /**
     * Crear un nuevo almacén
     */
    public Almacen crearAlmacen(Almacen almacen) {
        if (almacenRepository.existsByDireccion(almacen.getDireccion())) {
            throw new RuntimeException("Ya existe un almacén en la dirección: " + almacen.getDireccion());
        }

        return almacenRepository.save(almacen);
    }

    @Transactional(readOnly = true)
    public List<Almacen> obtenerTodos() {
        return almacenRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Almacen> obtenerPorId(Long id) {
        return almacenRepository.findById(id);
    }

    /**
     * Actualizar almacén existente
     */
    public Almacen actualizarAlmacen(Long id, Almacen almacenActualizado) {
        return almacenRepository.findById(id)
                .map(almacen -> {
                    if (!almacen.getDireccion().equals(almacenActualizado.getDireccion()) &&
                            almacenRepository.existsByDireccion(almacenActualizado.getDireccion())) {
                        throw new RuntimeException("Ya existe un almacén en la dirección: " + almacenActualizado.getDireccion());
                    }

                    almacen.setDireccion(almacenActualizado.getDireccion());
                    almacen.setCapacidad(almacenActualizado.getCapacidad());
                    almacen.setEstado(almacenActualizado.getEstado());

                    return almacenRepository.save(almacen);
                })
                .orElseThrow(() -> new RuntimeException("Almacén no encontrado con id: " + id));
    }

    public void eliminarAlmacen(Long id) {
        if (!almacenRepository.existsById(id)) {
            throw new RuntimeException("Almacén no encontrado con id: " + id);
        }
        almacenRepository.deleteById(id);
    }

    // ==================== CONSULTAS ESPECIALES ====================

    @Transactional(readOnly = true)
    public Page<Almacen> obtenerAlmacenesPaginados(int pagina, int tamanio, String ordenarPor, String direccion) {
        Sort sort = direccion.equalsIgnoreCase("desc")
                ? Sort.by(ordenarPor).descending()
                : Sort.by(ordenarPor).ascending();
        Pageable pageable = PageRequest.of(pagina, tamanio, sort);
        return almacenRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Almacen> buscarAlmacenes(String busqueda, int pagina, int tamanio) {
        Pageable pageable = PageRequest.of(pagina, tamanio, Sort.by("direccion").ascending());
        return almacenRepository.buscarPorDireccion(busqueda, pageable);
    }

    @Transactional(readOnly = true)
    public List<Almacen> obtenerPorEstado(EstadoAlmacen estado) {
        return almacenRepository.findByEstado(estado);
    }

    public Almacen cambiarEstado(Long id, EstadoAlmacen nuevoEstado) {
        return almacenRepository.findById(id)
                .map(almacen -> {
                    almacen.setEstado(nuevoEstado);
                    return almacenRepository.save(almacen);
                })
                .orElseThrow(() -> new RuntimeException("Almacén no encontrado con id: " + id));
    }

    // ==================== ESTADÍSTICAS ====================

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> estadisticas = new HashMap<>();

        long total = almacenRepository.count();
        long activos = almacenRepository.countByEstado(EstadoAlmacen.ACTIVO);
        long pendientes = almacenRepository.countByEstado(EstadoAlmacen.PENDIENTE);
        long bajas = almacenRepository.countByEstado(EstadoAlmacen.BAJA);

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

        // Calcular capacidad total
        Long capacidadTotal = almacenRepository.calcularCapacidadTotal(EstadoAlmacen.ACTIVO);
        estadisticas.put("capacidadTotal", capacidadTotal != null ? capacidadTotal : 0);

        return estadisticas;
    }

    @Transactional(readOnly = true)
    public List<Almacen> obtenerTopAlmacenes() {
        return almacenRepository.findTop5ByOrderByCapacidadDesc();
    }

    @Transactional(readOnly = true)
    public List<Almacen> obtenerPorCapacidadMayorA(Integer capacidad) {
        return almacenRepository.findByCapacidadGreaterThan(capacidad);
    }
}