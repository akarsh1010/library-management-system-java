package LibraryManagementSystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static LibraryManagementSystem.Main.scanner;

public class BookService {
    ArrayList<Book> books = new ArrayList<>();
    ArrayList<IssueRecord> issueHistory = new ArrayList<>();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public BookService(StudentService studentService) {
        loadBooksFromDB(studentService);
        loadIssueHistoryFromDB();
    }

    public BookService() {
        this(null);
    }

    public void loadBooksFromDB(StudentService studentService) {
        books.clear();
        String sql = "SELECT book_id, title, author, is_issued, issued_branch, issued_year, issued_roll_no, issue_date FROM books";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int bookId = rs.getInt("book_id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                int isIssued = rs.getInt("is_issued");
                String branch = rs.getString("issued_branch");
                int year = rs.getInt("issued_year");
                int rollNo = rs.getInt("issued_roll_no");
                String issueDate = rs.getString("issue_date");

                Book book = new Book(bookId, title, author);
                if (isIssued == 1) {
                    book.setIssued(true);
                    book.setIssueDate(issueDate);
                    if (studentService != null) {
                        Student s = studentService.findStudent(branch, year, rollNo);
                        book.setIssuedTo(s);
                    }
                }
                books.add(book);
            }
        } catch (SQLException e) {
            System.err.println("Failed to load books from database: " + e.getMessage());
        }
    }

    public void loadIssueHistoryFromDB() {
        issueHistory.clear();
        String sql = "SELECT book_id, book_title, student_name, branch, year, roll_no, phone_number, issue_date, return_date FROM issue_records ORDER BY id ASC";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int bookId = rs.getInt("book_id");
                String title = rs.getString("book_title");
                String name = rs.getString("student_name");
                String branch = rs.getString("branch");
                int year = rs.getInt("year");
                int rollNo = rs.getInt("roll_no");
                String phone = rs.getString("phone_number");
                String issueDate = rs.getString("issue_date");
                String returnDate = rs.getString("return_date");

                IssueRecord record = new IssueRecord(bookId, title, name, branch, year, rollNo, phone, issueDate);
                record.setReturnDate(returnDate);
                issueHistory.add(record);
            }
        } catch (SQLException e) {
            System.err.println("Failed to load issue history from database: " + e.getMessage());
        }
    }

    public void addBook() {
        int bookId;
        while (true) {
            bookId = StudentService.getValidInt("-> Enter Book ID : ");
            if (isBookIdExists(bookId)) {
                System.out.println("-> Book ID already exists!\n");
            } else {
                break;
            }
        }

        System.out.print("-> Enter Book Title : ");
        String title = scanner.nextLine().trim();

        System.out.print("-> Enter Author Name : ");
        String author = scanner.nextLine().trim();

        StringBuilder error = new StringBuilder();
        if (registerBook(bookId, title, author, error)) {
            System.out.println();
            System.out.println("-> Book saved successfully to database !! \n");
        } else {
            System.out.println("\n-> " + error + "\n");
        }
    }

    public synchronized boolean registerBook(int bookId, String title, String author, StringBuilder errorMsg) {
        if (title == null || title.trim().isEmpty()) {
            errorMsg.append("Book title cannot be empty!");
            return false;
        }
        if (author == null || author.trim().isEmpty()) {
            errorMsg.append("Author name cannot be empty!");
            return false;
        }
        title = title.trim();
        author = author.trim();

        if (isBookIdExists(bookId)) {
            errorMsg.append("Book ID #").append(bookId).append(" already exists!");
            return false;
        }

        String insertSQL = "INSERT INTO books (book_id, title, author, is_issued) VALUES (?, ?, ?, 0)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setInt(1, bookId);
            pstmt.setString(2, title);
            pstmt.setString(3, author);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            errorMsg.append("Failed to save book to database: ").append(e.getMessage());
            return false;
        }

        Book newBook = new Book(bookId, title, author);
        books.add(newBook);
        return true;
    }

    public void searchBook() {
        int bookId = StudentService.getValidInt("-> Enter Book ID : ");
        Book book = findBook(bookId);

        if (book != null) {
            System.out.println(" Book ID    : " + book.getBookId()
                    + "\n Title      : " + book.getTitle()
                    + "\n Author     : " + book.getAuthor()
                    + "\n Status     : " + (book.isIssued() ? "Issued" : "Available"));
            if (book.isIssued() && book.getIssuedTo() != null) {
                Student s = book.getIssuedTo();
                System.out.println(" Issued To  : " + s.getName() + " (" + s.getBranch() + " Year " + s.getYear() + ", Roll: " + s.getRollNo() + ")");
                System.out.println(" Issue Date : " + book.getIssueDate());
            }
            System.out.println();
        } else {
            System.out.println("\nBook not found !! \n");
        }
    }

    public void showAll() {
        if (books.isEmpty()) {
            System.out.println("No Books found !! \n");
        } else {
            for (Book book : books) {
                System.out.println(" Book ID    : " + book.getBookId()
                        + "\n Title      : " + book.getTitle()
                        + "\n Author     : " + book.getAuthor()
                        + "\n Status     : " + (book.isIssued() ? "Issued" : "Available"));
                if (book.isIssued() && book.getIssuedTo() != null) {
                    Student s = book.getIssuedTo();
                    System.out.println(" Issued To  : " + s.getName() + " (" + s.getBranch() + " Year " + s.getYear() + ", Roll: " + s.getRollNo() + ")");
                    System.out.println(" Issue Date : " + book.getIssueDate());
                }
                System.out.println();
            }
            System.out.println();
        }
    }

    public void deleteBook() {
        int bookId = StudentService.getValidInt("-> Enter Book ID : ");
        StringBuilder error = new StringBuilder();
        if (removeBook(bookId, error)) {
            System.out.println("Book deleted successfully from database !! \n");
        } else {
            System.out.println("-> " + error + " \n");
        }
    }

    public synchronized boolean removeBook(int bookId, StringBuilder errorMsg) {
        Book book = findBook(bookId);
        if (book == null) {
            errorMsg.append("Book with ID #").append(bookId).append(" not found!");
            return false;
        }
        if (book.isIssued()) {
            errorMsg.append("Cannot delete! This book is currently issued to a student. Return it first.");
            return false;
        }

        String deleteSQL = "DELETE FROM books WHERE book_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
            pstmt.setInt(1, bookId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            errorMsg.append("Failed to delete book from database: ").append(e.getMessage());
            return false;
        }

        books.remove(book);
        return true;
    }

    public void issueBook(StudentService studentService) {
        int bookId = StudentService.getValidInt("-> Enter Book ID to issue : ");
        Book book = findBook(bookId);

        if (book == null) {
            System.out.println("-> Book not found in library!\n");
            return;
        }

        if (book.isIssued()) {
            Student s = book.getIssuedTo();
            System.out.println("-> Book is ALREADY ISSUED to " + (s != null ? s.getName() : "someone") + " on " + book.getIssueDate() + "!\n");
            return;
        }

        System.out.println("Book Selected: " + book.getTitle() + " by " + book.getAuthor());
        System.out.println("\n-> Enter Student details to issue to:");
        System.out.print("-> Enter Branch : ");
        String branch = scanner.nextLine().toUpperCase().trim();

        int year = StudentService.getValidInt("-> Enter Year : ");
        int rollNo = StudentService.getValidInt("-> Enter Roll Number : ");

        StringBuilder error = new StringBuilder();
        if (issueBookDirect(bookId, branch, year, rollNo, studentService, error)) {
            Student student = studentService.findStudent(branch, year, rollNo);
            int currentIssuedCount = getIssuedBooksCount(student);
            System.out.println();
            System.out.println("=================================================");
            System.out.println("          BOOK ISSUED SUCCESSFULLY! 🎉          ");
            System.out.println("=================================================");
            System.out.println(" Book Title   : " + book.getTitle());
            System.out.println(" Book ID      : " + book.getBookId());
            System.out.println(" Issued To    : " + student.getName());
            System.out.println(" Class        : " + student.getBranch() + " - Year " + student.getYear() + " (Roll No: " + student.getRollNo() + ")");
            System.out.println(" Phone Number : " + student.getPhoneNumber());
            System.out.println(" Issue Date   : " + book.getIssueDate());
            System.out.println(" Total Issued : " + currentIssuedCount + " / 3 Books");
            System.out.println("=================================================\n");
        } else {
            System.out.println("\n❌ " + error + "\n");
        }
    }

    public synchronized boolean issueBookDirect(int bookId, String branch, int year, int rollNo, StudentService studentService, StringBuilder errorMsg) {
        Book book = findBook(bookId);
        if (book == null) {
            errorMsg.append("Book ID #").append(bookId).append(" not found in library!");
            return false;
        }
        if (book.isIssued()) {
            Student s = book.getIssuedTo();
            errorMsg.append("Book is ALREADY ISSUED to ").append(s != null ? s.getName() : "someone").append(" on ").append(book.getIssueDate()).append("!");
            return false;
        }

        Student student = studentService.findStudent(branch, year, rollNo);
        if (student == null) {
            errorMsg.append("Student NOT FOUND! (").append(branch).append(" Year ").append(year).append(", Roll ").append(rollNo).append("). Please register student first.");
            return false;
        }

        int currentIssuedCount = getIssuedBooksCount(student);
        if (currentIssuedCount >= 3) {
            errorMsg.append("LIMIT REACHED! ").append(student.getName()).append(" already has 3 books issued. Max 3 allowed!");
            return false;
        }

        String today = LocalDate.now().format(DATE_FORMATTER);
        String updateBookSQL = "UPDATE books SET is_issued = 1, issued_branch = ?, issued_year = ?, issued_roll_no = ?, issue_date = ? WHERE book_id = ?";
        String insertRecordSQL = "INSERT INTO issue_records (book_id, book_title, student_name, branch, year, roll_no, phone_number, issue_date, return_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'Currently Issued')";

        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmtBook = conn.prepareStatement(updateBookSQL);
                 PreparedStatement pstmtRecord = conn.prepareStatement(insertRecordSQL)) {

                pstmtBook.setString(1, student.getBranch());
                pstmtBook.setInt(2, student.getYear());
                pstmtBook.setInt(3, student.getRollNo());
                pstmtBook.setString(4, today);
                pstmtBook.setInt(5, book.getBookId());
                pstmtBook.executeUpdate();

                pstmtRecord.setInt(1, book.getBookId());
                pstmtRecord.setString(2, book.getTitle());
                pstmtRecord.setString(3, student.getName());
                pstmtRecord.setString(4, student.getBranch());
                pstmtRecord.setInt(5, student.getYear());
                pstmtRecord.setInt(6, student.getRollNo());
                pstmtRecord.setString(7, student.getPhoneNumber());
                pstmtRecord.setString(8, today);
                pstmtRecord.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                errorMsg.append("Database transaction failed: ").append(e.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            errorMsg.append("Database error: ").append(e.getMessage());
            return false;
        }

        book.setIssued(true);
        book.setIssuedTo(student);
        book.setIssueDate(today);

        IssueRecord record = new IssueRecord(
                book.getBookId(),
                book.getTitle(),
                student.getName(),
                student.getBranch(),
                student.getYear(),
                student.getRollNo(),
                student.getPhoneNumber(),
                today
        );
        issueHistory.add(record);
        return true;
    }

    public void returnBook() {
        int bookId = StudentService.getValidInt("-> Enter Book ID to return : ");
        Book book = findBook(bookId);
        Student s = book != null ? book.getIssuedTo() : null;
        String issueDate = book != null ? book.getIssueDate() : null;

        StringBuilder error = new StringBuilder();
        if (returnBookDirect(bookId, error)) {
            String returnDate = LocalDate.now().format(DATE_FORMATTER);
            System.out.println();
            System.out.println("=================================================");
            System.out.println("         BOOK RETURNED SUCCESSFULLY! ✅         ");
            System.out.println("=================================================");
            System.out.println(" Book Title   : " + (book != null ? book.getTitle() : ""));
            System.out.println(" Book ID      : " + bookId);
            if (s != null) {
                System.out.println(" Returned by  : " + s.getName() + " (" + s.getBranch() + " Year " + s.getYear() + ", Roll: " + s.getRollNo() + ")");
            }
            System.out.println(" Issue Date   : " + (issueDate != null ? issueDate : "N/A"));
            System.out.println(" Return Date  : " + returnDate);
            System.out.println(" Status       : Now Available in Library");
            System.out.println("=================================================\n");
        } else {
            System.out.println("\n❌ " + error + "\n");
        }
    }

    public synchronized boolean returnBookDirect(int bookId, StringBuilder errorMsg) {
        Book book = findBook(bookId);
        if (book == null) {
            errorMsg.append("Book ID #").append(bookId).append(" not found in library!");
            return false;
        }

        if (!book.isIssued()) {
            errorMsg.append("This book is NOT currently issued! It is already available in the library.");
            return false;
        }

        String returnDate = LocalDate.now().format(DATE_FORMATTER);
        String updateBookSQL = "UPDATE books SET is_issued = 0, issued_branch = NULL, issued_year = NULL, issued_roll_no = NULL, issue_date = NULL WHERE book_id = ?";
        String updateRecordSQL = "UPDATE issue_records SET return_date = ? WHERE book_id = ? AND return_date = 'Currently Issued'";

        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmtBook = conn.prepareStatement(updateBookSQL);
                 PreparedStatement pstmtRecord = conn.prepareStatement(updateRecordSQL)) {

                pstmtBook.setInt(1, book.getBookId());
                pstmtBook.executeUpdate();

                pstmtRecord.setString(1, returnDate);
                pstmtRecord.setInt(2, book.getBookId());
                pstmtRecord.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                errorMsg.append("Database transaction failed: ").append(e.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            errorMsg.append("Database error: ").append(e.getMessage());
            return false;
        }

        for (IssueRecord record : issueHistory) {
            if (record.getBookId() == bookId && record.getReturnDate().equals("Currently Issued")) {
                record.setReturnDate(returnDate);
                break;
            }
        }

        book.setIssued(false);
        book.setIssuedTo(null);
        book.setIssueDate(null);
        return true;
    }

    public ArrayList<Book> getAllBooks() {
        return new ArrayList<>(books);
    }

    public ArrayList<IssueRecord> getAllHistory() {
        return new ArrayList<>(issueHistory);
    }

    public void showIssuedBooks() {
        boolean hasIssued = false;
        System.out.println("=================================================");
        System.out.println("              CURRENTLY ISSUED BOOKS             ");
        System.out.println("=================================================");

        for (Book book : books) {
            if (book.isIssued()) {
                hasIssued = true;
                Student s = book.getIssuedTo();
                System.out.println(" Book ID      : " + book.getBookId());
                System.out.println(" Title        : " + book.getTitle());
                System.out.println(" Author       : " + book.getAuthor());
                if (s != null) {
                    System.out.println(" Issued To    : " + s.getName());
                    System.out.println(" Branch & Year: " + s.getBranch() + " - Year " + s.getYear() + " (Roll No: " + s.getRollNo() + ")");
                    System.out.println(" Phone Number : " + s.getPhoneNumber());
                }
                System.out.println(" Issue Date   : " + book.getIssueDate());
                System.out.println("-------------------------------------------------");
            }
        }

        if (!hasIssued) {
            System.out.println("No books are currently issued. All books are available! 📚");
            System.out.println("=================================================\n");
        } else {
            System.out.println();
        }
    }

    public void showIssueHistory() {
        if (issueHistory.isEmpty()) {
            System.out.println("No issue/return history found !! \n");
            return;
        }

        System.out.println("=========================================================================================");
        System.out.println("                                ISSUE & RETURN HISTORY                                   ");
        System.out.println("=========================================================================================");

        for (IssueRecord record : issueHistory) {
            System.out.println(" Book         : " + record.getBookTitle() + " (ID: " + record.getBookId() + ")");
            System.out.println(" Student      : " + record.getStudentName() + " [" + record.getBranch() + " Year " + record.getYear() + ", Roll: " + record.getRollNo() + "]");
            System.out.println(" Phone        : " + record.getPhoneNumber());
            System.out.println(" Issued On    : " + record.getIssueDate());
            System.out.println(" Returned On  : " + record.getReturnDate());
            System.out.println("-----------------------------------------------------------------------------------------");
        }
        System.out.println();
    }

    public int getIssuedBooksCount(Student student) {
        int count = 0;
        for (Book book : books) {
            if (book.isIssued() && book.getIssuedTo() != null) {
                Student s = book.getIssuedTo();
                if (s.getBranch().equalsIgnoreCase(student.getBranch())
                        && s.getYear() == student.getYear()
                        && s.getRollNo() == student.getRollNo()) {
                    count++;
                }
            }
        }
        return count;
    }

    public boolean isBookIdExists(int bookId) {
        return findBook(bookId) != null;
    }

    public Book findBook(int bookId) {
        for (Book book : books) {
            if (book.getBookId() == bookId) {
                return book;
            }
        }
        return null;
    }
}
