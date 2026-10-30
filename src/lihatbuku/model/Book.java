package lihatbuku.model;

/**
 * Model entitas berkas buku digital PDF.
 */
public class Book {
    private int id;
    private String title;
    private String author;
    private String genre;
    private int totalPages;
    private String synopsis;
    private String pdfPath;
    private String coverPath;
    private double rating;
    private int reviewCount;

    public Book() {}

    public Book(int id, String title, String author, String genre, int totalPages,
                String synopsis, String pdfPath, String coverPath, double rating, int reviewCount) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.totalPages = totalPages;
        this.synopsis = synopsis;
        this.pdfPath = pdfPath;
        this.coverPath = coverPath;
        this.rating = rating;
        this.reviewCount = reviewCount;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public String getSynopsis() { return synopsis; }
    public void setSynopsis(String synopsis) { this.synopsis = synopsis; }

    public String getPdfPath() { return pdfPath; }
    public void setPdfPath(String pdfPath) { this.pdfPath = pdfPath; }

    public String getCoverPath() { return coverPath; }
    public void setCoverPath(String coverPath) { this.coverPath = coverPath; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
}
