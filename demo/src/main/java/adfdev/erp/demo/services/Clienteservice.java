package adfdev.erp.demo.services;

import adfdev.erp.demo.Cliente;
import adfdev.erp.demo.Cliente.EstadoCliente;
import adfdev.erp.demo.interfaces.Clienterepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class Clienteservice {

    @Autowired
    private Clienterepository clienteRepository;

    /**
     * Crear un nuevo cliente
     */
    public Cliente crearCliente(Cliente cliente) {
        if (clienteRepository.existsByCorreo(cliente.getCorreo())) {
            throw new RuntimeException("Ya existe un cliente con el correo: " + cliente.getCorreo());
        }

        // AJUSTE DE SEGURIDAD: Asegurar que la fecha de registro no sea nula
        // para evitar errores de base de datos al insertar.
        if (cliente.getFechaRegistro() == null) {
            cliente.setFechaRegistro(LocalDate.now());
        }

        return clienteRepository.save(cliente);
    }

    @Transactional(readOnly = true)
    public List<Cliente> obtenerTodos() {
        return clienteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Cliente> obtenerPorId(Long id) {
        return clienteRepository.findById(id);
    }

    /**
     * Actualizar cliente existente
     */
    public Cliente actualizarCliente(Long id, Cliente clienteActualizado) {
        return clienteRepository.findById(id)
                .map(cliente -> {
                    if (!cliente.getCorreo().equals(clienteActualizado.getCorreo()) &&
                            clienteRepository.existsByCorreo(clienteActualizado.getCorreo())) {
                        throw new RuntimeException("Ya existe un cliente con el correo: " + clienteActualizado.getCorreo());
                    }

                    cliente.setNombre(clienteActualizado.getNombre());
                    cliente.setCorreo(clienteActualizado.getCorreo());
                    cliente.setCredito(clienteActualizado.getCredito());
                    cliente.setEstado(clienteActualizado.getEstado());

                    // AJUSTE: Mantenemos los nuevos campos mapeados (telefono, ciudad)
                    cliente.setTelefono(clienteActualizado.getTelefono());
                    cliente.setCiudad(clienteActualizado.getCiudad());

                    return clienteRepository.save(cliente);
                })
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + id));
    }

    public void eliminarCliente(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new RuntimeException("Cliente no encontrado con id: " + id);
        }
        clienteRepository.deleteById(id);
    }

    // ==================== CONSULTAS ESPECIALES ====================

    @Transactional(readOnly = true)
    public Page<Cliente> obtenerClientesPaginados(int pagina, int tamanio, String ordenarPor, String direccion) {
        // AJUSTE: Si el parámetro es "id", cámbialo a "id" (tu atributo en la clase Java)
        // para que coincida con lo que Hibernate espera.
        Sort sort = direccion.equalsIgnoreCase("desc")
                ? Sort.by(ordenarPor).descending()
                : Sort.by(ordenarPor).ascending();
        Pageable pageable = PageRequest.of(pagina, tamanio, sort);
        return clienteRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Cliente> buscarClientes(String busqueda, int pagina, int tamanio) {
        Pageable pageable = PageRequest.of(pagina, tamanio, Sort.by("nombre").ascending());
        return clienteRepository.buscarPorNombreOCorreo(busqueda, pageable);
    }

    @Transactional(readOnly = true)
    public List<Cliente> obtenerPorEstado(EstadoCliente estado) {
        return clienteRepository.findByEstado(estado);
    }

    public Cliente cambiarEstado(Long id, EstadoCliente nuevoEstado) {
        return clienteRepository.findById(id)
                .map(cliente -> {
                    cliente.setEstado(nuevoEstado);
                    return clienteRepository.save(cliente);
                })
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + id));
    }

    // ==================== ESTADÍSTICAS ====================

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> estadisticas = new HashMap<>();

        long total = clienteRepository.count();
        long activos = clienteRepository.countByEstado(EstadoCliente.ACTIVO);
        long pendientes = clienteRepository.countByEstado(EstadoCliente.PENDIENTE);
        long bajas = clienteRepository.countByEstado(EstadoCliente.BAJA);

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
    public List<Cliente> obtenerTopClientes() {
        return clienteRepository.findTop5ByOrderByCreditoDesc();
    }

    @Transactional(readOnly = true)
    public List<Cliente> obtenerClientesRecientes() {
        return clienteRepository.findTop5ByOrderByFechaRegistroDesc();
    }
}