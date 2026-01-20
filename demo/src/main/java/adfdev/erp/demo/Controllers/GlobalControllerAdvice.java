package adfdev.erp.demo.Controllers;

import adfdev.erp.demo.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute("usuario")
    public Usuario añadirUsuarioAlModelo(HttpSession session) {
        // Esto se ejecuta antes de cualquier @GetMapping de toda la app
        return (Usuario) session.getAttribute("usuarioLogueado");
    }
}