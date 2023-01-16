package ua.foxminded.school.dao.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import ua.foxminded.school.dao.CourseDao;
import ua.foxminded.school.dao.StudentDao;
import ua.foxminded.school.domain.model.Course;
import ua.foxminded.school.domain.model.Student;
import ua.foxminded.school.exception.DaoOperationException;
import ua.foxminded.school.util.FileReader;
import ua.foxminded.school.util.JdbcUtil;

class StudentDaoImplTest {
    private static final String TABLE_TEST_INITIALIZATION_SQL_FILE = "test_tables_initialization.sql";

    private static StudentDao studentDao;
    private static DataSource originalDataSource;
    private static DataSource spyDataSource;

    @BeforeAll
    static void setup() {
        originalDataSource = JdbcUtil.createDefaultInMemoryH2DataSource();
    }

    @BeforeEach
    void init() {
        spyDataSource = Mockito.spy(originalDataSource);
        studentDao = new StudentDaoImpl(spyDataSource);
        createTables(originalDataSource);
    }

    @Test
    void saveAllBatch_shouldThrowNullPointerException_whenPassingNull() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            studentDao.saveAllBatch(null);
        });
    }

    @Test
    void saveAllBatch_shouldThrowDaoOperationException_whenDBError() throws SQLException {
        Mockito.doThrow(new SQLException("Mock testing Exception")).when(spyDataSource).getConnection();
        Exception exception = Assertions.assertThrows(DaoOperationException.class, () -> {
            studentDao.saveAllBatch(Collections.emptyList());
        });

        String expectedMessage = "Error saving students";
        String actualMessage = exception.getMessage();
        Assertions.assertEquals(actualMessage, expectedMessage);
    }

    @Test
    void saveAllBatch_shouldSaveAllStudents_whenExample1() {
        List<Student> expected = List.of(new Student(1, 0, "FirstName", "LastName"));
        studentDao.saveAllBatch(expected);
        List<Student> actual = studentDao.findAll();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void assignToCoursesBatch_shouldThrowNullPointerException_whenPassingNull() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            studentDao.assignToCoursesBatch(null);
        });
    }

    @Test
    void assignToCoursesBatch_shouldThrowDaoOperationException_whenDBError() throws SQLException {
        Mockito.doThrow(new SQLException("Mock testing Exception")).when(spyDataSource).getConnection();
        Exception exception = Assertions.assertThrows(DaoOperationException.class, () -> {
            studentDao.assignToCoursesBatch(Collections.emptyMap());
        });

        String expectedMessage = "Error assigning students to courses";
        String actualMessage = exception.getMessage();
        Assertions.assertEquals(actualMessage, expectedMessage);
    }

    @Test
    void assignToCoursesBatch_shouldWorkProperly_whenExample1() {
        Student student = new Student(1, 0, "FirstName", "LastName");
        studentDao.save(student);
        Course course = new Course(1, "Name", "Descr");
        List<Course> expected = List.of(course);
        CourseDao courseDao = new CourseDaoImpl(spyDataSource);
        courseDao.saveAllBatch(expected);

        Map<Student, List<Course>> studentCourses = new HashMap<>();
        studentCourses.put(student, expected);
        studentDao.assignToCoursesBatch(studentCourses);

        List<Course> actual = courseDao.findAllByStudentId(student.getId());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void findAllByCourseName_shouldThrowNullPointerException_whenPassingNull() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            studentDao.findAllByCourseName(null);
        });
    }

    @Test
    void findAllByCourseName_shouldThrowDaoOperationException_whenDBError() throws SQLException {
        Mockito.doThrow(new SQLException("Mock testing Exception")).when(spyDataSource).getConnection();
        Exception exception = Assertions.assertThrows(DaoOperationException.class, () -> {
            studentDao.findAllByCourseName("CourseName");
        });

        String expectedMessage = "Error finding students by course name: CourseName";
        String actualMessage = exception.getMessage();
        Assertions.assertEquals(actualMessage, expectedMessage);
    }

    @Test
    void findAllByCourseName_shouldWorkProperly_whenExample1() {
        Student student = new Student(1, 0, "FirstName", "LastName");
        studentDao.save(student);
        Course course = new Course(1, "Name", "Descr");
        CourseDao courseDao = new CourseDaoImpl(spyDataSource);
        courseDao.saveAllBatch(List.of(course));
        studentDao.assignToCourse(student.getId(), course.getId());

        List<Student> expected = List.of(student);
        List<Student> actual = studentDao.findAllByCourseName(course.getName());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void findAllByCourseName_shouldReturnEmptyList_whenCourseWithGivenNameDoesntExist() {
        List<Student> expected = Collections.emptyList();
        List<Student> actual = studentDao.findAllByCourseName("CourseName");

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void save_shouldThrowNullPointerException_whenPassingNull() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            studentDao.save(null);
        });
    }

    @Test
    void save_shouldThrowDaoOperationException_whenDBError() throws SQLException {
        Mockito.doThrow(new SQLException("Mock testing Exception")).when(spyDataSource).getConnection();
        Student student = new Student(1, 0, "FirstName", "LastName");
        Exception exception = Assertions.assertThrows(DaoOperationException.class, () -> {
            studentDao.save(student);
        });

        String expectedMessage = String.format("Error saving student: %s", student);
        String actualMessage = exception.getMessage();
        Assertions.assertEquals(actualMessage, expectedMessage);
    }

    @Test
    void save_shouldWorkProperly_whenExample1() {
        Student student = new Student(1, 0, "FirstName", "LastName");
        studentDao.save(student);

        List<Student> expected = List.of(student);
        List<Student> actual = studentDao.findAll();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void deleteById_shouldThrowDaoOperationException_whenDBError() throws SQLException {
        Mockito.doThrow(new SQLException("Mock testing Exception")).when(spyDataSource).getConnection();
        int studentId = 7;
        Exception exception = Assertions.assertThrows(DaoOperationException.class, () -> {
            studentDao.deleteById(studentId);
        });

        String expectedMessage = String.format("Error deleting student with ID: %d", studentId);
        String actualMessage = exception.getMessage();
        Assertions.assertEquals(actualMessage, expectedMessage);
    }

    @Test
    void deleteById_shouldWorkProperly_whenExample1() {
        Student student = new Student(1, 0, "FirstName", "LastName");
        studentDao.save(student);
        studentDao.deleteById(student.getId());

        List<Student> expected = Collections.emptyList();
        List<Student> actual = studentDao.findAll();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void deleteById_shouldThrowDaoOperationException_whenStudentIdDoestExistInDb() {
        int studentId = 777;
        Exception exception = Assertions.assertThrows(DaoOperationException.class, () -> {
            studentDao.deleteById(studentId);
        });

        String expectedMessage = String.format("Does not exist student with given ID: %d", studentId);
        String actualMessage = exception.getMessage();
        Assertions.assertEquals(actualMessage, expectedMessage);
    }

    @Test
    void findAll_shouldThrowDaoOperationException_whenDBError() throws SQLException {
        Mockito.doThrow(new SQLException("Mock testing Exception")).when(spyDataSource).getConnection();
        Exception exception = Assertions.assertThrows(DaoOperationException.class, () -> {
            studentDao.findAll();
        });

        String expectedMessage = "Error finding students";
        String actualMessage = exception.getMessage();
        Assertions.assertEquals(actualMessage, expectedMessage);
    }

    @Test
    void findAll_shouldReturnCorrectListWithGroups_whenExample1() {
        List<Student> expected = List.of(new Student(1, 0, "FirstName", "LastName"));
        studentDao.saveAllBatch(expected);
        List<Student> actual = studentDao.findAll();
        Assertions.assertEquals(actual, expected);
    }

    @Test
    void assignToCourse_shouldThrowDaoOperationException_whenDBError() throws SQLException {
        Mockito.doThrow(new SQLException("Mock testing Exception")).when(spyDataSource).getConnection();
        int studentId = 777;
        int courseId = 777;
        Exception exception = Assertions.assertThrows(DaoOperationException.class, () -> {
            studentDao.assignToCourse(studentId, courseId);
        });

        String expectedMessage = String.format("Error assigning student with ID: %d to course with ID: %d", studentId,
                courseId);
        String actualMessage = exception.getMessage();
        Assertions.assertEquals(actualMessage, expectedMessage);
    }

    @Test
    void assignToCourse_shouldWorkProperly_whenExample1() {
        Student student = new Student(1, 0, "FirstName", "LastName");
        studentDao.save(student);
        Course course = new Course(1, "Name", "Descr");
        List<Course> expected = List.of(course);
        CourseDao courseDao = new CourseDaoImpl(spyDataSource);
        courseDao.saveAllBatch(expected);

        studentDao.assignToCourse(student.getId(), course.getId());

        List<Course> actual = courseDao.findAllByStudentId(student.getId());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void assignToCourse_shouldThrowDaoOperationException_whenStudentAlreadyHaveGivenCourse() {
        Student student = new Student(1, 0, "FirstName", "LastName");
        studentDao.save(student);
        Course course = new Course(1, "Name", "Descr");
        List<Course> expected = List.of(course);
        CourseDao courseDao = new CourseDaoImpl(spyDataSource);
        courseDao.saveAllBatch(expected);
        studentDao.assignToCourse(student.getId(), course.getId());

        Exception exception = Assertions.assertThrows(DaoOperationException.class, () -> {
            studentDao.assignToCourse(student.getId(), course.getId());
        });

        String expectedMessage = String.format("Error assigning student with ID: %d to course with ID: %d",
                student.getId(), course.getId());
        String actualMessage = exception.getMessage();
        Assertions.assertEquals(actualMessage, expectedMessage);
    }

    @Test
    void deleteFromCourse_shouldThrowDaoOperationException_whenDBError() throws SQLException {
        Mockito.doThrow(new SQLException("Mock testing Exception")).when(spyDataSource).getConnection();
        int studentId = 777;
        int courseId = 777;
        Exception exception = Assertions.assertThrows(DaoOperationException.class, () -> {
            studentDao.deleteFromCourse(studentId, courseId);
        });

        String expectedMessage = String.format("Error deleting student with ID: %d from course with ID: %d", studentId,
                courseId);
        String actualMessage = exception.getMessage();
        Assertions.assertEquals(actualMessage, expectedMessage);
    }

    @Test
    void deleteFromCourse_shouldWorkProperly_whenExample1() {
        Student student = new Student(1, 0, "FirstName", "LastName");
        studentDao.save(student);
        Course course = new Course(1, "Name", "Descr");
        List<Course> courseList = List.of(course);
        CourseDao courseDao = new CourseDaoImpl(spyDataSource);
        courseDao.saveAllBatch(courseList);
        studentDao.assignToCourse(student.getId(), course.getId());

        studentDao.deleteFromCourse(student.getId(), course.getId());

        List<Course> expected = Collections.emptyList();
        List<Course> actual = courseDao.findAllByStudentId(student.getId());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void deleteFromCourse_shouldThrowDaoOperationException_whenStudentDoesntHaveGivenCourse() {
        Student student = new Student(1, 0, "FirstName", "LastName");
        studentDao.save(student);
        int courseId = 777;

        Exception exception = Assertions.assertThrows(DaoOperationException.class, () -> {
            studentDao.deleteFromCourse(student.getId(), courseId);
        });

        String expectedMessage = String.format("Student with ID: %d was not deleted from course with ID: %d",
                student.getId(), courseId);
        String actualMessage = exception.getMessage();
        Assertions.assertEquals(actualMessage, expectedMessage);
    }

    private static void createTables(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            String createTablesSql = FileReader.readWholeFileFromResources(TABLE_TEST_INITIALIZATION_SQL_FILE);
            statement.execute(createTablesSql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
