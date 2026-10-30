package lihatbuku.dao;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lihatbuku.koneksi.Koneksi;
import lihatbuku.model.ReadingHistory;

/**
 * Data Access Object untuk penanda riwayat bacaan.
 */
public class ReadingHistoryDAO {

    private static final List<ReadingHistory> mockHistories = new ArrayList<>();

    static {
        mockHistories.add(new ReadingHistory(1, 1, 1,
                "Rekayasa Perangkat Lunak 2 (Praktikum & Pemodelan ERP)", "Teknologi", 34, 240, "Hari ini, 10:15 WIB"));
        mockHistories.add(new ReadingHistory(2, 1, 2,
                "Implementasi Enterprise Resource Planning (ERP) Berbasis Java", "Bisnis", 85, 190, "Kemarin, 19:40 WIB"));
        mockHistories.add(new ReadingHistory(3, 1, 3,
                "Arsitektur Basis Data Relasional & ORM Hibernate", "Teknologi", 112, 215, "14 Okt 2026"));
    }

    public List<ReadingHistory> getHistoryByUserId(int userId) {
        Connection conn = Koneksi.getConnection();
        if (conn != null) {
            List<ReadingHistory> list = new ArrayList<>();
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
                    return list;
                }
            } catch (SQLException e) {
                System.err.println("ReadingHistoryDAO.getHistoryByUserId error: " + e.getMessage());
            }
        }

        // Fallback in-memory
        List<ReadingHistory> userList = new ArrayList<>();
        for (ReadingHistory rh : mockHistories) {
            if (rh.getUserId() == userId) {
                userList.add(rh);
            }
        }
        return userList;
    }

    public boolean saveOrUpdateHistory(int userId, int bookId, String bookTitle, String bookGenre, int lastPage, int totalPages) {
        String now = new SimpleDateFormat("dd MMM yyyy, HH:mm 'WIB'").format(new Date());

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

        // Fallback in-memory
        for (ReadingHistory rh : mockHistories) {
            if (rh.getUserId() == userId && rh.getBookId() == bookId) {
                rh.setLastPage(lastPage);
                rh.setLastReadAt(now);
                return true;
            }
        }
        mockHistories.add(new ReadingHistory(mockHistories.size() + 1, userId, bookId, bookTitle, bookGenre, lastPage, totalPages, now));
        return true;
    }

    public int getLastReadPage(int userId, int bookId) {
        Connection conn = Koneksi.getConnection();
        if (conn != null) {
            String sql = "SELECT last_page FROM reading_history WHERE user_id = ? AND book_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setInt(2, bookId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt("last_page");
                }
            } catch (SQLException e) {
                System.err.println("ReadingHistoryDAO.getLastReadPage error: " + e.getMessage());
            }
        }

        for (ReadingHistory rh : mockHistories) {
            if (rh.getUserId() == userId && rh.getBookId() == bookId) {
                return rh.getLastPage();
            }
        }
        return 1;
    }
}
