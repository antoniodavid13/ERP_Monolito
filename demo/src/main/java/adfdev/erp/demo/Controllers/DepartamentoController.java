package adfdev.erp.demo.Controllers;

import adfdev.erp.demo.Departamento;
import adfdev.erp.demo.Departamento.EstadoDepartamento;
import adfdev.erp.demo.services.Departamentoservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/departamentos")
public class DepartamentoController {

    @Autowired
    private Departamentoservice departamentoService;

    /**
     * Listar todos los departamentos con paginación y búsqueda
     */
    @GetMapping
    public String listarDepartamentos(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "5") int tamanio,
            @RequestParam(defaultValue = "id") String ordenarPor,
            @RequestParam(defaultValue = "asc") String direccion,
            @RequestParam(required = false) String busqueda,
            Model model) {

        Page<Departamento> departamentosPage;

        if (busqueda != null && !busqueda.trim().isEmpty()) {
            departamentosPage = departamentoService.buscarDepartamentos(busqueda.trim(), pagina, tamanio);
            model.addAttribute("busqueda", busqueda);
        } else {
            departamentosPage = departamentoService.obtenerDepartamentosPaginados(pagina, tamanio, ordenarPor, direccion);
        }

        // Estadísticas para los gráficos circulares
        Map<String, Object> estadisticas = departamentoService.obtenerEstadisticas();

        // Top departamentos
        model.addAttribute("topDepartamentos", departamentoService.obtenerTopDepartamentos());

        // Paginación
        model.addAttribute("departamentos", departamentosPage.getContent());
        model.addAttribute("paginaActual", pagina);
        model.addAttribute("totalPaginas", departamentosPage.getTotalPages());
        model.addAttribute("totalElementos", departamentosPage.getTotalElements());
        model.addAttribute("tamanio", tamanio);
        model.addAttribute("ordenarPor", ordenarPor);
        model.addAttribute("direccion", direccion);

        // Estadísticas
        model.addAttribute("estadisticas", estadisticas);

        // Estados para el select del formulario
        model.addAttribute("estados", EstadoDepartamento.values());

        return "departamentos/lista";
    }

    /**
     * Mostrar formulario para crear nuevo departamento
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        Departamento departamentoNuevo = new Departamento();
        departamentoNuevo.setEstado(Departamento.EstadoDepartamento.ACTIVO); // Estado inicial
        model.addAttribute("departamento", departamentoNuevo);
        model.addAttribute("estados", Departamento.EstadoDepartamento.values());
        model.addAttribute("titulo", "Crear Departamento");
        model.addAttribute("accion", "crear");
        return "departamentos/formulario";
    }

    /**
     * Procesar creación de nuevo departamento
     */
    @PostMapping("/crear")
    public String crearDepartamento(@ModelAttribute Departamento departamento,
                                    RedirectAttributes redirectAttributes) {
        try {
            departamentoService.crearDepartamento(departamento);
            redirectAttributes.addFlashAttribute("exito", "Departamento creado exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/departamentos/nuevo";
        }
        return "redirect:/departamentos";
    }

    /**
     * Mostrar formulario para editar departamento
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model,
                                          RedirectAttributes redirectAttributes) {
        return departamentoService.obtenerPorId(id)
                .map(departamento -> {
                    model.addAttribute("departamento", departamento);
                    model.addAttribute("estados", EstadoDepartamento.values());
                    model.addAttribute("titulo", "Editar Departamento");
                    model.addAttribute("accion", "editar");
                    return "departamentos/formulario";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Departamento no encontrado");
                    return "redirect:/departamentos";
                });
    }

    /**
     * Procesar actualización de departamento
     */
    @PostMapping("/actualizar/{id}")
    public String actualizarDepartamento(@PathVariable Long id,
                                         @ModelAttribute Departamento departamento,
                                         RedirectAttributes redirectAttributes) {
        try {
            departamentoService.actualizarDepartamento(id, departamento);
            redirectAttributes.addFlashAttribute("exito", "Departamento actualizado exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/departamentos/editar/" + id;
        }
        return "redirect:/departamentos";
    }

    /**
     * Eliminar departamento
     */
    @GetMapping("/eliminar/{id}")
    public String eliminarDepartamento(@PathVariable Long id,
                                       RedirectAttributes redirectAttributes) {
        try {
            departamentoService.eliminarDepartamento(id);
            redirectAttributes.addFlashAttribute("exito", "Departamento eliminado exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/departamentos";
    }

    /**
     * Cambiar estado del departamento (AJAX-friendly)
     */
    @PostMapping("/cambiar-estado/{id}")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam EstadoDepartamento estado,
                                RedirectAttributes redirectAttributes) {
        try {
            departamentoService.cambiarEstado(id, estado);
            redirectAttributes.addFlashAttribute("exito", "Estado actualizado exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/departamentos";
    }

    /**
     * Ver detalle de un departamento
     */
    @GetMapping("/ver/{id}")
    public String verDepartamento(@PathVariable Long id, Model model,
                                  RedirectAttributes redirectAttributes) {
        return departamentoService.obtenerPorId(id)
                .map(departamento -> {
                    model.addAttribute("departamento", departamento);
                    return "departamentos/detalle";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Departamento no encontrado");
                    return "redirect:/departamentos";
                });
    }

    /**
     * Filtrar por estado
     */
    @GetMapping("/filtrar/{estado}")
    public String filtrarPorEstado(@PathVariable EstadoDepartamento estado, Model model) {
        model.addAttribute("departamentos", departamentoService.obtenerPorEstado(estado));
        model.addAttribute("estadoFiltrado", estado);
        model.addAttribute("estadisticas", departamentoService.obtenerEstadisticas());
        model.addAttribute("estados", EstadoDepartamento.values());
        return "departamentos/lista";
    }
}