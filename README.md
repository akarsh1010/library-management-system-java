# Library Management System

A robust Library Management System built in Java using Object-Oriented Programming (OOP) principles and SQLite Database persistence via JDBC. Designed for real-world college library workflows to manage students (members), book inventories, borrowing transactions, and audit history.

## 🚀 Features

### 1. 📚 Book Inventory Management
- **Add Book**: Add books with unique Book ID, Title, and Author.
- **Show All Books**: View all books with their availability status (*Available* / *Issued*).
- **Search Book**: Search book by ID with real-time issue status and borrower details.
- **Delete Book**: Remove books (protected against deleting currently issued books).

### 2. 🧑‍🎓 Student / Member Management
- **Add Student**: Register students with Name, Branch (e.g. BCA, CSE), Year (1–4), Class Roll Number, and a unique 10-digit Phone Number.
- **Composite Uniqueness**: Allows identical roll numbers across different branches or years (e.g. BCA 1st Year Roll 12 and BCA 2nd Year Roll 12 are both allowed), while preventing intra-class duplicates.
- **Phone Number Validation**: Enforces exact 10-digit unique phone numbers across all students.
- **Search & Delete Student**: Accurately targets students using `Branch + Year + Roll Number`.
- **Show All Students**: Complete roster display with contact details.

### 3. 🔄 Issue & Return System (with College Rules)
- **Issue Book**: Links a book to a registered student.
  - **Duplicate Issue Protection**: Prevents issuing already-borrowed books.
  - **Max 3 Books Limit**: Restricts any student from having more than 3 books issued simultaneously.
  - **Auto Issue Date**: Automatically stamps the current date (`dd-MM-yyyy`).
- **Return Book**: Returns the book, marks it *Available*, records the return date, and updates the student's active quota.
- **Currently Issued Books Tracker**: Live audit of all books currently checked out.
- **Issue & Return History**: Permanent transaction log of all past and active borrow records.

### 4. 💾 Persistent Database (SQLite via JDBC)
- All records (books, students, active borrowings, and return history) are permanently saved into a local SQLite database (`library.db`).
- **Zero Server Setup**: Self-contained database requiring zero installation on client machines.
- **Transaction Safety**: ACID-compliant transactions for multi-table updates during issue and return operations.

## 🛠️ Tech / Concepts Used
- **Language**: Java (Core Java / Java 8)
- **Database**: SQLite with JDBC (`sqlite-jdbc`)
- **OOP Principles**: Encapsulation, Composition, Separation of Concerns
- **Collections Framework**: `ArrayList`
- **Date & Time API**: `java.time.LocalDate` and `DateTimeFormatter`
- **Error Handling**: Graceful input scanner recovery (`InputMismatchException`) and SQL transaction rollbacks

## 🏃 How to Run
1. Clone the repository:
   ```bash
   git clone https://github.com/alok0770/library-management-system-java.git
   ```
2. Open the project in IntelliJ IDEA (or any Java IDE).
3. The project dependencies (`lib/sqlite-jdbc.jar`, `lib/slf4j-api.jar`, `lib/slf4j-simple.jar`) are included in the repository.
4. Run [`Main.java`](file:///Users/alokrana/IdeaProjects/student-management-system-java/src/LibraryManagementSystem/Main.java).