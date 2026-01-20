package adfdev.erp.demo.Controllers;

import adfdev.erp.demo.PedidoCliente;
import adfdev.erp.demo.PedidoCliente.EstadoPedido;
import adfdev.erp.demo.ProductoCliente;
import adfdev.erp.demo.services.PedidoClienteService;
import adfdev.erp.demo.services.ProductoClienteservice;
import adfdev.erp.demo.services.Clienteservice;
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
@RequestMapping("/pedidos-clientes")
public class PedidoClienteController {

    @Autowired
    private PedidoClienteService pedidoClienteService;

    @Autowired
    private ProductoClienteservice productoClienteService;

    @Autowired(required = false)
    private Clienteservice clienteService;

    @Autowired(required = false)
    private Trabajadorservice trabajadorService;

    @Autowired(required = false)
    private Almacenservice almacenService;

    @Autowired(required = false)
    private MetodoEnvioService metodoEnvioService;

    /**
     * Listar todos los pedidos de clientes
     */
    @GetMapping
    public String listarPedidosClientes(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanio,
            @RequestParam(defaultValue = "id") String ordenarPor,
            @RequestParam(defaultValue = "desc") String direccion,
            Model model) {

        Page<PedidoCliente> pedidosPage = pedidoClienteService.obtenerPedidosClientesPaginados(
                pagina, tamanio, ordenarPor, direccion);

        Map<String, Object> estadisticas = pedidoClienteService.obtenerEstadisticas();

        model.addAttribute("pedidos", pedidosPage.getContent());
        model.addAttribute("pedidosRecientes", pedidoClienteService.obtenerPedidosRecientes());
        model.addAttribute("paginaActual", pagina);
        model.addAttribute("totalPaginas", pedidosPage.getTotalPages());
        model.addAttribute("totalElementos", pedidosPage.getTotalElements());
        model.addAttribute("tamanio", tamanio);
        model.addAttribute("ordenarPor", ordenarPor);
        model.addAttribute("direccion", direccion);
        model.addAttribute("estadisticas", estadisticas);
        model.addAttribute("estados", EstadoPedido.values());

        return "pedidos-clientes/lista";
    }

    /**
     * Mostrar formulario para crear nuevo pedido
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        PedidoCliente pedidoNuevo = new PedidoCliente();
        pedidoNuevo.setEstado(EstadoPedido.EN_ESPERA);

        // Obtener productos activos de clientes
        List<ProductoCliente> productosDisponibles = productoClienteService
                .obtenerPorEstado(ProductoCliente.EstadoProducto.ACTIVO);

        model.addAttribute("pedido", pedidoNuevo);
        model.addAttribute("productosDisponibles", productosDisponibles);
        model.addAttribute("estados", EstadoPedido.values());

        // Agregar listas para los selectores
        if (clienteService != null) {
            model.addAttribute("clientes", clienteService.obtenerTodos());
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

        model.addAttribute("titulo", "Crear Pedido Cliente");
        model.addAttribute("accion", "crear");

        return "pedidos-clientes/formulario";
    }

    /**
     * Procesar creación de pedido
     */
    @PostMapping("/crear")
    public String crearPedidoCliente(
            @ModelAttribute PedidoCliente pedido,
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

            pedidoClienteService.crearPedidoCliente(pedido, productosSeleccionados);
            redirectAttributes.addFlashAttribute("exito", "Pedido creado exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/pedidos-clientes/nuevo";
        }

        return "redirect:/pedidos-clientes";
    }

    /**
     * Mostrar formulario para editar pedido
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model,
                                          RedirectAttributes redirectAttributes) {
        return pedidoClienteService.obtenerPorId(id)
                .map(pedido -> {
                    // Obtener productos activos de clientes
                    List<ProductoCliente> productosDisponibles = productoClienteService
                            .obtenerPorEstado(ProductoCliente.EstadoProducto.ACTIVO);

                    model.addAttribute("pedido", pedido);
                    model.addAttribute("productosDisponibles", productosDisponibles);
                    model.addAttribute("estados", EstadoPedido.values());

                    // Agregar listas para los selectores
                    if (clienteService != null) {
                        model.addAttribute("clientes", clienteService.obtenerTodos());
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

                    model.addAttribute("titulo", "Editar Pedido Cliente");
                    model.addAttribute("accion", "editar");

                    return "pedidos-clientes/formulario";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Pedido no encontrado");
                    return "redirect:/pedidos-clientes";
                });
    }

    /**
     * Procesar actualización de pedido
     */
    @PostMapping("/actualizar/{id}")
    public String actualizarPedidoCliente(
            @PathVariable Long id,
            @ModelAttribute PedidoCliente pedido,
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

            pedidoClienteService.actualizarPedidoCliente(id, pedido, productosSeleccionados);
            redirectAttributes.addFlashAttribute("exito", "Pedido actualizado exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/pedidos-clientes/editar/" + id;
        }

        return "redirect:/pedidos-clientes";
    }

    /**
     * Ver detalle del pedido
     */
    @GetMapping("/ver/{id}")
    public String verPedidoCliente(@PathVariable Long id, Model model,
                                   RedirectAttributes redirectAttributes) {
        return pedidoClienteService.obtenerPorId(id)
                .map(pedido -> {
                    model.addAttribute("pedido", pedido);
                    return "pedidos-clientes/detalle";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Pedido no encontrado");
                    return "redirect:/pedidos-clientes";
                });
    }

    /**
     * Eliminar pedido
     */
    @GetMapping("/eliminar/{id}")
    public String eliminarPedidoCliente(@PathVariable Long id,
                                        RedirectAttributes redirectAttributes) {
        try {
            pedidoClienteService.eliminarPedidoCliente(id);
            redirectAttributes.addFlashAttribute("exito", "Pedido eliminado exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos-clientes";
    }

    /**
     * Cambiar estado del pedido
     */
    @PostMapping("/cambiar-estado/{id}")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam EstadoPedido nuevoEstado,
                                RedirectAttributes redirectAttributes) {
        try {
            pedidoClienteService.cambiarEstado(id, nuevoEstado);
            redirectAttributes.addFlashAttribute("exito", "Estado cambiado exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos-clientes";
    }

    /**
     * Filtrar por estado
     */
    @GetMapping("/filtrar/{estado}")
    public String filtrarPorEstado(@PathVariable EstadoPedido estado, Model model) {
        model.addAttribute("pedidos", pedidoClienteService.obtenerPorEstado(estado));
        model.addAttribute("estadoFiltrado", estado);
        model.addAttribute("estadisticas", pedidoClienteService.obtenerEstadisticas());
        model.addAttribute("estados", EstadoPedido.values());
        return "pedidos-clientes/lista";
    }
}