package adfdev.erp.demo.services;

import adfdev.erp.demo.Departamento;
import adfdev.erp.demo.Departamento.EstadoDepartamento;
import adfdev.erp.demo.interfaces.Departamentorepository;
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
public class Departamentoservice {

    @Autowired
    private Departamentorepository departamentoRepository;

    /**
     * Crear un nuevo departamento
     */
    public Departamento crearDepartamento(Departamento departamento) {
        if (departamentoRepository.existsByNombre(departamento.getNombre())) {
            throw new RuntimeException("Ya existe un departamento con el nombre: " + departamento.getNombre());
        }

        return departamentoRepository.save(departamento);
    }

    @Transactional(readOnly = true)
    public List<Departamento> obtenerTodos() {
        return departamentoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Departamento> obtenerPorId(Long id) {
        return departamentoRepository.findById(id);
    }

    /**
     * Actualizar departamento existente
     */
    public Departamento actualizarDepartamento(Long id, Departamento departamentoActualizado) {
        return departamentoRepository.findById(id)
                .map(departamento -> {
                    if (!departamento.getNombre().equals(departamentoActualizado.getNombre()) &&
                            departamentoRepository.existsByNombre(departamentoActualizado.getNombre())) {
                        throw new RuntimeException("Ya existe un departamento con el nombre: " + departamentoActualizado.getNombre());
                    }

                    departamento.setNombre(departamentoActualizado.getNombre());
                    departamento.setDireccion(departamentoActualizado.getDireccion());
                    departamento.setIdTipoDepartamento(departamentoActualizado.getIdTipoDepartamento());
                    departamento.setEstado(departamentoActualizado.getEstado());

                    return departamentoRepository.save(departamento);
                })
                .orElseThrow(() -> new RuntimeException("Departamento no encontrado con id: " + id));
    }

    public void eliminarDepartamento(Long id) {
        if (!departamentoRepository.existsById(id)) {
            throw new RuntimeException("Departamento no encontrado con id: " + id);
        }
        departamentoRepository.deleteById(id);
    }

    // ==================== CONSULTAS ESPECIALES ====================

    @Transactional(readOnly = true)
    public Page<Departamento> obtenerDepartamentosPaginados(int pagina, int tamanio, String ordenarPor, String direccion) {
        Sort sort = direccion.equalsIgnoreCase("desc")
                ? Sort.by(ordenarPor).descending()
                : Sort.by(ordenarPor).ascending();
        Pageable pageable = PageRequest.of(pagina, tamanio, sort);
        return departamentoRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Departamento> buscarDepartamentos(String busqueda, int pagina, int tamanio) {
        Pageable pageable = PageRequest.of(pagina, tamanio, Sort.by("nombre").ascending());
        return departamentoRepository.buscarPorNombreODireccion(busqueda, pageable);
    }

    @Transactional(readOnly = true)
    public List<Departamento> obtenerPorEstado(EstadoDepartamento estado) {
        return departamentoRepository.findByEstado(estado);
    }

    public Departamento cambiarEstado(Long id, EstadoDepartamento nuevoEstado) {
        return departamentoRepository.findById(id)
                .map(departamento -> {
                    departamento.setEstado(nuevoEstado);
                    return departamentoRepository.save(departamento);
                })
                .orElseThrow(() -> new RuntimeException("Departamento no encontrado con id: " + id));
    }

    // ==================== ESTADÍSTICAS ====================

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> estadisticas = new HashMap<>();

        long total = departamentoRepository.count();
        long activos = departamentoRepository.countByEstado(EstadoDepartamento.ACTIVO);
        long pendientes = departamentoRepository.countByEstado(EstadoDepartamento.PENDIENTE);
        long bajas = departamentoRepository.countByEstado(EstadoDepartamento.BAJA);

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
    public List<Departamento> obtenerTopDepartamentos() {
        return departamentoRepository.findTop5ByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<Departamento> obtenerPorTipoDepartamento(Integer idTipoDepartamento) {
        return departamentoRepository.findByIdTipoDepartamento(idTipoDepartamento);
    }

    @Transactional(readOnly = true)
    public List<Departamento> obtenerPorDireccion(String direccion) {
        return departamentoRepository.findByDireccion(direccion);
    }
}