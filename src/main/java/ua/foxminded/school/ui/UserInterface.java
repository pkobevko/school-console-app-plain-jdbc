package ua.foxminded.school.ui;

import java.util.List;
import java.util.Optional;
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

public class UserInterface {
    private final Scanner scanner;
    private final CourseDao courseDao;
    private final GroupDao groupDao;
    private final StudentDao studentDao;

    public UserInterface(DataSource dataSource) {
        scanner = new Scanner(System.in);
        ;
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
                findGroupsByEqualOrLessStudentsCount();
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

    private void findGroupsByEqualOrLessStudentsCount() {
        System.out.println("Find groups by max. students count: ");
        System.out.print("Enter students count >>> ");
        int studentCount = getNumber();

        System.out.println("List of groups:");
        List<Group> groups = groupDao.findAllByEqualOrLessStudentsCount(studentCount);
        printGroups(groups);
    }

    private void findStudentsByCourseName() {
        System.out.println("Find students by course name:");
        System.out.print("Enter course name >>> ");
        String courseName = scanner.next();

        Optional<Course> courseOpt = courseDao.findByName(courseName);
        courseOpt.ifPresentOrElse(course -> {
            System.out.println("Students from course \"" + course.getName() + "\":");
            List<Student> students = studentDao.findAllByCourseName(course.getName());
            printStudents(students);
        }, () -> {
            System.out.println("Course with given name doesnt exist. Check course name and try again");
        });
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

        if (studentDao.save(student)) {
            System.out.print("Successfully added a new student: ");
            printStudent(student);
        } else {
            System.out.println("Student was not saved. Please, try again");
        }
    }

    private void deleteStudentById() {
        System.out.println("Delete student by ID:");
        printStudents(studentDao.findAll());
        System.out.print("Enter student ID: ");
        int studentId = getNumber();
        if (studentDao.deleteById(studentId)) {
            System.out.println("Student was successfully deleted");
        } else {
            System.out.println("Student was not deleted. Check student ID and try again");
        }
    }

    private void addStudentToCourse() {
        System.out.println("Add student to course:");
        List<Student> students = studentDao.findAll();
        printStudents(students);
        System.out.print("Enter student ID >>> ");
        int studentId = getNumber();

        List<Course> courses = courseDao.findAll();
        printCourses(courses);
        System.out.print("Enter course ID >>> ");
        int courseId = getNumber();

        if (studentDao.assignToCourse(studentId, courseId)) {
            System.out.println("Student added to course successfully");
        } else {
            System.out.println("Student was not added to course. Check IDs and try again");
        }
    }

    private void removeStudentCourse() {
        System.out.println("Remove student course:");
        List<Student> students = studentDao.findAll();
        printStudents(students);
        System.out.print("Enter student ID >>> ");
        int studentId = getNumber();

        List<Course> studentCourses = courseDao.findAllByStudentId(studentId);
        printCourses(studentCourses);

        System.out.print("Enter course ID >>> ");
        int courseId = getNumber();

        if (studentDao.deleteFromCourse(studentId, courseId)) {
            System.out.println("Student successfully deleted from course");
        } else {
            System.out.println("Student was not deleted from course. Check IDs and try again");
        }
    }

    private int getNumber() {
        boolean numberIsIncorrect = true;
        int number = 0;
        while (numberIsIncorrect) {
            try {
                number = Integer.parseInt(scanner.next());
                System.out.println("Number entered: " + number);
                numberIsIncorrect = false;
            } catch (Exception e) {
                System.out.print("Error! Please enter number >>> ");
            }
        }
        return number;
    }

    private void printGroups(List<Group> groups) {
        for (Group group : groups) {
            printGroup(group);
        }
    }

    private void printGroup(Group group) {
        System.out.println(String.format("Group ID: %d | Group name: %s", group.getId(), group.getName()));
    }

    private void printStudents(List<Student> students) {
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
        for (Course course : courses) {
            printCourse(course);
        }
    }

    private void printCourse(Course course) {
        System.out.println(String.format("Course ID: %d | Course name: %s | Course description: %s", course.getId(),
                course.getName(), course.getDescription()));
    }
}