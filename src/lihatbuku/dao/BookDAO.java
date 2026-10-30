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

    private static final List<Book> mockBooks = new ArrayList<>();

    static {
        mockBooks.add(new Book(1, "Rekayasa Perangkat Lunak 2 (Praktikum & Pemodelan ERP)",
                "Roger S. Pressman & Tim Dosen Lab", "Teknologi", 240,
                "Buku panduan komprehensif metodologi pengembangan perangkat lunak, siklus hidup ERP, pemodelan data relasional, dan penjaminan mutu perangkat lunak pada lingkungan laboratorium.",
                "resources/pdf/rpl2_modul_erp.pdf", "resources/covers/cover_rpl2.png", 4.8, 12));

        mockBooks.add(new Book(2, "Implementasi Enterprise Resource Planning (ERP) Berbasis Java",
                "Ian Sommerville", "Bisnis", 190,
                "Kajian terstruktur arsitektur sistem ERP modular berbasis Java desktop dan basis data relasional untuk otomasi alur transaksi inventaris dan keuangan perusahaan.",
                "resources/pdf/implementasi_erp_java.pdf", "resources/covers/cover_erp.png", 4.7, 18));

        mockBooks.add(new Book(3, "Arsitektur Basis Data Relasional & ORM Hibernate",
                "C.J. Date", "Teknologi", 215,
                "Panduan normalisasi skema 3NF, integritas referensial foreign key, transaksi ACID, dan implementasi pemetaan entitas objek Java menggunakan Spring Hibernate.",
                "resources/pdf/basis_data_hibernate.pdf", "resources/covers/cover_db.png", 4.9, 9));

        mockBooks.add(new Book(4, "Desain Antarmuka Pengguna & Interaksi Manusia Komputer",
                "Ben Shneiderman", "Desain", 175,
                "Prinsip usability, konsistensi tata letak visual aplikasi desktop, evaluasi heuristik, dan penyusunan prototipe antarmuka sistem interaktif.",
                "resources/pdf/imk_desain_antarmuka.pdf", "resources/covers/cover_imk.png", 4.5, 6));

        mockBooks.add(new Book(5, "Manajemen Proyek Perangkat Lunak & Jaminan Kualitas",
                "Kathy Schwalbe", "Manajemen", 160,
                "Perencanaan alokasi jadwal pengembangan, analisis matriks risiko, dan prosedur pengujian verifikasi perangkat lunak.",
                "resources/pdf/manajemen_proyek_ti.pdf", "resources/covers/cover_pm.png", 4.6, 14));

        mockBooks.add(new Book(6, "Kecerdasan Buatan & Sistem Temu Kembali Informasi",
                "Stuart Russell", "Sains", 280,
                "Konsep fundamental pengindeksan dokumen teks, temu kembali informasi biner, pemilahan kategori, dan logika berbasis aturan sistem pakar.",
                "resources/pdf/kecerdasan_buatan_nlp.pdf", "resources/covers/cover_ai.png", 4.9, 21));
    }

    public List<Book> getAllBooks() {
        Connection conn = Koneksi.getConnection();
        if (conn != null) {
            List<Book> list = new ArrayList<>();
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
                return list;
            } catch (SQLException e) {
                System.err.println("BookDAO.getAllBooks error: " + e.getMessage());
            }
        }
        return new ArrayList<>(mockBooks);
    }

    public List<Book> searchBooks(String query, String genre) {
        String q = (query == null) ? "" : query.trim().toLowerCase();
        String g = (genre == null) ? "Semua Kategori" : genre.trim();

        Connection conn = Koneksi.getConnection();
        if (conn != null) {
            List<Book> list = new ArrayList<>();
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
                    return list;
                }
            } catch (SQLException e) {
                System.err.println("BookDAO.searchBooks error: " + e.getMessage());
            }
        }

        // Fallback in-memory
        List<Book> result = new ArrayList<>();
        for (Book b : mockBooks) {
            boolean matchQ = q.isEmpty() || b.getTitle().toLowerCase().contains(q) || b.getAuthor().toLowerCase().contains(q);
            boolean matchG = g.equals("Semua Kategori") || b.getGenre().equalsIgnoreCase(g);
            if (matchQ && matchG) {
                result.add(b);
            }
        }
        return result;
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

        for (Book b : mockBooks) {
            if (b.getId() == id) return b;
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

        Book b = getBookById(bookId);
        if (b != null) {
            b.setRating(newRating);
            b.setReviewCount(newReviewCount);
        }
    }
}
