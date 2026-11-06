package lihatbuku.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import lihatbuku.koneksi.Koneksi;
import lihatbuku.model.ReadingHistory;

/**
 * Data Access Object untuk penanda riwayat bacaan (tabel: reading_history).
 */
public class ReadingHistoryDAO {

    public List<ReadingHistory> getHistoryByUserId(int userId) {
        List<ReadingHistory> list = new ArrayList<>();
        Connection conn = Koneksi.getConnection();
        if (conn != null) {
            String sql = "SELECT h.id, h.user_id, h.book_id, b.title, b.genre, h.last_page, b.total_pages, h.last_read_at "
                    + "FROM reading_history h "
                    + "JOIN books b ON h.book_id = b.id "
                    + "WHERE h.user_id = ? ORDER BY h.last_read_at DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(new ReadingHistory(
                                rs.getInt("id"),
                                rs.getInt("user_id"),
                                rs.getInt("book_id"),
                                rs.getString("title"),
                                rs.getString("genre"),
                                rs.getInt("last_page"),
                                rs.getInt("total_pages"),
                                rs.getString("last_read_at")
                        ));
                    }
                }
            } catch (SQLException e) {
                System.err.println("ReadingHistoryDAO.getHistoryByUserId error: " + e.getMessage());
            }
        }
        return list;
    }

    public boolean saveOrUpdateHistory(int userId, int bookId, String bookTitle, String bookGenre, int lastPage, int totalPages) {
        Connection conn = Koneksi.getConnection();
        if (conn != null) {
            String sql = "INSERT INTO reading_history (user_id, book_id, last_page, last_read_at) "
                    + "VALUES (?, ?, ?, NOW()) "
                    + "ON DUPLICATE KEY UPDATE last_page = VALUES(last_page), last_read_at = NOW()";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setInt(2, bookId);
                ps.setInt(3, lastPage);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("ReadingHistoryDAO.saveOrUpdateHistory error: " + e.getMessage());
            }
        }
        return false;
    }

    public int getLastReadPage(int userId, int bookId) {
        Connection conn = Koneksi.getConnection();
        if (conn != null) {
            String sql = "SELECT last_page FROM reading_history WHERE user_id = ? AND book_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setInt(2, bookId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("last_page");
                    }
                }
            } catch (SQLException e) {
                System.err.println("ReadingHistoryDAO.getLastReadPage error: " + e.getMessage());
            }
        }
        return 1;
    }
}
