package lihatbuku.dao;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lihatbuku.koneksi.Koneksi;
import lihatbuku.model.Review;

/**
 * Data Access Object untuk ulasan dan penilaian.
 */
public class ReviewDAO {

    private static final List<Review> mockReviews = new ArrayList<>();

    static {
        mockReviews.add(new Review(1, 1, 2, "Budi Santoso", 5,
                "Penjelasan siklus proses bisnis pada Modul ERP minggu ke-1 sangat jelas, terutama keterhubungan tabel penjualan dan persediaan.",
                "12 Okt 2026"));
        mockReviews.add(new Review(2, 1, 3, "Siti Rahma", 4,
                "Materi normalisasi database 3NF dan pemetaan Hibernate sangat membantu saat praktikum di Lab Informatika.",
                "15 Okt 2026"));
        mockReviews.add(new Review(3, 2, 1, "Ega Jismi Saputra", 5,
                "Contoh implementasi arsitektur sistem modular di buku ini sangat aplikatif untuk tugas akhir praktikum RPL 2.",
                "18 Okt 2026"));
    }

    public List<Review> getReviewsByBookId(int bookId) {
        Connection conn = Koneksi.getConnection();
        if (conn != null) {
            List<Review> list = new ArrayList<>();
            String sql = "SELECT r.id, r.book_id, r.user_id, u.full_name, r.rating, r.comment, r.created_at "
                    + "FROM reviews r JOIN users u ON r.user_id = u.id "
                    + "WHERE r.book_id = ? ORDER BY r.created_at DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, bookId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(new Review(
                                rs.getInt("id"),
                                rs.getInt("book_id"),
                                rs.getInt("user_id"),
                                rs.getString("full_name"),
                                rs.getInt("rating"),
                                rs.getString("comment"),
                                rs.getString("created_at")
                        ));
                    }
                    return list;
                }
            } catch (SQLException e) {
                System.err.println("ReviewDAO.getReviewsByBookId error: " + e.getMessage());
            }
        }

        List<Review> filtered = new ArrayList<>();
        for (Review r : mockReviews) {
            if (r.getBookId() == bookId) {
                filtered.add(r);
            }
        }
        return filtered;
    }

    public boolean addReview(Review review) {
        String now = new SimpleDateFormat("dd MMM yyyy").format(new Date());
        review.setCreatedAt(now);

        Connection conn = Koneksi.getConnection();
        if (conn != null) {
            String sql = "INSERT INTO reviews (book_id, user_id, rating, comment, created_at) VALUES (?, ?, ?, ?, NOW())";
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, review.getBookId());
                ps.setInt(2, review.getUserId());
                ps.setInt(3, review.getRating());
                ps.setString(4, review.getComment());
                int affected = ps.executeUpdate();
                if (affected > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) review.setId(rs.getInt(1));
                    }
                    return true;
                }
            } catch (SQLException e) {
                System.err.println("ReviewDAO.addReview error: " + e.getMessage());
            }
        }

        review.setId(mockReviews.size() + 1);
        mockReviews.add(0, review);
        return true;
    }

    public double calculateAverageRating(int bookId) {
        List<Review> reviews = getReviewsByBookId(bookId);
        if (reviews.isEmpty()) return 0.0;
        double sum = 0;
        for (Review r : reviews) sum += r.getRating();
        return sum / reviews.size();
    }
}
