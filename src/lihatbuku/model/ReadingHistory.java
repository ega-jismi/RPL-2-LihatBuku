package lihatbuku.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model penanda nomor halaman terakhir dibaca pengguna terdaftar.
 */
@Entity
@Table(name = "reading_history", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "book_id"})
})
public class ReadingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "user_id", nullable = false)
    private int userId;

    @Column(name = "book_id", nullable = false)
    private int bookId;

    @Transient
    private String bookTitle;

    @Transient
    private String bookGenre;

    @Column(name = "last_page", nullable = false)
    private int lastPage;

    @Transient
    private int totalPages;

    @Column(name = "last_read_at", nullable = false)
    private LocalDateTime lastReadAt = LocalDateTime.now();

    public ReadingHistory() {
        this.lastReadAt = LocalDateTime.now();
    }

    public ReadingHistory(int id, int userId, int bookId, String bookTitle, String bookGenre, int lastPage, int totalPages, String lastReadAt) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.bookGenre = bookGenre;
        this.lastPage = lastPage;
        this.totalPages = totalPages;
        setLastReadAt(lastReadAt);
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getBookGenre() { return bookGenre; }
    public void setBookGenre(String bookGenre) { this.bookGenre = bookGenre; }

    public int getLastPage() { return lastPage; }
    public void setLastPage(int lastPage) { this.lastPage = lastPage; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public String getLastReadAt() {
        if (lastReadAt == null) return "";
        return lastReadAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public void setLastReadAt(String lastReadAtStr) {
        if (lastReadAtStr == null || lastReadAtStr.trim().isEmpty()) {
            this.lastReadAt = LocalDateTime.now();
            return;
        }
        try {
            String clean = lastReadAtStr.trim().replace(" ", "T");
            if (clean.length() == 16) clean += ":00";
            this.lastReadAt = LocalDateTime.parse(clean);
        } catch (Exception e) {
            this.lastReadAt = LocalDateTime.now();
        }
    }

    public LocalDateTime getLastReadAtDateTime() { return lastReadAt; }
    public void setLastReadAtDateTime(LocalDateTime lastReadAt) { this.lastReadAt = lastReadAt; }

    public int getProgressPercentage() {
        if (totalPages <= 0) return 0;
        return (int) Math.round(((double) lastPage / totalPages) * 100);
    }
}
