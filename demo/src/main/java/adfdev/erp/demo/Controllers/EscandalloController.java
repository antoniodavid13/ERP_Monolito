package adfdev.erp.demo.Controllers;

import adfdev.erp.demo.Escandallo;
import adfdev.erp.demo.services.Escandalloservice;
import adfdev.erp.demo.services.ProductoProveedorservice;
import adfdev.erp.demo.services.ProductoClienteservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/escandallo")
public class EscandalloController {

    @Autowired
    private Escandalloservice escandalloService;

    @Autowired
    private ProductoClienteservice productoClienteService;

    @Autowired
    private ProductoProveedorservice productoProveedorService;

    /**
     * Ver escandallo de un producto cliente
     */
    @GetMapping("/{idProductoCli}")
    public String verEscandallo(@PathVariable Long idProductoCli, Model model,
                                RedirectAttributes redirectAttributes) {
        return productoClienteService.obtenerPorId(idProductoCli)
                .map(producto -> {
                    List<Escandallo> lineas = escandalloService.obtenerPorProductoCliente(idProductoCli);
                    model.addAttribute("producto", producto);
                    model.addAttribute("lineas", lineas);
                    model.addAttribute("productosProveedor", productoProveedorService.obtenerTodos());
                    return "escandallo/detalle";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Producto cliente no encontrado");
                    return "redirect:/productos-clientes";
                });
    }

    /**
     * Agregar línea de escandallo
     */
    @PostMapping("/{idProductoCli}/agregar")
    public String agregarLinea(@PathVariable Long idProductoCli,
                               @RequestParam Long idProductoProv,
                               @RequestParam BigDecimal cantidadNecesaria,
                               RedirectAttributes redirectAttributes) {
        try {
            escandalloService.agregarLinea(idProductoCli, idProductoProv, cantidadNecesaria);
            redirectAttributes.addFlashAttribute("exito", "Componente añadido al escandallo");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/escandallo/" + idProductoCli;
    }

    /**
     * Actualizar cantidad de una línea
     */
    @PostMapping("/{idProductoCli}/actualizar/{idProductoProv}")
    public String actualizarLinea(@PathVariable Long idProductoCli,
                                  @PathVariable Long idProductoProv,
                                  @RequestParam BigDecimal cantidadNecesaria,
                                  RedirectAttributes redirectAttributes) {
        try {
            escandalloService.actualizarCantidad(idProductoCli, idProductoProv, cantidadNecesaria);
            redirectAttributes.addFlashAttribute("exito", "Cantidad actualizada");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/escandallo/" + idProductoCli;
    }

    /**
     * Eliminar línea de escandallo
     */
    @GetMapping("/{idProductoCli}/eliminar/{idProductoProv}")
    public String eliminarLinea(@PathVariable Long idProductoCli,
                                @PathVariable Long idProductoProv,
                                RedirectAttributes redirectAttributes) {
        try {
            escandalloService.eliminarLinea(idProductoCli, idProductoProv);
            redirectAttributes.addFlashAttribute("exito", "Componente eliminado del escandallo");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/escandallo/" + idProductoCli;
    }

    /**
     * Recalcular todos los escandallos
     */
    @PostMapping("/recalcular-todos")
    public String recalcularTodos(RedirectAttributes redirectAttributes) {
        try {
            escandalloService.recalcularTodos();
            redirectAttributes.addFlashAttribute("exito", "Todos los costes recalculados");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/productos-clientes";
    }
}