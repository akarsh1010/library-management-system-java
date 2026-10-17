# Library Management System

A console-based Library Management System built in Java using Object-Oriented Programming (OOP) principles. Designed for college library workflows to manage students (members), book inventories, and borrowing transactions.

## 🚀 Features

### Current (Member / Student Management)
- **Add Student**: Register a new student with Name, Roll Number, and Branch (includes duplicate roll number validation).
- **Search Student**: Search registered students by their roll number.
- **Delete Student**: Remove a student record using their roll number.
- **Show All Students**: Display details of all registered students.
- **Input Validation**: Graceful exception handling for invalid inputs (`InputMismatchException`) ensuring the application doesn't crash on incorrect user entries.

### 📌 Upcoming Features (Roadmap)
- **Book Inventory Management**: Add, search, view, and remove books (with ISBN/ID, Title, Author, Category).
- **Issue & Return System**: Issue books to registered students and handle book returns.
- **Fine & Due Date Tracking**: Enforce borrowing limits and compute late return fines.

## 🛠️ Tech / Concepts Used
- **Language**: Java (Core Java)
- **OOP Principles**: Classes, Objects, Encapsulation, Separation of Concerns
- **Collections Framework**: `ArrayList`
- **Exception Handling**: Safe input scanner processing (`try-catch`)
- **Design**: Modular code architecture following the DRY (Don't Repeat Yourself) principle

## 🏃 How to Run
1. Clone the repository:
   ```bash
   git clone https://github.com/alok0770/library-management-system-java.git
   ```
2. Open the project in IntelliJ IDEA (or any Java IDE).
3. Navigate to `src/LibraryManagementSystem/Main.java`.
4. Run `Main.java`.