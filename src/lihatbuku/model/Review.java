package lihatbuku.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model evaluasi penilaian rating bintang dan teks ulasan pembaca.
 */
@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "book_id", nullable = false)
    private int bookId;

    @Column(name = "user_id", nullable = false)
    private int userId;

    @Transient
    private String userName;

    @Column(name = "rating", nullable = false)
    private int rating; // 1 - 5

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Review() {
        this.createdAt = LocalDateTime.now();
    }

    public Review(int id, int bookId, int userId, String userName, int rating, String comment, String createdAt) {
        this.id = id;
        this.bookId = bookId;
        this.userId = userId;
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
        setCreatedAt(createdAt);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getCreatedAt() {
        if (createdAt == null) return "";
        return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public void setCreatedAt(String createdAtStr) {
        if (createdAtStr == null || createdAtStr.trim().isEmpty()) {
            this.createdAt = LocalDateTime.now();
            return;
        }
        try {
            String clean = createdAtStr.trim().replace(" ", "T");
            if (clean.length() == 16) clean += ":00";
            this.createdAt = LocalDateTime.parse(clean);
        } catch (Exception e) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public LocalDateTime getCreatedAtDateTime() { return createdAt; }
    public void setCreatedAtDateTime(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
