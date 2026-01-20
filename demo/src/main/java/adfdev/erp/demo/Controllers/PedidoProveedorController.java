package adfdev.erp.demo.Controllers;

import adfdev.erp.demo.PedidoProveedor;
import adfdev.erp.demo.PedidoProveedor.EstadoPedido;
import adfdev.erp.demo.PedidoProveedor.Prioridad;
import adfdev.erp.demo.ProductoProveedor;
import adfdev.erp.demo.services.PedidoProveedorService;
import adfdev.erp.demo.services.ProductoProveedorservice;
import adfdev.erp.demo.services.Proveedorservice;
import adfdev.erp.demo.services.Trabajadorservice;
import adfdev.erp.demo.services.Almacenservice;
import adfdev.erp.demo.services.MetodoEnvioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/pedidos-proveedores")
public class PedidoProveedorController {

    @Autowired
    private PedidoProveedorService pedidoProveedorService;

    @Autowired
    private ProductoProveedorservice productoProveedorService;

    @Autowired(required = false)
    private Proveedorservice proveedorService;

    @Autowired(required = false)
    private Trabajadorservice trabajadorService;

    @Autowired(required = false)
    private Almacenservice almacenService;

    @Autowired(required = false)
    private MetodoEnvioService metodoEnvioService;

    /**
     * Listar todos los pedidos de proveedores
     */
    @GetMapping
    public String listarPedidosProveedores(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanio,
            @RequestParam(defaultValue = "id") String ordenarPor,
            @RequestParam(defaultValue = "desc") String direccion,
            Model model) {

        Page<PedidoProveedor> pedidosPage = pedidoProveedorService.obtenerPedidosProveedoresPaginados(
                pagina, tamanio, ordenarPor, direccion);

        Map<String, Object> estadisticas = pedidoProveedorService.obtenerEstadisticas();

        model.addAttribute("pedidos", pedidosPage.getContent());
        model.addAttribute("pedidosRecientes", pedidoProveedorService.obtenerPedidosRecientes());
        model.addAttribute("pedidosUrgentes", pedidoProveedorService.obtenerPedidosUrgentes());
        model.addAttribute("paginaActual", pagina);
        model.addAttribute("totalPaginas", pedidosPage.getTotalPages());
        model.addAttribute("totalElementos", pedidosPage.getTotalElements());
        model.addAttribute("tamanio", tamanio);
        model.addAttribute("ordenarPor", ordenarPor);
        model.addAttribute("direccion", direccion);
        model.addAttribute("estadisticas", estadisticas);
        model.addAttribute("estados", EstadoPedido.values());
        model.addAttribute("prioridades", Prioridad.values());

        return "pedidos-proveedores/lista";
    }

    /**
     * Mostrar formulario para crear nuevo pedido
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        PedidoProveedor pedidoNuevo = new PedidoProveedor();
        pedidoNuevo.setEstado(EstadoPedido.EN_ESPERA);
        pedidoNuevo.setPrioridad(Prioridad.BAJA);

        // Obtener productos activos de proveedores
        List<ProductoProveedor> productosDisponibles = productoProveedorService
                .obtenerPorEstado(ProductoProveedor.EstadoProducto.ACTIVO);

        model.addAttribute("pedido", pedidoNuevo);
        model.addAttribute("productosDisponibles", productosDisponibles);
        model.addAttribute("estados", EstadoPedido.values());
        model.addAttribute("prioridades", Prioridad.values());

        // Agregar listas para los selectores
        if (proveedorService != null) {
            model.addAttribute("proveedores", proveedorService.obtenerTodos());
        }
        if (trabajadorService != null) {
            model.addAttribute("trabajadores", trabajadorService.obtenerTodos());
        }
        if (almacenService != null) {
            model.addAttribute("almacenes", almacenService.obtenerTodos());
        }
        if (metodoEnvioService != null) {
            model.addAttribute("metodosEnvio", metodoEnvioService.obtenerTodos());
        }

        model.addAttribute("titulo", "Crear Pedido Proveedor");
        model.addAttribute("accion", "crear");

        return "pedidos-proveedores/formulario";
    }

    /**
     * Procesar creación de pedido
     */
    @PostMapping("/crear")
    public String crearPedidoProveedor(
            @ModelAttribute PedidoProveedor pedido,
            @RequestParam(value = "productosIds", required = false) List<Long> productosIds,
            @RequestParam(value = "cantidades", required = false) List<Integer> cantidades,
            RedirectAttributes redirectAttributes) {

        try {
            // Validar que se hayan seleccionado productos
            if (productosIds == null || productosIds.isEmpty()) {
                throw new RuntimeException("Debe seleccionar al menos un producto");
            }

            // Preparar lista de productos seleccionados
            List<Map<String, Object>> productosSeleccionados = new ArrayList<>();
            for (int i = 0; i < productosIds.size(); i++) {
                Map<String, Object> productoData = new HashMap<>();
                productoData.put("idProducto", productosIds.get(i));
                productoData.put("cantidad", cantidades.get(i));
                productosSeleccionados.add(productoData);
            }

            pedidoProveedorService.crearPedidoProveedor(pedido, productosSeleccionados);
            redirectAttributes.addFlashAttribute("exito", "Pedido creado exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/pedidos-proveedores/nuevo";
        }

        return "redirect:/pedidos-proveedores";
    }

    /**
     * Mostrar formulario para editar pedido
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model,
                                          RedirectAttributes redirectAttributes) {
        return pedidoProveedorService.obtenerPorId(id)
                .map(pedido -> {
                    // Obtener productos activos de proveedores
                    List<ProductoProveedor> productosDisponibles = productoProveedorService
                            .obtenerPorEstado(ProductoProveedor.EstadoProducto.ACTIVO);

                    model.addAttribute("pedido", pedido);
                    model.addAttribute("productosDisponibles", productosDisponibles);
                    model.addAttribute("estados", EstadoPedido.values());
                    model.addAttribute("prioridades", Prioridad.values());

                    // Agregar listas para los selectores
                    if (proveedorService != null) {
                        model.addAttribute("proveedores", proveedorService.obtenerTodos());
                    }
                    if (trabajadorService != null) {
                        model.addAttribute("trabajadores", trabajadorService.obtenerTodos());
                    }
                    if (almacenService != null) {
                        model.addAttribute("almacenes", almacenService.obtenerTodos());
                    }
                    if (metodoEnvioService != null) {
                        model.addAttribute("metodosEnvio", metodoEnvioService.obtenerTodos());
                    }

                    model.addAttribute("titulo", "Editar Pedido Proveedor");
                    model.addAttribute("accion", "editar");

                    return "pedidos-proveedores/formulario";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Pedido no encontrado");
                    return "redirect:/pedidos-proveedores";
                });
    }

    /**
     * Procesar actualización de pedido
     */
    @PostMapping("/actualizar/{id}")
    public String actualizarPedidoProveedor(
            @PathVariable Long id,
            @ModelAttribute PedidoProveedor pedido,
            @RequestParam(value = "productosIds", required = false) List<Long> productosIds,
            @RequestParam(value = "cantidades", required = false) List<Integer> cantidades,
            RedirectAttributes redirectAttributes) {

        try {
            // Validar que se hayan seleccionado productos
            if (productosIds == null || productosIds.isEmpty()) {
                throw new RuntimeException("Debe seleccionar al menos un producto");
            }

            // Preparar lista de productos seleccionados
            List<Map<String, Object>> productosSeleccionados = new ArrayList<>();
            for (int i = 0; i < productosIds.size(); i++) {
                Map<String, Object> productoData = new HashMap<>();
                productoData.put("idProducto", productosIds.get(i));
                productoData.put("cantidad", cantidades.get(i));
                productosSeleccionados.add(productoData);
            }

            pedidoProveedorService.actualizarPedidoProveedor(id, pedido, productosSeleccionados);
            redirectAttributes.addFlashAttribute("exito", "Pedido actualizado exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/pedidos-proveedores/editar/" + id;
        }

        return "redirect:/pedidos-proveedores";
    }

    /**
     * Ver detalle del pedido
     */
    @GetMapping("/ver/{id}")
    public String verPedidoProveedor(@PathVariable Long id, Model model,
                                     RedirectAttributes redirectAttributes) {
        return pedidoProveedorService.obtenerPorId(id)
                .map(pedido -> {
                    model.addAttribute("pedido", pedido);
                    return "pedidos-proveedores/detalle";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Pedido no encontrado");
                    return "redirect:/pedidos-proveedores";
                });
    }

    /**
     * Eliminar pedido
     */
    @GetMapping("/eliminar/{id}")
    public String eliminarPedidoProveedor(@PathVariable Long id,
                                          RedirectAttributes redirectAttributes) {
        try {
            pedidoProveedorService.eliminarPedidoProveedor(id);
            redirectAttributes.addFlashAttribute("exito", "Pedido eliminado exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos-proveedores";
    }

    /**
     * Cambiar estado del pedido
     */
    @PostMapping("/cambiar-estado/{id}")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam EstadoPedido nuevoEstado,
                                RedirectAttributes redirectAttributes) {
        try {
            pedidoProveedorService.cambiarEstado(id, nuevoEstado);
            redirectAttributes.addFlashAttribute("exito", "Estado cambiado exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos-proveedores";
    }

    /**
     * Filtrar por estado
     */
    @GetMapping("/filtrar/estado/{estado}")
    public String filtrarPorEstado(@PathVariable EstadoPedido estado, Model model) {
        model.addAttribute("pedidos", pedidoProveedorService.obtenerPorEstado(estado));
        model.addAttribute("estadoFiltrado", estado);
        model.addAttribute("estadisticas", pedidoProveedorService.obtenerEstadisticas());
        model.addAttribute("estados", EstadoPedido.values());
        model.addAttribute("prioridades", Prioridad.values());
        return "pedidos-proveedores/lista";
    }

    /**
     * Filtrar por prioridad
     */
    @GetMapping("/filtrar/prioridad/{prioridad}")
    public String filtrarPorPrioridad(@PathVariable Prioridad prioridad, Model model) {
        model.addAttribute("pedidos", pedidoProveedorService.obtenerPorPrioridad(prioridad));
        model.addAttribute("prioridadFiltrada", prioridad);
        model.addAttribute("estadisticas", pedidoProveedorService.obtenerEstadisticas());
        model.addAttribute("estados", EstadoPedido.values());
        model.addAttribute("prioridades", Prioridad.values());
        return "pedidos-proveedores/lista";
    }
}