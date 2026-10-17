package LibraryManagementSystem;

import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    static Scanner scanner = new Scanner(System.in);
    static StudentService service = new StudentService();


    public static void main(String[] args) {



        System.out.println("STUDENT MANAGEMENT SYSTEM ");
        System.out.println("==========================");
        System.out.println();



        while (true) {
            Main.menu();


            System.out.println("-> Hey welcome :) ");

            int choice = 0;



            boolean valid = false;

            while (!valid) {
                try {
                    System.out.print("\n-> Enter your choice : ");
                    choice = scanner.nextInt();
                    scanner.nextLine();
                    valid = true;


                } catch (InputMismatchException e) {
                    scanner.nextLine();
                    System.out.println("-> Invalid choice : ");
                }
            }

            if (choice == 5) {
                System.out.println("Thanks for using :) ");
                break;
            }


            switch (choice) {
                case 1 -> {

                    System.out.println("Adding Student :");
                    System.out.println("--------------\n");
                    service.addStudent();
                }

                case 2 -> {
                    System.out.println("Deleting Student :");
                    System.out.println("--------------\n");
                    service.deleteStudent();
                }

                case 3 ->{
                    System.out.println("Searching :");
                    System.out.println("--------------\n");
                    service.searchStudent();
                }

                case 4 -> {
                    System.out.println("Showing :");
                    System.out.println("--------------\n");
                    service.showAll();
                }

                default -> System.out.println("Invalid choice :\n");
            }
        }


    }

    public static void menu() {


        System.out.println("======================");
        System.out.println("         Menu         ");
        System.out.println("======================");
        System.out.println();

        System.out.println("1. Add Student ");
        System.out.println("2. Delete Student ");
        System.out.println("3. Search Student ");
        System.out.println("4. ShowAll Student ");
        System.out.println("5. Exit :) ");
        System.out.println();
    }
}


