package lihatbuku.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import lihatbuku.koneksi.Koneksi;
import lihatbuku.model.User;

/**
 * Data Access Object untuk entitas pengguna.
 */
public class UserDAO {

    // Fallback in-memory ketika server MySQL offline
    private static final List<User> mockUsers = new ArrayList<>();

    static {
        mockUsers.add(new User(1, "ega_jismi", "12345", "Ega Jismi Saputra", "REGISTERED"));
        mockUsers.add(new User(2, "budi_s", "12345", "Budi Santoso", "REGISTERED"));
        mockUsers.add(new User(3, "siti_r", "12345", "Siti Rahma", "REGISTERED"));
    }

    public User login(String username, String password) {
        Connection conn = Koneksi.getConnection();
        if (conn != null) {
            String sql = "SELECT id, username, password, full_name, role FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, password);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new User(
                                rs.getInt("id"),
                                rs.getString("username"),
                                rs.getString("password"),
                                rs.getString("full_name"),
                                rs.getString("role")
                        );
                    }
                }
            } catch (SQLException e) {
                System.err.println("UserDAO.login error: " + e.getMessage());
            }
        }

        // Fallback in-memory
        for (User u : mockUsers) {
            if (u.getUsername().equalsIgnoreCase(username) && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }

    public boolean register(User user) {
        Connection conn = Koneksi.getConnection();
        if (conn != null) {
            String sql = "INSERT INTO users (username, password, full_name, role) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getPassword());
                ps.setString(3, user.getFullName());
                ps.setString(4, user.getRole() != null ? user.getRole() : "REGISTERED");
                int affected = ps.executeUpdate();
                if (affected > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) user.setId(rs.getInt(1));
                    }
                    return true;
                }
            } catch (SQLException e) {
                System.err.println("UserDAO.register error: " + e.getMessage());
            }
        }

        // Fallback in-memory
        user.setId(mockUsers.size() + 1);
        user.setRole("REGISTERED");
        mockUsers.add(user);
        return true;
    }

    public boolean isUsernameTaken(String username) {
        Connection conn = Koneksi.getConnection();
        if (conn != null) {
            String sql = "SELECT id FROM users WHERE username = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            } catch (SQLException e) {
                System.err.println("UserDAO.isUsernameTaken error: " + e.getMessage());
            }
        }

        for (User u : mockUsers) {
            if (u.getUsername().equalsIgnoreCase(username)) return true;
        }
        return false;
    }
}
