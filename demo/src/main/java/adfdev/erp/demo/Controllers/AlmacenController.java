package adfdev.erp.demo.Controllers;

import adfdev.erp.demo.Almacen;
import adfdev.erp.demo.Almacen.EstadoAlmacen;
import adfdev.erp.demo.Usuario;
import adfdev.erp.demo.services.Almacenservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/almacenes")
public class AlmacenController {

    @Autowired
    private Almacenservice almacenService;

    /**
     * Listar todos los almacenes con paginación y búsqueda
     */
    @GetMapping
    public String listarAlmacenes(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "5") int tamanio,
            @RequestParam(defaultValue = "id") String ordenarPor,
            @RequestParam(defaultValue = "asc") String direccion,
            @RequestParam(required = false) String busqueda,
            Model model) {

        Page<Almacen> almacenesPage;

        if (busqueda != null && !busqueda.trim().isEmpty()) {
            almacenesPage = almacenService.buscarAlmacenes(busqueda.trim(), pagina, tamanio);
            model.addAttribute("busqueda", busqueda);
        } else {
            almacenesPage = almacenService.obtenerAlmacenesPaginados(pagina, tamanio, ordenarPor, direccion);
        }

        // Estadísticas para los gráficos circulares
        Map<String, Object> estadisticas = almacenService.obtenerEstadisticas();

        // Top almacenes
        model.addAttribute("topAlmacenes", almacenService.obtenerTopAlmacenes());

        // Paginación
        model.addAttribute("almacenes", almacenesPage.getContent());
        model.addAttribute("paginaActual", pagina);
        model.addAttribute("totalPaginas", almacenesPage.getTotalPages());
        model.addAttribute("totalElementos", almacenesPage.getTotalElements());
        model.addAttribute("tamanio", tamanio);
        model.addAttribute("ordenarPor", ordenarPor);
        model.addAttribute("direccion", direccion);

        // Estadísticas
        model.addAttribute("estadisticas", estadisticas);

        // Estados para el select del formulario
        model.addAttribute("estados", EstadoAlmacen.values());

        return "almacenes/lista";
    }

    /**
     * Mostrar formulario para crear nuevo almacén
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        Almacen almacenNuevo = new Almacen();
        almacenNuevo.setEstado(Almacen.EstadoAlmacen.ACTIVO); // Estado inicial
        model.addAttribute("almacen", almacenNuevo);
        model.addAttribute("estados", Almacen.EstadoAlmacen.values());
        model.addAttribute("titulo", "Crear Almacén");
        model.addAttribute("accion", "crear");
        return "almacenes/formulario";
    }

    /**
     * Procesar creación de nuevo almacén
     */
    @PostMapping("/crear")
    public String crearAlmacen(@ModelAttribute Almacen almacen,
                               RedirectAttributes redirectAttributes) {
        try {
            almacenService.crearAlmacen(almacen);
            redirectAttributes.addFlashAttribute("exito", "Almacén creado exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/almacenes/nuevo";
        }
        return "redirect:/almacenes";
    }

    /**
     * Mostrar formulario para editar almacén
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model,
                                          RedirectAttributes redirectAttributes) {
        return almacenService.obtenerPorId(id)
                .map(almacen -> {
                    model.addAttribute("almacen", almacen);
                    model.addAttribute("estados", EstadoAlmacen.values());
                    model.addAttribute("titulo", "Editar Almacén");
                    model.addAttribute("accion", "editar");
                    return "almacenes/formulario";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Almacén no encontrado");
                    return "redirect:/almacenes";
                });
    }

    /**
     * Procesar actualización de almacén
     */
    @PostMapping("/actualizar/{id}")
    public String actualizarAlmacen(@PathVariable Long id,
                                    @ModelAttribute Almacen almacen,
                                    RedirectAttributes redirectAttributes) {
        try {
            almacenService.actualizarAlmacen(id, almacen);
            redirectAttributes.addFlashAttribute("exito", "Almacén actualizado exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/almacenes/editar/" + id;
        }
        return "redirect:/almacenes";
    }

    /**
     * Eliminar almacén
     */
    @GetMapping("/eliminar/{id}")
    public String eliminarAlmacen(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        try {
            almacenService.eliminarAlmacen(id);
            redirectAttributes.addFlashAttribute("exito", "Almacén eliminado exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/almacenes";
    }

    /**
     * Cambiar estado del almacén (AJAX-friendly)
     */
    @PostMapping("/cambiar-estado/{id}")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam EstadoAlmacen estado,
                                RedirectAttributes redirectAttributes) {
        try {
            almacenService.cambiarEstado(id, estado);
            redirectAttributes.addFlashAttribute("exito", "Estado actualizado exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/almacenes";
    }

    /**
     * Ver detalle de un almacén
     */
    @GetMapping("/ver/{id}")
    public String verAlmacen(@PathVariable Long id, Model model,
                             RedirectAttributes redirectAttributes) {
        return almacenService.obtenerPorId(id)
                .map(almacen -> {
                    model.addAttribute("almacen", almacen);
                    return "almacenes/detalle";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Almacén no encontrado");
                    return "redirect:/almacenes";
                });
    }

    /**
     * Filtrar por estado
     */
    @GetMapping("/filtrar/{estado}")
    public String filtrarPorEstado(@PathVariable EstadoAlmacen estado, Model model) {
        model.addAttribute("almacenes", almacenService.obtenerPorEstado(estado));
        model.addAttribute("estadoFiltrado", estado);
        model.addAttribute("estadisticas", almacenService.obtenerEstadisticas());
        model.addAttribute("estados", EstadoAlmacen.values());
        return "almacenes/lista";
    }
}