package adfdev.erp.demo.Controllers;

import adfdev.erp.demo.Usuario;
import adfdev.erp.demo.database.UserDAO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.sql.SQLException;

@Controller
public class AuthController {

    private final UserDAO userDAO = new UserDAO();

    // ==================== LOGIN ====================

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(
            @RequestParam("usuario") String usuario,
            @RequestParam("password") String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Validar campos vacíos
        if (usuario == null || usuario.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El usuario es obligatorio");
            return "redirect:/login";
        }

        if (password == null || password.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "La contraseña es obligatoria");
            return "redirect:/login";
        }

        try {
            // Intentar iniciar sesión con los datos del formulario
            Usuario usuarioLogueado = userDAO.iniciarSesion(usuario.trim(), password);

            if (usuarioLogueado != null) {
                // LOGIN EXITOSO - Guardar usuario en sesión
                session.setAttribute("usuarioLogueado", usuarioLogueado);
                return "redirect:/dashboard";
            } else {
                // LOGIN FALLIDO - Verificar por qué falló
                boolean existeUsuario = userDAO.existeUsername(usuario.trim());
                boolean existeEmail = userDAO.existeEmail(usuario.trim());

                if (!existeUsuario && !existeEmail) {
                    redirectAttributes.addFlashAttribute("error", "El usuario '" + usuario + "' no existe en el sistema");
                } else {
                    redirectAttributes.addFlashAttribute("error", "La contraseña es incorrecta");
                }
                redirectAttributes.addFlashAttribute("usuarioIngresado", usuario);
                return "redirect:/login";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error de conexión con la base de datos: " + e.getMessage());
            return "redirect:/login";
        }
    }

    // ==================== REGISTRO ====================

    @GetMapping("/registro")
    public String mostrarRegistro() {
        return "registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(
            @RequestParam("usuario") String usuario,
            @RequestParam("correo") String correo,
            @RequestParam("password") String password,
            RedirectAttributes redirectAttributes) {

        // Validar campos vacíos
        if (usuario == null || usuario.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El nombre de usuario es obligatorio");
            redirectAttributes.addFlashAttribute("correoIngresado", correo);
            return "redirect:/registro";
        }

        if (correo == null || correo.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El correo es obligatorio");
            redirectAttributes.addFlashAttribute("usuarioIngresado", usuario);
            return "redirect:/registro";
        }

        if (password == null || password.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "La contraseña es obligatoria");
            redirectAttributes.addFlashAttribute("usuarioIngresado", usuario);
            redirectAttributes.addFlashAttribute("correoIngresado", correo);
            return "redirect:/registro";
        }

        if (password.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 6 caracteres");
            redirectAttributes.addFlashAttribute("usuarioIngresado", usuario);
            redirectAttributes.addFlashAttribute("correoIngresado", correo);
            return "redirect:/registro";
        }

        try {
            // Verificar si el USERNAME ya existe en la base de datos
            if (userDAO.existeUsername(usuario.trim())) {
                redirectAttributes.addFlashAttribute("error", "El nombre de usuario '" + usuario + "' ya está registrado");
                redirectAttributes.addFlashAttribute("correoIngresado", correo);
                return "redirect:/registro";
            }

            // Verificar si el EMAIL ya existe en la base de datos
            if (userDAO.existeEmail(correo.trim())) {
                redirectAttributes.addFlashAttribute("error", "El correo '" + correo + "' ya está registrado");
                redirectAttributes.addFlashAttribute("usuarioIngresado", usuario);
                return "redirect:/registro";
            }

            // REGISTRAR USUARIO EN LA BASE DE DATOS
            boolean registrado = userDAO.registrarUsuario(usuario.trim(), correo.trim(), password);

            if (registrado) {
                // REGISTRO EXITOSO
                redirectAttributes.addFlashAttribute("exito", "¡Cuenta creada exitosamente! Ya puedes iniciar sesión");
                return "redirect:/login";
            } else {
                redirectAttributes.addFlashAttribute("error", "No se pudo crear la cuenta. Intenta de nuevo");
                redirectAttributes.addFlashAttribute("usuarioIngresado", usuario);
                redirectAttributes.addFlashAttribute("correoIngresado", correo);
                return "redirect:/registro";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error de base de datos: " + e.getMessage());
            return "redirect:/registro";
        }
    }

    // ==================== LOGOUT ====================

    @GetMapping("/logout")
    public String cerrarSesion(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("exito", "Sesión cerrada correctamente");
        return "redirect:/login";
    }

    // ==================== DASHBOARD (Página protegida) ====================

    @GetMapping("/dashboard")
    public String mostrarDashboard(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");

        // Si no hay sesión, redirigir al login
        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute("usuario", usuario);
        return "dashboard";
    }

    // ==================== PÁGINA PRINCIPAL ====================

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }
}