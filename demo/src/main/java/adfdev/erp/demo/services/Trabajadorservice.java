package adfdev.erp.demo.services;

import adfdev.erp.demo.Trabajador;
import adfdev.erp.demo.Trabajador.EstadoTrabajador;
import adfdev.erp.demo.interfaces.Trabajadorrepository;
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
public class Trabajadorservice {

    @Autowired
    private Trabajadorrepository trabajadorRepository;

    /**
     * Crear un nuevo trabajador
     */
    public Trabajador crearTrabajador(Trabajador trabajador) {
        if (trabajadorRepository.existsByCorreo(trabajador.getCorreo())) {
            throw new RuntimeException("Ya existe un trabajador con el correo: " + trabajador.getCorreo());
        }

        return trabajadorRepository.save(trabajador);
    }

    @Transactional(readOnly = true)
    public List<Trabajador> obtenerTodos() {
        return trabajadorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Trabajador> obtenerPorId(Long id) {
        return trabajadorRepository.findById(id);
    }

    /**
     * Actualizar trabajador existente
     */
    public Trabajador actualizarTrabajador(Long id, Trabajador trabajadorActualizado) {
        return trabajadorRepository.findById(id)
                .map(trabajador -> {
                    if (!trabajador.getCorreo().equals(trabajadorActualizado.getCorreo()) &&
                            trabajadorRepository.existsByCorreo(trabajadorActualizado.getCorreo())) {
                        throw new RuntimeException("Ya existe un trabajador con el correo: " + trabajadorActualizado.getCorreo());
                    }

                    trabajador.setNombre(trabajadorActualizado.getNombre());
                    trabajador.setCorreo(trabajadorActualizado.getCorreo());
                    trabajador.setTelefono(trabajadorActualizado.getTelefono());
                    trabajador.setCiudad(trabajadorActualizado.getCiudad());
                    trabajador.setSalario(trabajadorActualizado.getSalario());
                    trabajador.setPuesto(trabajadorActualizado.getPuesto());
                    trabajador.setDepartamento(trabajadorActualizado.getDepartamento());
                    trabajador.setEstado(trabajadorActualizado.getEstado());

                    return trabajadorRepository.save(trabajador);
                })
                .orElseThrow(() -> new RuntimeException("Trabajador no encontrado con id: " + id));
    }

    public void eliminarTrabajador(Long id) {
        if (!trabajadorRepository.existsById(id)) {
            throw new RuntimeException("Trabajador no encontrado con id: " + id);
        }
        trabajadorRepository.deleteById(id);
    }

    // ==================== CONSULTAS ESPECIALES ====================

    @Transactional(readOnly = true)
    public Page<Trabajador> obtenerTrabajadoresPaginados(int pagina, int tamanio, String ordenarPor, String direccion) {
        Sort sort = direccion.equalsIgnoreCase("desc")
                ? Sort.by(ordenarPor).descending()
                : Sort.by(ordenarPor).ascending();
        Pageable pageable = PageRequest.of(pagina, tamanio, sort);
        return trabajadorRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Trabajador> buscarTrabajadores(String busqueda, int pagina, int tamanio) {
        Pageable pageable = PageRequest.of(pagina, tamanio, Sort.by("nombre").ascending());
        return trabajadorRepository.buscarPorNombreCorreoPuestoOCiudad(busqueda, pageable);
    }

    @Transactional(readOnly = true)
    public List<Trabajador> obtenerPorEstado(EstadoTrabajador estado) {
        return trabajadorRepository.findByEstado(estado);
    }

    public Trabajador cambiarEstado(Long id, EstadoTrabajador nuevoEstado) {
        return trabajadorRepository.findById(id)
                .map(trabajador -> {
                    trabajador.setEstado(nuevoEstado);
                    return trabajadorRepository.save(trabajador);
                })
                .orElseThrow(() -> new RuntimeException("Trabajador no encontrado con id: " + id));
    }

    // ==================== ESTADÍSTICAS ====================

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> estadisticas = new HashMap<>();

        long total = trabajadorRepository.count();
        long activos = trabajadorRepository.countByEstado(EstadoTrabajador.ACTIVO);
        long pendientes = trabajadorRepository.countByEstado(EstadoTrabajador.PENDIENTE);
        long bajas = trabajadorRepository.countByEstado(EstadoTrabajador.BAJA);

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
    public List<Trabajador> obtenerTopTrabajadores() {
        return trabajadorRepository.findTop5ByOrderBySalarioDesc();
    }

    @Transactional(readOnly = true)
    public List<Trabajador> obtenerPorPuesto(String puesto) {
        return trabajadorRepository.findByPuesto(puesto);
    }

    @Transactional(readOnly = true)
    public List<Trabajador> obtenerPorDepartamento(Long departamentoId) {
        return trabajadorRepository.findByDepartamentoId(departamentoId);
    }
}