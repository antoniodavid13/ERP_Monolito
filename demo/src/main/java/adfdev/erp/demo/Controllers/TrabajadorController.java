package adfdev.erp.demo.Controllers;

import adfdev.erp.demo.Trabajador;
import adfdev.erp.demo.Trabajador.EstadoTrabajador;
import adfdev.erp.demo.services.Departamentoservice;
import adfdev.erp.demo.services.Trabajadorservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/trabajadores")
public class TrabajadorController {

    @Autowired
    private Trabajadorservice trabajadorService;

    @Autowired
    private Departamentoservice departamentoService;

    /**
     * Listar todos los trabajadores con paginación y búsqueda
     */
    @GetMapping
    public String listarTrabajadores(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "5") int tamanio,
            @RequestParam(defaultValue = "id") String ordenarPor,
            @RequestParam(defaultValue = "asc") String direccion,
            @RequestParam(required = false) String busqueda,
            Model model) {

        Page<Trabajador> trabajadoresPage;

        if (busqueda != null && !busqueda.trim().isEmpty()) {
            trabajadoresPage = trabajadorService.buscarTrabajadores(busqueda.trim(), pagina, tamanio);
            model.addAttribute("busqueda", busqueda);
        } else {
            trabajadoresPage = trabajadorService.obtenerTrabajadoresPaginados(pagina, tamanio, ordenarPor, direccion);
        }

        // Estadísticas para los gráficos circulares
        Map<String, Object> estadisticas = trabajadorService.obtenerEstadisticas();

        // Top trabajadores
        model.addAttribute("topTrabajadores", trabajadorService.obtenerTopTrabajadores());

        // Paginación
        model.addAttribute("trabajadores", trabajadoresPage.getContent());
        model.addAttribute("paginaActual", pagina);
        model.addAttribute("totalPaginas", trabajadoresPage.getTotalPages());
        model.addAttribute("totalElementos", trabajadoresPage.getTotalElements());
        model.addAttribute("tamanio", tamanio);
        model.addAttribute("ordenarPor", ordenarPor);
        model.addAttribute("direccion", direccion);

        // Estadísticas
        model.addAttribute("estadisticas", estadisticas);

        // Estados para el select del formulario
        model.addAttribute("estados", EstadoTrabajador.values());

        return "trabajadores/lista";
    }

    /**
     * Mostrar formulario para crear nuevo trabajador
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        Trabajador trabajadorNuevo = new Trabajador();
        trabajadorNuevo.setEstado(Trabajador.EstadoTrabajador.ACTIVO); // Estado inicial
        model.addAttribute("trabajador", trabajadorNuevo);
        model.addAttribute("estados", Trabajador.EstadoTrabajador.values());
        model.addAttribute("departamentos", departamentoService.obtenerTodos()); // Lista de departamentos
        model.addAttribute("titulo", "Crear Trabajador");
        model.addAttribute("accion", "crear");
        return "trabajadores/formulario";
    }

    /**
     * Procesar creación de nuevo trabajador
     */
    @PostMapping("/crear")
    public String crearTrabajador(@ModelAttribute Trabajador trabajador,
                                  @RequestParam(required = false) Long departamentoId,
                                  RedirectAttributes redirectAttributes) {
        try {
            if (departamentoId != null) {
                departamentoService.obtenerPorId(departamentoId).ifPresent(trabajador::setDepartamento);
            }
            trabajadorService.crearTrabajador(trabajador);
            redirectAttributes.addFlashAttribute("exito", "Trabajador creado exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/trabajadores/nuevo";
        }
        return "redirect:/trabajadores";
    }

    /**
     * Mostrar formulario para editar trabajador
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model,
                                          RedirectAttributes redirectAttributes) {
        return trabajadorService.obtenerPorId(id)
                .map(trabajador -> {
                    model.addAttribute("trabajador", trabajador);
                    model.addAttribute("estados", EstadoTrabajador.values());
                    model.addAttribute("departamentos", departamentoService.obtenerTodos()); // Lista de departamentos
                    model.addAttribute("titulo", "Editar Trabajador");
                    model.addAttribute("accion", "editar");
                    return "trabajadores/formulario";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Trabajador no encontrado");
                    return "redirect:/trabajadores";
                });
    }

    /**
     * Procesar actualización de trabajador
     */
    @PostMapping("/actualizar/{id}")
    public String actualizarTrabajador(@PathVariable Long id,
                                       @ModelAttribute Trabajador trabajador,
                                       @RequestParam(required = false) Long departamentoId,
                                       RedirectAttributes redirectAttributes) {
        try {
            if (departamentoId != null) {
                departamentoService.obtenerPorId(departamentoId).ifPresent(trabajador::setDepartamento);
            }
            trabajadorService.actualizarTrabajador(id, trabajador);
            redirectAttributes.addFlashAttribute("exito", "Trabajador actualizado exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/trabajadores/editar/" + id;
        }
        return "redirect:/trabajadores";
    }

    /**
     * Eliminar trabajador
     */
    @GetMapping("/eliminar/{id}")
    public String eliminarTrabajador(@PathVariable Long id,
                                     RedirectAttributes redirectAttributes) {
        try {
            trabajadorService.eliminarTrabajador(id);
            redirectAttributes.addFlashAttribute("exito", "Trabajador eliminado exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/trabajadores";
    }

    /**
     * Cambiar estado del trabajador (AJAX-friendly)
     */
    @PostMapping("/cambiar-estado/{id}")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam EstadoTrabajador estado,
                                RedirectAttributes redirectAttributes) {
        try {
            trabajadorService.cambiarEstado(id, estado);
            redirectAttributes.addFlashAttribute("exito", "Estado actualizado exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/trabajadores";
    }

    /**
     * Ver detalle de un trabajador
     */
    @GetMapping("/ver/{id}")
    public String verTrabajador(@PathVariable Long id, Model model,
                                RedirectAttributes redirectAttributes) {
        return trabajadorService.obtenerPorId(id)
                .map(trabajador -> {
                    model.addAttribute("trabajador", trabajador);
                    return "trabajadores/detalle";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Trabajador no encontrado");
                    return "redirect:/trabajadores";
                });
    }

    /**
     * Filtrar por estado
     */
    @GetMapping("/filtrar/{estado}")
    public String filtrarPorEstado(@PathVariable EstadoTrabajador estado, Model model) {
        model.addAttribute("trabajadores", trabajadorService.obtenerPorEstado(estado));
        model.addAttribute("estadoFiltrado", estado);
        model.addAttribute("estadisticas", trabajadorService.obtenerEstadisticas());
        model.addAttribute("estados", EstadoTrabajador.values());
        return "trabajadores/lista";
    }
}