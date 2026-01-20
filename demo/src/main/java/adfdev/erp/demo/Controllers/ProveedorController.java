package adfdev.erp.demo.Controllers;

import adfdev.erp.demo.Proveedor;
import adfdev.erp.demo.Proveedor.EstadoProveedor;
import adfdev.erp.demo.services.Proveedorservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/proveedores")
public class ProveedorController {

    @Autowired
    private Proveedorservice proveedorService;

    /**
     * Listar todos los proveedores con paginación y búsqueda
     */
    @GetMapping
    public String listarProveedores(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "5") int tamanio,
            @RequestParam(defaultValue = "id") String ordenarPor,
            @RequestParam(defaultValue = "asc") String direccion,
            @RequestParam(required = false) String busqueda,
            Model model) {

        Page<Proveedor> proveedoresPage;

        if (busqueda != null && !busqueda.trim().isEmpty()) {
            proveedoresPage = proveedorService.buscarProveedores(busqueda.trim(), pagina, tamanio);
            model.addAttribute("busqueda", busqueda);
        } else {
            proveedoresPage = proveedorService.obtenerProveedoresPaginados(pagina, tamanio, ordenarPor, direccion);
        }

        // Estadísticas para los gráficos circulares
        Map<String, Object> estadisticas = proveedorService.obtenerEstadisticas();

        // Top proveedores
        model.addAttribute("topProveedores", proveedorService.obtenerTopProveedores());

        // Paginación
        model.addAttribute("proveedores", proveedoresPage.getContent());
        model.addAttribute("paginaActual", pagina);
        model.addAttribute("totalPaginas", proveedoresPage.getTotalPages());
        model.addAttribute("totalElementos", proveedoresPage.getTotalElements());
        model.addAttribute("tamanio", tamanio);
        model.addAttribute("ordenarPor", ordenarPor);
        model.addAttribute("direccion", direccion);

        // Estadísticas
        model.addAttribute("estadisticas", estadisticas);

        // Estados para el select del formulario
        model.addAttribute("estados", EstadoProveedor.values());

        return "proveedores/lista";
    }

    /**
     * Mostrar formulario para crear nuevo proveedor
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        Proveedor proveedorNuevo = new Proveedor();
        proveedorNuevo.setEstado(Proveedor.EstadoProveedor.ACTIVO); // Estado inicial
        model.addAttribute("proveedor", proveedorNuevo);
        model.addAttribute("estados", Proveedor.EstadoProveedor.values());
        model.addAttribute("titulo", "Crear Proveedor");
        model.addAttribute("accion", "crear");
        return "proveedores/formulario";
    }

    /**
     * Procesar creación de nuevo proveedor
     */
    @PostMapping("/crear")
    public String crearProveedor(@ModelAttribute Proveedor proveedor,
                                 RedirectAttributes redirectAttributes) {
        try {
            proveedorService.crearProveedor(proveedor);
            redirectAttributes.addFlashAttribute("exito", "Proveedor creado exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/proveedores/nuevo";
        }
        return "redirect:/proveedores";
    }

    /**
     * Mostrar formulario para editar proveedor
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model,
                                          RedirectAttributes redirectAttributes) {
        return proveedorService.obtenerPorId(id)
                .map(proveedor -> {
                    model.addAttribute("proveedor", proveedor);
                    model.addAttribute("estados", EstadoProveedor.values());
                    model.addAttribute("titulo", "Editar Proveedor");
                    model.addAttribute("accion", "editar");
                    return "proveedores/formulario";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Proveedor no encontrado");
                    return "redirect:/proveedores";
                });
    }

    /**
     * Procesar actualización de proveedor
     */
    @PostMapping("/actualizar/{id}")
    public String actualizarProveedor(@PathVariable Long id,
                                      @ModelAttribute Proveedor proveedor,
                                      RedirectAttributes redirectAttributes) {
        try {
            proveedorService.actualizarProveedor(id, proveedor);
            redirectAttributes.addFlashAttribute("exito", "Proveedor actualizado exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/proveedores/editar/" + id;
        }
        return "redirect:/proveedores";
    }

    /**
     * Eliminar proveedor
     */
    @GetMapping("/eliminar/{id}")
    public String eliminarProveedor(@PathVariable Long id,
                                    RedirectAttributes redirectAttributes) {
        try {
            proveedorService.eliminarProveedor(id);
            redirectAttributes.addFlashAttribute("exito", "Proveedor eliminado exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/proveedores";
    }

    /**
     * Cambiar estado del proveedor (AJAX-friendly)
     */
    @PostMapping("/cambiar-estado/{id}")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam EstadoProveedor estado,
                                RedirectAttributes redirectAttributes) {
        try {
            proveedorService.cambiarEstado(id, estado);
            redirectAttributes.addFlashAttribute("exito", "Estado actualizado exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/proveedores";
    }

    /**
     * Ver detalle de un proveedor
     */
    @GetMapping("/ver/{id}")
    public String verProveedor(@PathVariable Long id, Model model,
                               RedirectAttributes redirectAttributes) {
        return proveedorService.obtenerPorId(id)
                .map(proveedor -> {
                    model.addAttribute("proveedor", proveedor);
                    return "proveedores/detalle";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Proveedor no encontrado");
                    return "redirect:/proveedores";
                });
    }

    /**
     * Filtrar por estado
     */
    @GetMapping("/filtrar/{estado}")
    public String filtrarPorEstado(@PathVariable EstadoProveedor estado, Model model) {
        model.addAttribute("proveedores", proveedorService.obtenerPorEstado(estado));
        model.addAttribute("estadoFiltrado", estado);
        model.addAttribute("estadisticas", proveedorService.obtenerEstadisticas());
        model.addAttribute("estados", EstadoProveedor.values());
        return "proveedores/lista";
    }
}