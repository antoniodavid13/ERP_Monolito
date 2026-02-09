package adfdev.erp.demo.database;

import adfdev.erp.demo.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    /**
     * REGISTRAR USUARIO EN LA BASE DE DATOS
     * Recibe la contraseña ya hasheada con SHA-256 desde el Controller
     */
    public boolean registrarUsuario(String username, String email, String passwordHasheada) throws SQLException {
        String sql = "INSERT INTO usuarios (username, email, password) VALUES (?, ?, ?)";

        try (Connection conn = database.getConection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, passwordHasheada);

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    /**
     * INICIAR SESIÓN
     * Compara directamente el email y el hash SHA-256 en la base de datos.
     */
    public Usuario iniciarSesion(String email, String passwordHasheada) throws SQLException {
        // Al usar SHA-256 (que es determinista), comparamos directamente en el WHERE
        String sql = "SELECT id, username, email FROM usuarios WHERE email = ? AND password = ?";

        try (Connection conn = database.getConection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, passwordHasheada);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Si hay resultado, las credenciales son correctas
                    Usuario usuario = new Usuario();
                    usuario.setId(rs.getInt("id"));
                    usuario.setUsername(rs.getString("username"));
                    usuario.setEmail(rs.getString("email"));
                    return usuario;
                }
            }
        }
        return null; // Credenciales incorrectas o usuario no encontrado
    }

    /**
     * VERIFICAR SI EXISTE UN USERNAME
     */
    public boolean existeUsername(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE username = ?";
        try (Connection conn = database.getConection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * VERIFICAR SI EXISTE UN EMAIL
     */
    public boolean existeEmail(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE email = ?";
        try (Connection conn = database.getConection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }
}