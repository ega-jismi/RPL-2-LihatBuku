package lihatbuku.model;

/**
 * Model penanda nomor halaman terakhir dibaca pengguna terdaftar.
 */
public class ReadingHistory {
    private int id;
    private int userId;
    private int bookId;
    private String bookTitle;
    private String bookGenre;
    private int lastPage;
    private int totalPages;
    private String lastReadAt;

    public ReadingHistory() {}

    public ReadingHistory(int id, int userId, int bookId, String bookTitle, String bookGenre, int lastPage, int totalPages, String lastReadAt) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.bookGenre = bookGenre;
        this.lastPage = lastPage;
        this.totalPages = totalPages;
        this.lastReadAt = lastReadAt;
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

    public String getLastReadAt() { return lastReadAt; }
    public void setLastReadAt(String lastReadAt) { this.lastReadAt = lastReadAt; }

    public int getProgressPercentage() {
        if (totalPages <= 0) return 0;
        return (int) Math.round(((double) lastPage / totalPages) * 100);
    }
}
