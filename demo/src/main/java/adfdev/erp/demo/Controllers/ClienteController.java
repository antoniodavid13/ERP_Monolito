package adfdev.erp.demo.Controllers;

import adfdev.erp.demo.Cliente;
import adfdev.erp.demo.Cliente.EstadoCliente;
import adfdev.erp.demo.Usuario;
import adfdev.erp.demo.services.Clienteservice;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/clientes")

public class ClienteController {


    @Autowired
    private Clienteservice clienteService;

    /**
     * Listar todos los clientes con paginación y búsqueda
     */
    @GetMapping
    public String listarClientes(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "5") int tamanio,
            @RequestParam(defaultValue = "id") String ordenarPor,
            @RequestParam(defaultValue = "asc") String direccion,
            @RequestParam(required = false) String busqueda,
            Model model) {

        Page<Cliente> clientesPage;

        if (busqueda != null && !busqueda.trim().isEmpty()) {
            clientesPage = clienteService.buscarClientes(busqueda.trim(), pagina, tamanio);
            model.addAttribute("busqueda", busqueda);
        } else {
            clientesPage = clienteService.obtenerClientesPaginados(pagina, tamanio, ordenarPor, direccion);
        }

        // Estadísticas para los gráficos circulares
        Map<String, Object> estadisticas = clienteService.obtenerEstadisticas();

        // Clientes recientes y top clientes
        model.addAttribute("clientesRecientes", clienteService.obtenerClientesRecientes());
        model.addAttribute("topClientes", clienteService.obtenerTopClientes());

        // Paginación
        model.addAttribute("clientes", clientesPage.getContent());
        model.addAttribute("paginaActual", pagina);
        model.addAttribute("totalPaginas", clientesPage.getTotalPages());
        model.addAttribute("totalElementos", clientesPage.getTotalElements());
        model.addAttribute("tamanio", tamanio);
        model.addAttribute("ordenarPor", ordenarPor);
        model.addAttribute("direccion", direccion);

        // Estadísticas
        model.addAttribute("estadisticas", estadisticas);

        // Estados para el select del formulario
        model.addAttribute("estados", EstadoCliente.values());

        return "clientes/lista";
    }

    /**
     * Mostrar formulario para crear nuevo cliente
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        Cliente clienteNuevo = new Cliente();
        clienteNuevo.setEstado(Cliente.EstadoCliente.ACTIVO); // Estado inicial
        model.addAttribute("cliente", clienteNuevo);
        model.addAttribute("estados", Cliente.EstadoCliente.values());
        model.addAttribute("titulo", "Crear Cliente");
        model.addAttribute("accion", "crear");
        return "clientes/formulario";
    }

    /**
     * Procesar creación de nuevo cliente
     */
    @PostMapping("/crear")
    public String crearCliente(@ModelAttribute Cliente cliente,
                               RedirectAttributes redirectAttributes) {
        try {
            clienteService.crearCliente(cliente);
            redirectAttributes.addFlashAttribute("exito", "Cliente creado exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/clientes/nuevo";
        }
        return "redirect:/clientes";
    }

    /**
     * Mostrar formulario para editar cliente
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model,
                                          RedirectAttributes redirectAttributes) {
        return clienteService.obtenerPorId(id)
                .map(cliente -> {
                    model.addAttribute("cliente", cliente);
                    model.addAttribute("estados", EstadoCliente.values());
                    model.addAttribute("titulo", "Editar Cliente");
                    model.addAttribute("accion", "editar");
                    return "clientes/formulario";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Cliente no encontrado");
                    return "redirect:/clientes";
                });
    }

    /**
     * Procesar actualización de cliente
     */
    @PostMapping("/actualizar/{id}")
    public String actualizarCliente(@PathVariable Long id,
                                    @ModelAttribute Cliente cliente,
                                    RedirectAttributes redirectAttributes) {
        try {
            clienteService.actualizarCliente(id, cliente);
            redirectAttributes.addFlashAttribute("exito", "Cliente actualizado exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/clientes/editar/" + id;
        }
        return "redirect:/clientes";
    }

    /**
     * Eliminar cliente
     */
    @GetMapping("/eliminar/{id}")
    public String eliminarCliente(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        try {
            clienteService.eliminarCliente(id);
            redirectAttributes.addFlashAttribute("exito", "Cliente eliminado exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/clientes";
    }

    /**
     * Cambiar estado del cliente (AJAX-friendly)
     */
    @PostMapping("/cambiar-estado/{id}")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam EstadoCliente estado,
                                RedirectAttributes redirectAttributes) {
        try {
            clienteService.cambiarEstado(id, estado);
            redirectAttributes.addFlashAttribute("exito", "Estado actualizado exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/clientes";
    }

    /**
     * Ver detalle de un cliente
     */
    @GetMapping("/ver/{id}")
    public String verCliente(@PathVariable Long id, Model model,
                             RedirectAttributes redirectAttributes) {
        return clienteService.obtenerPorId(id)
                .map(cliente -> {
                    model.addAttribute("cliente", cliente);
                    return "clientes/detalle";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Cliente no encontrado");
                    return "redirect:/clientes";
                });
    }

    /**
     * Filtrar por estado
     */
    @GetMapping("/filtrar/{estado}")
    public String filtrarPorEstado(@PathVariable EstadoCliente estado, Model model) {
        model.addAttribute("clientes", clienteService.obtenerPorEstado(estado));
        model.addAttribute("estadoFiltrado", estado);
        model.addAttribute("estadisticas", clienteService.obtenerEstadisticas());
        model.addAttribute("estados", EstadoCliente.values());
        return "clientes/lista";
    }
}
