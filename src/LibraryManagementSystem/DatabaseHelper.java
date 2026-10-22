package LibraryManagementSystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public class DatabaseHelper {

    private static final String DB_URL = "jdbc:sqlite:library.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            // 1. Create students table
            String createStudentsTable = """
                CREATE TABLE IF NOT EXISTS students (
                    name TEXT NOT NULL,
                    branch TEXT NOT NULL,
                    year INTEGER NOT NULL,
                    roll_no INTEGER NOT NULL,
                    phone_number TEXT NOT NULL UNIQUE,
                    PRIMARY KEY (branch, year, roll_no)
                );
            """;
            stmt.execute(createStudentsTable);

            // 2. Create books table
            String createBooksTable = """
                CREATE TABLE IF NOT EXISTS books (
                    book_id INTEGER PRIMARY KEY,
                    title TEXT NOT NULL,
                    author TEXT NOT NULL,
                    is_issued INTEGER DEFAULT 0,
                    issued_branch TEXT,
                    issued_year INTEGER,
                    issued_roll_no INTEGER,
                    issue_date TEXT
                );
            """;
            stmt.execute(createBooksTable);

            // 3. Create issue_records table
            String createIssueRecordsTable = """
                CREATE TABLE IF NOT EXISTS issue_records (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    book_id INTEGER NOT NULL,
                    book_title TEXT NOT NULL,
                    student_name TEXT NOT NULL,
                    branch TEXT NOT NULL,
                    year INTEGER NOT NULL,
                    roll_no INTEGER NOT NULL,
                    phone_number TEXT NOT NULL,
                    issue_date TEXT NOT NULL,
                    return_date TEXT NOT NULL
                );
            """;
            stmt.execute(createIssueRecordsTable);

        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }
}
