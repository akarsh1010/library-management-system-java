package LibraryManagementSystem;

public class Book {

    private int bookId;
    private String title;
    private String author;
    private boolean isIssued;

    public Book(int bookId, String title, String author ) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.isIssued = false;
    }

    public int getBookId() {
        return this.bookId;
    }

    public String getTitle() {
        return this.title;
    }

    public String getAuthor() {
        return this.author;
    }

    public boolean isIssued() {
        return this.isIssued;
    }

    public void setIssued(boolean issued) {
        this.isIssued = issued;
    }
}
