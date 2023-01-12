package ua.foxminded.school.ui;

import java.util.List;
import java.util.Scanner;

import javax.sql.DataSource;

import ua.foxminded.school.dao.CourseDao;
import ua.foxminded.school.dao.GroupDao;
import ua.foxminded.school.dao.StudentDao;
import ua.foxminded.school.dao.impl.CourseDaoImpl;
import ua.foxminded.school.dao.impl.GroupDaoImpl;
import ua.foxminded.school.dao.impl.StudentDaoImpl;
import ua.foxminded.school.domain.model.Course;
import ua.foxminded.school.domain.model.Group;
import ua.foxminded.school.domain.model.Student;
import ua.foxminded.school.exception.DaoOperationException;

public class UserInterface {
    private final Scanner scanner;
    private final CourseDao courseDao;
    private final GroupDao groupDao;
    private final StudentDao studentDao;

    public UserInterface(DataSource dataSource) {
        scanner = new Scanner(System.in);
        courseDao = new CourseDaoImpl(dataSource);
        groupDao = new GroupDaoImpl(dataSource);
        studentDao = new StudentDaoImpl(dataSource);
    }

    public void run() {
        boolean exit = false;
        while (!exit) {
            printMainMenu();
            String input = scanner.next();
            System.out.println();
            if (input.equals("1")) {
                findGroups();
            } else if (input.equals("2")) {
                findStudentsByCourseName();
            } else if (input.equals("3")) {
                addNewStudent();
            } else if (input.equals("4")) {
                deleteStudentById();
            } else if (input.equals("5")) {
                addStudentToCourse();
            } else if (input.equals("6")) {
                removeStudentCourse();
            } else if (input.equals("q")) {
                exit = true;
                System.out.println("Exiting...");
            }
        }
        scanner.close();
    }

    private void printMainMenu() {
        System.out.println();
        System.out.println("*** MAIN MENU ***");
        System.out.println("1. Find all groups with less or equals student count");
        System.out.println("2. Find all students related to course with given name");
        System.out.println("3. Add new student");
        System.out.println("4. Delete student by ID");
        System.out.println("5. Add a student to the course (from a list)");
        System.out.println("6. Remove the student from one of his or her courses");
        System.out.println("q. Exit program");
        System.out.print("Enter menu-letter >>> ");
    }

    private void findGroups() {
        System.out.println("Find groups by max. students count: ");
        System.out.print("Enter students count >>> ");
        int studentCount = getNumber();
        try {
            List<Group> groups = groupDao.findAllByStudentsCount(studentCount);
            if (groups.isEmpty()) {
                System.out.println("There are no such groups");
                return;
            }
            printGroups(groups);
        } catch (DaoOperationException e) {
            System.out.println("Cannot find groups: " + e.getMessage());
        }
    }

    private void findStudentsByCourseName() {
        System.out.println("Find students by course name:");
        System.out.print("Enter course name >>> ");
        String courseName = scanner.next();
        try {
            List<Student> students = studentDao.findByCourseName(courseName);

            if (students.isEmpty()) {
                System.out.println("There are no students attending that course. Check course name and try again");
                return;
            }

            System.out.println("Students from course \"" + courseName + "\":");
            printStudents(students);
        } catch (DaoOperationException e) {
            System.out.println("Cannot find students: " + e.getMessage());
        }
    }

    private void addNewStudent() {
        System.out.println("Add new student:");
        System.out.print("Enter first name >>> ");
        String firstName = scanner.next();
        System.out.print("Enter last name >>> ");
        String lastName = scanner.next();

        Student student = new Student();
        student.setFirstName(firstName);
        student.setLastName(lastName);

        try {
            studentDao.save(student);
            System.out.print("Successfully added a new student: ");
            printStudent(student);
        } catch (DaoOperationException e) {
            System.out.println("Cannot add new student:\n" + e.getMessage());
        }
    }

    private void deleteStudentById() {
        System.out.println("Delete student by ID:");
        try {
            printStudents(studentDao.findAll());
            System.out.print("Enter student ID: ");
            int studentId = getNumber();
            studentDao.deleteById(studentId);
            System.out.println("Student was successfully deleted");
        } catch (DaoOperationException e) {
            System.out.println("Cannot delete student by ID\n" + e.getMessage());
        }
    }

    private void addStudentToCourse() {
        System.out.println("Add student to course:");
        try {
            List<Student> students = studentDao.findAll();
            printStudents(students);
            System.out.print("Enter student ID >>> ");
            int studentId = getNumber();

            List<Course> courses = courseDao.findAll();
            printCourses(courses);
            System.out.print("Enter course ID >>> ");
            int courseId = getNumber();

            if (checkIfIdsIsCorrect(studentId, students, courseId, courses)) {
                studentDao.assignToCourse(studentId, courseId);
                System.out.println("Student added to course successfully");
            } else {
                System.out.println("Error, wrong IDs entered. Check IDs and try again");
            }
        } catch (DaoOperationException e) {
            System.out.println("Cannot add student to course\n" + e.getMessage());
        }
    }

    private void removeStudentCourse() {
        System.out.println("Remove student course:");
        try {
            List<Student> students = studentDao.findAll();
            printStudents(students);
            System.out.print("Enter student ID >>> ");
            int studentId = getNumber();

            List<Course> studentCourses = courseDao.findAllByStudentId(studentId);
            printCourses(studentCourses);

            System.out.print("Enter course ID >>> ");
            int courseId = getNumber();

            if (checkIfIdsIsCorrect(studentId, students, courseId, studentCourses)) {
                studentDao.deleteFromCourse(studentId, courseId);
                System.out.println("Student successfully deleted from course");
            } else {
                System.out.println("Error, wrong IDs entered. Check IDs and try again");
            }
        } catch (DaoOperationException e) {
            System.out.println("Cannot remove student from course\n" + e.getMessage());
        }
    }

    private boolean checkIfIdsIsCorrect(int studentId, List<Student> students, int courseId, List<Course> courses) {
        List<Integer> studentsIds = students.stream().map(Student::getId).toList();
        List<Integer> coursesIds = courses.stream().map(Course::getId).toList();
        return studentsIds.contains(studentId) && coursesIds.contains(courseId);
    }

    private int getNumber() {
        int number = 0;
        while (number == 0) {
            try {
                number = scanner.nextInt();
                System.out.println("Number entered: " + number);
            } catch (Exception e) {
                System.out.println("Error! Please enter number!");
            }
        }
        return number;
    }

    private void printGroups(List<Group> groups) {
        System.out.println("List of groups:");
        for (Group group : groups) {
            printGroup(group);
        }
    }

    private void printGroup(Group group) {
        System.out.println(String.format("Group ID: %d | Group name: %s", group.getId(), group.getName()));
    }

    private void printStudents(List<Student> students) {
        System.out.println("List of students:");
        for (Student student : students) {
            printStudent(student);
        }
    }

    private void printStudent(Student student) {
        String groupId = String.valueOf(student.getGroupId());
        if (student.getGroupId() == 0) {
            groupId = "W/O";
        }
        System.out.println(String.format("ID: %d | Group ID: %s | First name: %s | Last name: %s", student.getId(),
                groupId, student.getFirstName(), student.getLastName()));
    }

    private void printCourses(List<Course> courses) {
        System.out.println("List of groups:");
        for (Course course : courses) {
            printCourse(course);
        }
    }

    private void printCourse(Course course) {
        System.out.println(String.format("Course ID: %d | Course name: %s | Course description: %s", course.getId(),
                course.getName(), course.getDescription()));
    }
}