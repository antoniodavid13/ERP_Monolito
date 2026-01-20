package adfdev.erp.demo.Controllers;

import adfdev.erp.demo.ProductoProveedor;
import adfdev.erp.demo.ProductoProveedor.EstadoProducto;
import adfdev.erp.demo.services.ProductoProveedorservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/productos-proveedores")
public class ProductoProveedorController {

    @Autowired
    private ProductoProveedorservice productoProveedorService;

    /**
     * Listar todos los productos de proveedores
     */
    @GetMapping
    public String listarProductosProveedores(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "5") int tamanio,
            @RequestParam(defaultValue = "id") String ordenarPor,
            @RequestParam(defaultValue = "asc") String direccion,
            @RequestParam(required = false) String busqueda,
            Model model) {

        Page<ProductoProveedor> productosPage;

        if (busqueda != null && !busqueda.trim().isEmpty()) {
            productosPage = productoProveedorService.buscarProductosProveedores(busqueda.trim(), pagina, tamanio);
            model.addAttribute("busqueda", busqueda);
        } else {
            productosPage = productoProveedorService.obtenerProductosProveedoresPaginados(pagina, tamanio, ordenarPor, direccion);
        }

        Map<String, Object> estadisticas = productoProveedorService.obtenerEstadisticas();

        model.addAttribute("topProductos", productoProveedorService.obtenerTopProductos());
        model.addAttribute("productos", productosPage.getContent());
        model.addAttribute("paginaActual", pagina);
        model.addAttribute("totalPaginas", productosPage.getTotalPages());
        model.addAttribute("totalElementos", productosPage.getTotalElements());
        model.addAttribute("tamanio", tamanio);
        model.addAttribute("ordenarPor", ordenarPor);
        model.addAttribute("direccion", direccion);
        model.addAttribute("estadisticas", estadisticas);
        model.addAttribute("estados", EstadoProducto.values());

        return "productos-proveedores/lista";
    }

    /**
     * Mostrar formulario para crear nuevo producto
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        ProductoProveedor productoNuevo = new ProductoProveedor();
        productoNuevo.setEstado(EstadoProducto.ACTIVO);
        model.addAttribute("producto", productoNuevo);
        model.addAttribute("estados", EstadoProducto.values());
        model.addAttribute("titulo", "Crear Producto Proveedor");
        model.addAttribute("accion", "crear");
        return "productos-proveedores/formulario";
    }

    /**
     * Procesar creación
     */
    @PostMapping("/crear")
    public String crearProductoProveedor(@ModelAttribute ProductoProveedor producto,
                                         RedirectAttributes redirectAttributes) {
        try {
            productoProveedorService.crearProductoProveedor(producto);
            redirectAttributes.addFlashAttribute("exito", "Producto creado exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/productos-proveedores/nuevo";
        }
        return "redirect:/productos-proveedores";
    }

    /**
     * Mostrar formulario para editar
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model,
                                          RedirectAttributes redirectAttributes) {
        return productoProveedorService.obtenerPorId(id)
                .map(producto -> {
                    model.addAttribute("producto", producto);
                    model.addAttribute("estados", EstadoProducto.values());
                    model.addAttribute("titulo", "Editar Producto Proveedor");
                    model.addAttribute("accion", "editar");
                    return "productos-proveedores/formulario";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
                    return "redirect:/productos-proveedores";
                });
    }

    /**
     * Procesar actualización
     */
    @PostMapping("/actualizar/{id}")
    public String actualizarProductoProveedor(@PathVariable Long id,
                                              @ModelAttribute ProductoProveedor producto,
                                              RedirectAttributes redirectAttributes) {
        try {
            productoProveedorService.actualizarProductoProveedor(id, producto);
            redirectAttributes.addFlashAttribute("exito", "Producto actualizado exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/productos-proveedores/editar/" + id;
        }
        return "redirect:/productos-proveedores";
    }

    /**
     * Eliminar producto
     */
    @GetMapping("/eliminar/{id}")
    public String eliminarProductoProveedor(@PathVariable Long id,
                                            RedirectAttributes redirectAttributes) {
        try {
            productoProveedorService.eliminarProductoProveedor(id);
            redirectAttributes.addFlashAttribute("exito", "Producto eliminado exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/productos-proveedores";
    }

    /**
     * Ver detalle
     */
    @GetMapping("/ver/{id}")
    public String verProductoProveedor(@PathVariable Long id, Model model,
                                       RedirectAttributes redirectAttributes) {
        return productoProveedorService.obtenerPorId(id)
                .map(producto -> {
                    model.addAttribute("producto", producto);
                    return "productos-proveedores/detalle";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
                    return "redirect:/productos-proveedores";
                });
    }

    /**
     * Filtrar por estado
     */
    @GetMapping("/filtrar/{estado}")
    public String filtrarPorEstado(@PathVariable EstadoProducto estado, Model model) {
        model.addAttribute("productos", productoProveedorService.obtenerPorEstado(estado));
        model.addAttribute("estadoFiltrado", estado);
        model.addAttribute("estadisticas", productoProveedorService.obtenerEstadisticas());
        model.addAttribute("estados", EstadoProducto.values());
        return "productos-proveedores/lista";
    }

    /**
     * Cambiar estado de un producto
     */
    @PostMapping("/cambiar-estado/{id}")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam EstadoProducto nuevoEstado,
                                RedirectAttributes redirectAttributes) {
        try {
            productoProveedorService.cambiarEstado(id, nuevoEstado);
            redirectAttributes.addFlashAttribute("exito", "Estado cambiado exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/productos-proveedores";
    }
}