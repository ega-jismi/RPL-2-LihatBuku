package lihatbuku.model;

/**
 * Model evaluasi penilaian rating bintang dan teks ulasan pembaca.
 */
public class Review {
    private int id;
    private int bookId;
    private int userId;
    private String userName;
    private int rating; // 1 - 5
    private String comment;
    private String createdAt;

    public Review() {}

    public Review(int id, int bookId, int userId, String userName, int rating, String comment, String createdAt) {
        this.id = id;
        this.bookId = bookId;
        this.userId = userId;
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
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

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
