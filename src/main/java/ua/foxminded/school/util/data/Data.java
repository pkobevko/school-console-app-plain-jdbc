package ua.foxminded.school.util.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import ua.foxminded.school.domain.model.Course;
import ua.foxminded.school.domain.model.Group;
import ua.foxminded.school.domain.model.Student;

public class Data {
    private static final int NUMBER_OF_GROUPS = 10;
    private static final int UPPER_LETTER_A_CHARCODE = 65;
    private static final int UPPER_LETTER_Z_CHARCODE = 90;
    private static final int STREAM_SIZE = 2;
    private static final int STUDENTS_AMOUNT = 200;
    private static final int MAX_AMOUNT_COURSES_FOR_STUDENT = 3;
    private static final int MIN_AMOUNT_COURSES_FOR_STUDENT = 1;

    private static final String HYPHEN = "-";
    private final Random random = new Random();

    private final List<String> firstNames;
    private final List<String> lastNames;
    private final List<Course> courses;
    private final List<Integer> studentsInGroup;

    public List<Group> getGroups() {
        List<Group> groups = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_GROUPS; i++) {
            Group group = new Group();
            group.setId(i + 1);
            StringBuilder stringBuilder = new StringBuilder();
            String letters = random.ints(STREAM_SIZE, UPPER_LETTER_A_CHARCODE, UPPER_LETTER_Z_CHARCODE)
                    .mapToObj(letter -> String.valueOf((char) letter)).collect(Collectors.joining());
            String numbers = random.ints(STREAM_SIZE, 0, 9).mapToObj(Integer::toString).collect(Collectors.joining());
            group.setName(stringBuilder.append(letters + HYPHEN + numbers).toString());
            groups.add(group);
        }
        return groups;
    }

    public List<Course> getCourses() {
        return this.courses;
    }

    public List<Student> getStudents(List<Group> groups) {
        List<Student> students = new ArrayList<>();
        for (int i = 0; i < STUDENTS_AMOUNT; i++) {
            Student student = new Student();
            student.setId(i + 1);
            student.setFirstName(getRandomFirstName());
            student.setLastName(getRandomLastName());
            students.add(student);
        }
        return assignStudentsToGroups(students, groups);
    }

    private List<Student> assignStudentsToGroups(List<Student> students, List<Group> groups) {
        List<Student> studentsTemp = new ArrayList<>(students);
        for (Group group : groups) {
            int studentsCount = getRandomStudentsCount();
            for (int i = 0; i < studentsCount; i++) {
                if (studentsCount < studentsTemp.size()) {
                    Student student = getRandomStudent(studentsTemp);
                    assignStudentToGroup(student, group);
                    studentsTemp.remove(student);
                }
            }
        }
        return students;
    }

    private Student getRandomStudent(List<Student> students) {
        return students.get(random.nextInt(students.size()));
    }

    private void assignStudentToGroup(Student student, Group group) {
        student.setGroupId(group.getId());
    }

    private int getRandomStudentsCount() {
        return studentsInGroup.get(random.nextInt(studentsInGroup.size()));
    }

    private String getRandomLastName() {
        return lastNames.get(random.nextInt(lastNames.size()));
    }

    private String getRandomFirstName() {
        return firstNames.get(random.nextInt(firstNames.size()));
    }

    public Map<Student, List<Course>> getStudentsCourses(List<Student> students, List<Course> courses) {
        Map<Student, List<Course>> result = new HashMap<>();
        for (Student student : students) {
            int coursesAmount = getRandomCoursesAmount();
            List<Course> studentCourses = getRandomCourses(coursesAmount, courses);
            result.put(student, studentCourses);
        }
        return result;
    }

    private int getRandomCoursesAmount() {
        return random.nextInt(MAX_AMOUNT_COURSES_FOR_STUDENT) + MIN_AMOUNT_COURSES_FOR_STUDENT;
    }

    private List<Course> getRandomCourses(int coursesAmount, List<Course> courses) {
        List<Course> result = new ArrayList<>();
        List<Course> coursesTemp = new ArrayList<>(courses);
        for (int i = 0; i < coursesAmount; i++) {
            Course randomCourse = getRandomCourse(coursesTemp);
            result.add(randomCourse);
            coursesTemp.remove(randomCourse);
        }
        return result;
    }

    private Course getRandomCourse(List<Course> courses) {
        return courses.get(random.nextInt(courses.size()));
    }

    public Data() {
        this.firstNames = List.of("Amy", "Abigail", "Juliette", "Harlow", "Kaiden", "Zuri", "Julianna", "Easton", "Ari",
                "Aleb", "David", "Natalie", "Laila", "Elizabeth", "Theo", "Jane", "Paisley", "Edwin", "Adalynn",
                "Gabriella");
        this.lastNames = List.of("Bates", "Gill", "Gomez", "Lewis", "Ward", "McCarty", "Hale", "Cooper", "Horton",
                "Woords", "Horton", "Torres", "Harrison", "Jackson", "Watkins", "Rogers", "King", "Collins", "Kim",
                "Thompson");
        this.courses = List.of(new Course(1, "Art", "Art learning"), new Course(2, "Geography", "Geography learning"),
                new Course(3, "English", "English learning"), new Course(4, "Math", "Math learning"),
                new Course(5, "Music", "Music learining"), new Course(6, "Science", "Science learning"),
                new Course(7, "Chemistry", "Chemistry learning"), new Course(8, "Biology", "Biology learning"),
                new Course(9, "Physics", "Physics learning"), new Course(10, "IT", "Information Technology learning"));
        this.studentsInGroup = List.of(0, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
                29, 30);
    }
}
