package lihatbuku.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import lihatbuku.koneksi.Koneksi;
import lihatbuku.model.Book;

/**
 * Data Access Object untuk entitas buku (tabel: books).
 */
public class BookDAO {

    public List<Book> getAllBooks() {
        List<Book> list = new ArrayList<>();
        Connection conn = Koneksi.getConnection();
        if (conn != null) {
            String sql = "SELECT id, title, author, genre, total_pages, synopsis, pdf_path, cover_path, rating, review_count FROM books";
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(new Book(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getString("genre"),
                            rs.getInt("total_pages"),
                            rs.getString("synopsis"),
                            rs.getString("pdf_path"),
                            rs.getString("cover_path"),
                            rs.getDouble("rating"),
                            rs.getInt("review_count")
                    ));
                }
            } catch (SQLException e) {
                System.err.println("BookDAO.getAllBooks error: " + e.getMessage());
            }
        }
        return list;
    }

    public List<Book> searchBooks(String query, String genre) {
        List<Book> list = new ArrayList<>();
        String q = (query == null) ? "" : query.trim().toLowerCase();
        String g = (genre == null) ? "Semua Kategori" : genre.trim();

        Connection conn = Koneksi.getConnection();
        if (conn != null) {
            StringBuilder sql = new StringBuilder("SELECT id, title, author, genre, total_pages, synopsis, pdf_path, cover_path, rating, review_count FROM books WHERE 1=1 ");
            List<String> params = new ArrayList<>();

            if (!q.isEmpty()) {
                sql.append("AND (LOWER(title) LIKE ? OR LOWER(author) LIKE ?) ");
                params.add("%" + q + "%");
                params.add("%" + q + "%");
            }
            if (!g.equals("Semua Kategori")) {
                sql.append("AND genre = ? ");
                params.add(g);
            }

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setString(i + 1, params.get(i));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(new Book(
                                rs.getInt("id"),
                                rs.getString("title"),
                                rs.getString("author"),
                                rs.getString("genre"),
                                rs.getInt("total_pages"),
                                rs.getString("synopsis"),
                                rs.getString("pdf_path"),
                                rs.getString("cover_path"),
                                rs.getDouble("rating"),
                                rs.getInt("review_count")
                        ));
                    }
                }
            } catch (SQLException e) {
                System.err.println("BookDAO.searchBooks error: " + e.getMessage());
            }
        }
        return list;
    }

    public Book getBookById(int id) {
        Connection conn = Koneksi.getConnection();
        if (conn != null) {
            String sql = "SELECT id, title, author, genre, total_pages, synopsis, pdf_path, cover_path, rating, review_count FROM books WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new Book(
                                rs.getInt("id"),
                                rs.getString("title"),
                                rs.getString("author"),
                                rs.getString("genre"),
                                rs.getInt("total_pages"),
                                rs.getString("synopsis"),
                                rs.getString("pdf_path"),
                                rs.getString("cover_path"),
                                rs.getDouble("rating"),
                                rs.getInt("review_count")
                        );
                    }
                }
            } catch (SQLException e) {
                System.err.println("BookDAO.getBookById error: " + e.getMessage());
            }
        }
        return null;
    }

    public void updateRating(int bookId, double newRating, int newReviewCount) {
        Connection conn = Koneksi.getConnection();
        if (conn != null) {
            String sql = "UPDATE books SET rating = ?, review_count = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDouble(1, newRating);
                ps.setInt(2, newReviewCount);
                ps.setInt(3, bookId);
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("BookDAO.updateRating error: " + e.getMessage());
            }
        }
    }

    public boolean addBook(Book book) {
        Connection conn = Koneksi.getConnection();
        if (conn != null) {
            String sql = "INSERT INTO books (title, author, genre, total_pages, synopsis, pdf_path, cover_path, rating, review_count) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, book.getTitle());
                ps.setString(2, book.getAuthor());
                ps.setString(3, book.getGenre());
                ps.setInt(4, book.getTotalPages());
                ps.setString(5, book.getSynopsis());
                ps.setString(6, book.getPdfPath());
                ps.setString(7, book.getCoverPath());
                ps.setDouble(8, book.getRating());
                ps.setInt(9, book.getReviewCount());
                int affected = ps.executeUpdate();
                if (affected > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            book.setId(rs.getInt(1));
                        }
                    }
                    return true;
                }
            } catch (SQLException e) {
                System.err.println("BookDAO.addBook error: " + e.getMessage());
            }
        }
        return false;
    }

    public boolean updateBook(Book book) {
        Connection conn = Koneksi.getConnection();
        if (conn != null) {
            String sql = "UPDATE books SET title = ?, author = ?, genre = ?, total_pages = ?, synopsis = ?, "
                    + "pdf_path = ?, cover_path = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, book.getTitle());
                ps.setString(2, book.getAuthor());
                ps.setString(3, book.getGenre());
                ps.setInt(4, book.getTotalPages());
                ps.setString(5, book.getSynopsis());
                ps.setString(6, book.getPdfPath());
                ps.setString(7, book.getCoverPath());
                ps.setInt(8, book.getId());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("BookDAO.updateBook error: " + e.getMessage());
            }
        }
        return false;
    }

    public boolean deleteBook(int bookId) {
        Connection conn = Koneksi.getConnection();
        if (conn != null) {
            try {
                try (PreparedStatement ps1 = conn.prepareStatement("DELETE FROM reading_history WHERE book_id = ?")) {
                    ps1.setInt(1, bookId);
                    ps1.executeUpdate();
                }
                try (PreparedStatement ps2 = conn.prepareStatement("DELETE FROM reviews WHERE book_id = ?")) {
                    ps2.setInt(1, bookId);
                    ps2.executeUpdate();
                }
                try (PreparedStatement ps3 = conn.prepareStatement("DELETE FROM books WHERE id = ?")) {
                    ps3.setInt(1, bookId);
                    return ps3.executeUpdate() > 0;
                }
            } catch (SQLException e) {
                System.err.println("BookDAO.deleteBook error: " + e.getMessage());
            }
        }
        return false;
    }
}
