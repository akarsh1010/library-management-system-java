package LibraryManagementSystem;

import java.util.ArrayList;

import static LibraryManagementSystem.Main.scanner;

public class BookService {
    ArrayList<Book> books = new ArrayList<>();

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

        boolean found = false;

        for (Book book : books) {
            if (book.getBookId() == bookId) {
                System.out.println(" Book ID : " + book.getBookId()
                        + "\n Title   : " + book.getTitle()
                        + "\n Author  : " + book.getAuthor()
                        + "\n Status  : " + (book.isIssued() ? "Issued" : "Available"));
                System.out.println();
                found = true;
                break;
            }
        }

        if (!found) {
            System.out.println("\nBook not found !! \n");
        }
    }

    public void showAll() {
        if (books.isEmpty()) {
            System.out.println("No Books found !! \n");
        } else {
            for (Book book : books) {
                System.out.println(" Book ID : " + book.getBookId()
                        + "\n Title   : " + book.getTitle()
                        + "\n Author  : " + book.getAuthor()
                        + "\n Status  : " + (book.isIssued() ? "Issued" : "Available"));
                System.out.println();
            }
            System.out.println();
        }
    }

    public void deleteBook() {
        int bookId = StudentService.getValidInt("-> Enter Book ID : ");
        boolean found = false;

        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getBookId() == bookId) {
                books.remove(i);
                System.out.println("Book deleted successfully !! \n");
                found = true;
                break;
            }
        }

        if (!found) {
            System.out.println("Book not found !! \n");
        }
    }

    public boolean isBookIdExists(int bookId) {
        for (Book book : books) {
            if (book.getBookId() == bookId) {
                return true;
            }
        }
        return false;
    }
}
