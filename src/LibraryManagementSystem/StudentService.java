package LibraryManagementSystem;

import java.util.ArrayList;
import java.util.InputMismatchException;

import static LibraryManagementSystem.Main.scanner;

public class StudentService {
    ArrayList<Student> students = new ArrayList<>();

    public StudentService() {
        students.add(new Student("RAHUL SHARMA", 12, "BCA", 2, "9876543210"));
        students.add(new Student("AMAN VERMA", 15, "CSE", 3, "9812345678"));
        students.add(new Student("PRIYA SINGH", 5, "BCA", 1, "9765432109"));
    }

    public void addStudent() {
        System.out.print("-> Enter Student name : ");
        String name = scanner.nextLine().toUpperCase().trim();

        System.out.print("-> Enter Branch (e.g. BCA, CSE) : ");
        String branch = scanner.nextLine().toUpperCase().trim();

        int year;
        while (true) {
            year = getValidInt("-> Enter Year (1-4) : ");
            if (year >= 1 && year <= 4) {
                break;
            } else {
                System.out.println("-> Please enter a valid year between 1 and 4!\n");
            }
        }

        int rollNo;
        while (true) {
            rollNo = getValidInt("-> Enter the roll number : ");
            if (isStudentExists(branch, year, rollNo)) {
                System.out.println("-> Student with Roll No " + rollNo + " already exists in " + branch + " Year " + year + "!\n");
            } else {
                break;
            }
        }

        String phoneNumber;
        while (true) {
            System.out.print("-> Enter 10-digit Phone Number : ");
            phoneNumber = scanner.nextLine().trim();
            if (phoneNumber.length() != 10 || !phoneNumber.matches("\\d+")) {
                System.out.println("-> Invalid Phone Number! Please enter exactly 10 digits.\n");
            } else if (isPhoneNumberExists(phoneNumber)) {
                System.out.println("-> Phone number already exists! Please enter a unique phone number.\n");
            } else {
                break;
            }
        }

        Student newStudent = new Student(name, rollNo, branch, year, phoneNumber);
        students.add(newStudent);
        System.out.println();
        System.out.println("-> Student saved successfully !! \n");
    }

    public void searchStudent() {
        System.out.print("-> Enter Branch : ");
        String branch = scanner.nextLine().toUpperCase().trim();

        int year = getValidInt("-> Enter Year : ");
        int rollNo = getValidInt("-> Enter Roll Number : ");

        Student student = findStudent(branch, year, rollNo);
        if (student != null) {
            System.out.println(" Name         : " + student.getName()
                    + "\n Branch       : " + student.getBranch()
                    + "\n Year         : " + student.getYear()
                    + "\n Roll No      : " + student.getRollNo()
                    + "\n Phone Number : " + student.getPhoneNumber());
            System.out.println();
        } else {
            System.out.println("\nStudent not exist : \n");
        }
    }

    public void showAll() {
        if (students.isEmpty()) {
            System.out.println("No Students found !! \n");
        } else {
            for (Student student : students) {
                System.out.println(" Name         : " + student.getName()
                        + "\n Branch       : " + student.getBranch()
                        + "\n Year         : " + student.getYear()
                        + "\n Roll No      : " + student.getRollNo()
                        + "\n Phone Number : " + student.getPhoneNumber());
                System.out.println();
            }
            System.out.println();
        }
    }

    public void deleteStudent() {
        System.out.print("-> Enter Branch : ");
        String branch = scanner.nextLine().toUpperCase().trim();

        int year = getValidInt("-> Enter Year : ");
        int rollNo = getValidInt("-> Enter Roll Number : ");

        boolean found = false;
        for (int i = 0; i < students.size(); i++) {
            Student s = students.get(i);
            if (s.getBranch().equalsIgnoreCase(branch) && s.getYear() == year && s.getRollNo() == rollNo) {
                students.remove(i);
                System.out.println("Student deleted successfully !! \n");
                found = true;
                break;
            }
        }

        if (!found) {
            System.out.println("Student not found : ");
        }
    }

    public static int getValidInt(String message) {
        int value;
        while (true) {
            try {
                System.out.print(message);
                value = scanner.nextInt();
                scanner.nextLine();
                break;
            } catch (InputMismatchException e) {
                System.out.println("Invalid Input !! ");
                scanner.nextLine();
            }
        }
        return value;
    }

    public boolean isStudentExists(String branch, int year, int rollNo) {
        for (Student s : students) {
            if (s.getBranch().equalsIgnoreCase(branch)
                    && s.getYear() == year
                    && s.getRollNo() == rollNo) {
                return true;
            }
        }
        return false;
    }

    public Student findStudent(String branch, int year, int rollNo) {
        for (Student s : students) {
            if (s.getBranch().equalsIgnoreCase(branch)
                    && s.getYear() == year
                    && s.getRollNo() == rollNo) {
                return s;
            }
        }
        return null;
    }

    public boolean isPhoneNumberExists(String phoneNumber) {
        for (Student s : students) {
            if (s.getPhoneNumber().equals(phoneNumber)) {
                return true;
            }
        }
        return false;
    }
}
