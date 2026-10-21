package LibraryManagementSystem;

public class Book {

    private int bookId;
    private String title;
    private String author;
    private boolean isIssued;
    private Student issuedTo;
    private String issueDate;

    public Book(int bookId, String title, String author ) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.isIssued = false;
        this.issuedTo = null;
        this.issueDate = null;
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

    public Student getIssuedTo() {
        return this.issuedTo;
    }

    public void setIssuedTo(Student issuedTo) {
        this.issuedTo = issuedTo;
    }

    public String getIssueDate() {
        return this.issueDate;
    }

    public void setIssueDate(String issueDate) {
        this.issueDate = issueDate;
    }
}
