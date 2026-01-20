package adfdev.erp.demo.Controllers;

import adfdev.erp.demo.ProductoCliente;
import adfdev.erp.demo.ProductoCliente.EstadoProducto;
import adfdev.erp.demo.services.ProductoClienteservice;
import adfdev.erp.demo.services.Almacenservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/productos-clientes")
public class ProductoClienteController {

    @Autowired
    private ProductoClienteservice productoClienteService;

    /**
     * Listar todos los productos de clientes
     */
    @GetMapping
    public String listarProductosClientes(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "5") int tamanio,
            @RequestParam(defaultValue = "id") String ordenarPor,
            @RequestParam(defaultValue = "asc") String direccion,
            @RequestParam(required = false) String busqueda,
            Model model) {

        Page<ProductoCliente> productosPage;

        if (busqueda != null && !busqueda.trim().isEmpty()) {
            productosPage = productoClienteService.buscarProductosClientes(busqueda.trim(), pagina, tamanio);
            model.addAttribute("busqueda", busqueda);
        } else {
            productosPage = productoClienteService.obtenerProductosClientesPaginados(pagina, tamanio, ordenarPor, direccion);
        }

        Map<String, Object> estadisticas = productoClienteService.obtenerEstadisticas();

        model.addAttribute("topProductos", productoClienteService.obtenerTopProductos());
        model.addAttribute("productos", productosPage.getContent());
        model.addAttribute("paginaActual", pagina);
        model.addAttribute("totalPaginas", productosPage.getTotalPages());
        model.addAttribute("totalElementos", productosPage.getTotalElements());
        model.addAttribute("tamanio", tamanio);
        model.addAttribute("ordenarPor", ordenarPor);
        model.addAttribute("direccion", direccion);
        model.addAttribute("estadisticas", estadisticas);
        model.addAttribute("estados", EstadoProducto.values());

        return "productos-clientes/lista";
    }

    /**
     * Mostrar formulario para crear nuevo producto
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        ProductoCliente productoNuevo = new ProductoCliente();
        productoNuevo.setEstado(EstadoProducto.ACTIVO);
        model.addAttribute("producto", productoNuevo);
        model.addAttribute("estados", EstadoProducto.values());
        model.addAttribute("titulo", "Crear Producto Cliente");
        model.addAttribute("accion", "crear");
        return "productos-clientes/formulario";
    }

    /**
     * Procesar creación
     */
    @PostMapping("/crear")
    public String crearProductoCliente(@ModelAttribute ProductoCliente producto,
                                       RedirectAttributes redirectAttributes) {
        try {
            productoClienteService.crearProductoCliente(producto);
            redirectAttributes.addFlashAttribute("exito", "Producto creado exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/productos-clientes/nuevo";
        }
        return "redirect:/productos-clientes";
    }

    /**
     * Mostrar formulario para editar
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model,
                                          RedirectAttributes redirectAttributes) {
        return productoClienteService.obtenerPorId(id)
                .map(producto -> {
                    model.addAttribute("producto", producto);
                    model.addAttribute("estados", EstadoProducto.values());
                    model.addAttribute("titulo", "Editar Producto Cliente");
                    model.addAttribute("accion", "editar");
                    return "productos-clientes/formulario";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
                    return "redirect:/productos-clientes";
                });
    }

    /**
     * Procesar actualización
     */
    @PostMapping("/actualizar/{id}")
    public String actualizarProductoCliente(@PathVariable Long id,
                                            @ModelAttribute ProductoCliente producto,
                                            RedirectAttributes redirectAttributes) {
        try {
            productoClienteService.actualizarProductoCliente(id, producto);
            redirectAttributes.addFlashAttribute("exito", "Producto actualizado exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/productos-clientes/editar/" + id;
        }
        return "redirect:/productos-clientes";
    }

    /**
     * Eliminar producto
     */
    @GetMapping("/eliminar/{id}")
    public String eliminarProductoCliente(@PathVariable Long id,
                                          RedirectAttributes redirectAttributes) {
        try {
            productoClienteService.eliminarProductoCliente(id);
            redirectAttributes.addFlashAttribute("exito", "Producto eliminado exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/productos-clientes";
    }

    /**
     * Ver detalle
     */
    @GetMapping("/ver/{id}")
    public String verProductoCliente(@PathVariable Long id, Model model,
                                     RedirectAttributes redirectAttributes) {
        return productoClienteService.obtenerPorId(id)
                .map(producto -> {
                    model.addAttribute("producto", producto);
                    return "productos-clientes/detalle";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
                    return "redirect:/productos-clientes";
                });
    }

    /**
     * Filtrar por estado
     */
    @GetMapping("/filtrar/{estado}")
    public String filtrarPorEstado(@PathVariable EstadoProducto estado, Model model) {
        model.addAttribute("productos", productoClienteService.obtenerPorEstado(estado));
        model.addAttribute("estadoFiltrado", estado);
        model.addAttribute("estadisticas", productoClienteService.obtenerEstadisticas());
        model.addAttribute("estados", EstadoProducto.values());
        return "productos-clientes/lista";
    }
}