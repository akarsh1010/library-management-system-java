package LibraryManagementSystem;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static LibraryManagementSystem.Main.scanner;

public class BookService {
    ArrayList<Book> books = new ArrayList<>();
    ArrayList<IssueRecord> issueHistory = new ArrayList<>();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public BookService() {
        books.add(new Book(101, "Java Complete Reference", "Herbert Schildt"));
        books.add(new Book(102, "Clean Code", "Robert C. Martin"));
        books.add(new Book(103, "Data Structures & Algorithms", "Mark Allen Weiss"));
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

        Book newBook = new Book(bookId, title, author);
        books.add(newBook);
        System.out.println();
        System.out.println("-> Book saved successfully !! \n");
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
        Book book = findBook(bookId);

        if (book != null) {
            if (book.isIssued()) {
                System.out.println("Cannot delete! This book is currently issued to a student. Return it first.\n");
                return;
            }
            books.remove(book);
            System.out.println("Book deleted successfully !! \n");
        } else {
            System.out.println("Book not found !! \n");
        }
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

        Student student = studentService.findStudent(branch, year, rollNo);
        if (student == null) {
            System.out.println("-> Student NOT FOUND! Please register the student first.\n");
            return;
        }

        // Rule 1: Max 3 Books Limit Check
        int currentIssuedCount = getIssuedBooksCount(student);
        if (currentIssuedCount >= 3) {
            System.out.println("\n❌ LIMIT REACHED! " + student.getName() + " already has 3 books issued.");
            System.out.println("-> Maximum allowed limit is 3 books per student. Please return a book first.\n");
            return;
        }

        // Rule 2: Set Issue Date & link to student
        String today = LocalDate.now().format(DATE_FORMATTER);
        book.setIssued(true);
        book.setIssuedTo(student);
        book.setIssueDate(today);

        // Add to permanent issue history
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

        System.out.println();
        System.out.println("=================================================");
        System.out.println("          BOOK ISSUED SUCCESSFULLY! 🎉          ");
        System.out.println("=================================================");
        System.out.println(" Book Title   : " + book.getTitle());
        System.out.println(" Book ID      : " + book.getBookId());
        System.out.println(" Issued To    : " + student.getName());
        System.out.println(" Class        : " + student.getBranch() + " - Year " + student.getYear() + " (Roll No: " + student.getRollNo() + ")");
        System.out.println(" Phone Number : " + student.getPhoneNumber());
        System.out.println(" Issue Date   : " + today);
        System.out.println(" Total Issued : " + (currentIssuedCount + 1) + " / 3 Books");
        System.out.println("=================================================\n");
    }

    public void returnBook() {
        int bookId = StudentService.getValidInt("-> Enter Book ID to return : ");
        Book book = findBook(bookId);

        if (book == null) {
            System.out.println("-> Book not found in library!\n");
            return;
        }

        if (!book.isIssued()) {
            System.out.println("-> This book is NOT currently issued! It is already available in the library.\n");
            return;
        }

        Student s = book.getIssuedTo();
        String issueDate = book.getIssueDate();
        String returnDate = LocalDate.now().format(DATE_FORMATTER);

        // Update return date in history
        for (IssueRecord record : issueHistory) {
            if (record.getBookId() == bookId && record.getReturnDate().equals("Currently Issued")) {
                record.setReturnDate(returnDate);
                break;
            }
        }

        book.setIssued(false);
        book.setIssuedTo(null);
        book.setIssueDate(null);

        System.out.println();
        System.out.println("=================================================");
        System.out.println("         BOOK RETURNED SUCCESSFULLY! ✅         ");
        System.out.println("=================================================");
        System.out.println(" Book Title   : " + book.getTitle());
        System.out.println(" Book ID      : " + book.getBookId());
        if (s != null) {
            System.out.println(" Returned by  : " + s.getName() + " (" + s.getBranch() + " Year " + s.getYear() + ", Roll: " + s.getRollNo() + ")");
        }
        System.out.println(" Issue Date   : " + (issueDate != null ? issueDate : "N/A"));
        System.out.println(" Return Date  : " + returnDate);
        System.out.println(" Status       : Now Available in Library");
        System.out.println("=================================================\n");
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
