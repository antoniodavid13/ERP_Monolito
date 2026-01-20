package adfdev.erp.demo.services;

import adfdev.erp.demo.MetodoEnvio;
import adfdev.erp.demo.interfaces.MetodoEnvioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MetodoEnvioService {

    @Autowired
    private MetodoEnvioRepository metodoEnvioRepository;

    /**
     * Crear un nuevo método de envío
     */
    public MetodoEnvio crearMetodoEnvio(MetodoEnvio metodoEnvio) {
        if (metodoEnvioRepository.existsByNombre(metodoEnvio.getNombre())) {
            throw new RuntimeException("Ya existe un método de envío con el nombre: " + metodoEnvio.getNombre());
        }
        return metodoEnvioRepository.save(metodoEnvio);
    }

    /**
     * Obtener todos los métodos de envío
     */
    @Transactional(readOnly = true)
    public List<MetodoEnvio> obtenerTodos() {
        return metodoEnvioRepository.findAll(Sort.by("nombre").ascending());
    }

    /**
     * Obtener método de envío por ID
     */
    @Transactional(readOnly = true)
    public Optional<MetodoEnvio> obtenerPorId(Long id) {
        return metodoEnvioRepository.findById(id);
    }

    /**
     * Obtener método de envío por nombre
     */
    @Transactional(readOnly = true)
    public Optional<MetodoEnvio> obtenerPorNombre(String nombre) {
        return metodoEnvioRepository.findByNombre(nombre);
    }

    /**
     * Actualizar método de envío
     */
    public MetodoEnvio actualizarMetodoEnvio(Long id, MetodoEnvio metodoEnvioActualizado) {
        return metodoEnvioRepository.findById(id)
                .map(metodoEnvio -> {
                    if (!metodoEnvio.getNombre().equals(metodoEnvioActualizado.getNombre()) &&
                            metodoEnvioRepository.existsByNombre(metodoEnvioActualizado.getNombre())) {
                        throw new RuntimeException("Ya existe un método de envío con el nombre: " + metodoEnvioActualizado.getNombre());
                    }

                    metodoEnvio.setNombre(metodoEnvioActualizado.getNombre());
                    return metodoEnvioRepository.save(metodoEnvio);
                })
                .orElseThrow(() -> new RuntimeException("Método de envío no encontrado con id: " + id));
    }

    /**
     * Eliminar método de envío
     */
    public void eliminarMetodoEnvio(Long id) {
        if (!metodoEnvioRepository.existsById(id)) {
            throw new RuntimeException("Método de envío no encontrado con id: " + id);
        }
        metodoEnvioRepository.deleteById(id);
    }

    /**
     * Obtener métodos de envío paginados
     */
    @Transactional(readOnly = true)
    public Page<MetodoEnvio> obtenerMetodosEnvioPaginados(int pagina, int tamanio, String ordenarPor, String direccion) {
        Sort sort = direccion.equalsIgnoreCase("desc")
                ? Sort.by(ordenarPor).descending()
                : Sort.by(ordenarPor).ascending();
        Pageable pageable = PageRequest.of(pagina, tamanio, sort);
        return metodoEnvioRepository.findAll(pageable);
    }

    /**
     * Contar total de métodos de envío
     */
    @Transactional(readOnly = true)
    public long contarTotal() {
        return metodoEnvioRepository.count();
    }
}