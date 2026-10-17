package LibraryManagementSystem;

    public class Student {

        private String name;
        private int rollNo;
        private String branch;

        public Student(String name, int rollNo, String branch) {
            this.name = name;
            this.rollNo = rollNo;
            this.branch = branch;
        }

        public String getName() {
            return this.name;
        }

        public int getRollNo() {
            return this.rollNo;
        }

        public String getBranch() {
            return this.branch;
        }
    }

