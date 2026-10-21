package LibraryManagementSystem;

public class IssueRecord {

    private int bookId;
    private String bookTitle;
    private String studentName;
    private String branch;
    private int year;
    private int rollNo;
    private String phoneNumber;
    private String issueDate;
    private String returnDate;

    public IssueRecord(int bookId, String bookTitle, String studentName, String branch, int year, int rollNo, String phoneNumber, String issueDate) {
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.studentName = studentName;
        this.branch = branch;
        this.year = year;
        this.rollNo = rollNo;
        this.phoneNumber = phoneNumber;
        this.issueDate = issueDate;
        this.returnDate = "Currently Issued";
    }

    public int getBookId() {
        return this.bookId;
    }

    public String getBookTitle() {
        return this.bookTitle;
    }

    public String getStudentName() {
        return this.studentName;
    }

    public String getBranch() {
        return this.branch;
    }

    public int getYear() {
        return this.year;
    }

    public int getRollNo() {
        return this.rollNo;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public String getIssueDate() {
        return this.issueDate;
    }

    public String getReturnDate() {
        return this.returnDate;
    }

    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }
}
