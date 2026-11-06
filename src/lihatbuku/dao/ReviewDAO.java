package lihatbuku.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import lihatbuku.koneksi.Koneksi;
import lihatbuku.model.Review;

/**
 * Data Access Object untuk ulasan dan penilaian (tabel: reviews).
 */
public class ReviewDAO {

    public List<Review> getReviewsByBookId(int bookId) {
        List<Review> list = new ArrayList<>();
        Connection conn = Koneksi.getConnection();
        if (conn != null) {
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
                }
            } catch (SQLException e) {
                System.err.println("ReviewDAO.getReviewsByBookId error: " + e.getMessage());
            }
        }
        return list;
    }

    public boolean addReview(Review review) {
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
        return false;
    }

    public double calculateAverageRating(int bookId) {
        List<Review> reviews = getReviewsByBookId(bookId);
        if (reviews.isEmpty()) return 0.0;
        double sum = 0;
        for (Review r : reviews) sum += r.getRating();
        return sum / reviews.size();
    }
}
