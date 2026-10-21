package LibraryManagementSystem;

    public class Student {

        private String name;
        private int rollNo;
        private String branch;
        private int year;
        private String phoneNumber;

        public Student(String name, int rollNo, String branch, int year , String phoneNumber) {
            this.name = name;
            this.rollNo = rollNo;
            this.branch = branch;
            this.year = year;
            this.phoneNumber = phoneNumber;
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

        public int getYear(){
            return this.year;
        }

        public String getPhoneNumber(){
            return this.phoneNumber;
        }
    }


