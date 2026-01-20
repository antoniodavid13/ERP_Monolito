package adfdev.erp.demo.database;

import adfdev.erp.demo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    /**
     * REGISTRAR USUARIO EN LA BASE DE DATOS
     * Inserta un nuevo usuario si no existe
     */
    public boolean registrarUsuario(String username, String email, String password) throws SQLException {
        String sql = "INSERT INTO usuarios (username, email, password) VALUES (?, ?, ?)";

        try (Connection conn = database.getConection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, password); // En producción usar BCrypt

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    /**
     * INICIAR SESIÓN
     * Busca en la base de datos si existe un usuario con ese username/email y password
     * Retorna el Usuario si las credenciales son correctas, null si no
     */
    public Usuario iniciarSesion(String usernameOrEmail, String password) throws SQLException {
        String sql = "SELECT id, username, email FROM usuarios WHERE (username = ? OR email = ?) AND password = ?";

        try (Connection conn = database.getConection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usernameOrEmail);
            stmt.setString(2, usernameOrEmail);
            stmt.setString(3, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Usuario encontrado - crear objeto y retornar
                    Usuario usuario = new Usuario();
                    usuario.setId(rs.getInt("id"));
                    usuario.setUsername(rs.getString("username"));
                    usuario.setEmail(rs.getString("email"));
                    return usuario;
                }
            }
        }
        // No se encontró usuario con esas credenciales
        return null;
    }

    /**
     * VERIFICAR SI EXISTE UN USERNAME
     * Consulta la base de datos para ver si el username ya está registrado
     */
    public boolean existeUsername(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE username = ?";

        try (Connection conn = database.getConection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * VERIFICAR SI EXISTE UN EMAIL
     * Consulta la base de datos para ver si el email ya está registrado
     */
    public boolean existeEmail(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE email = ?";

        try (Connection conn = database.getConection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * VERIFICAR SI EXISTE USUARIO (por username O email)
     */
    public boolean existeUsuario(String username, String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE username = ? OR email = ?";

        try (Connection conn = database.getConection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}