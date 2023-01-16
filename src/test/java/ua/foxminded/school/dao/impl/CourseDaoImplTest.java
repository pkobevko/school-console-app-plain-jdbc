package ua.foxminded.school.dao.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

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

class CourseDaoImplTest {
    private static final String TABLE_TEST_INITIALIZATION_SQL_FILE = "test_tables_initialization.sql";
    private static final int TEST_STUDENT_ID = 1;

    private static CourseDao courseDao;
    private static DataSource originalDataSource;
    private static DataSource spyDataSource;

    @BeforeAll
    static void setup() {
        originalDataSource = JdbcUtil.createDefaultInMemoryH2DataSource();
    }

    @BeforeEach
    void init() {
        spyDataSource = Mockito.spy(originalDataSource);
        courseDao = new CourseDaoImpl(spyDataSource);
        createTables(originalDataSource);
    }

    @Test
    void saveAllBatch_shouldThrowNullPointerException_whenPassingNull() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            courseDao.saveAllBatch(null);
        });
    }

    @Test
    void saveAllBatch_shouldThrowDaoOperationException_whenDBError() throws SQLException {
        Mockito.doThrow(new SQLException("Mock testing Exception")).when(spyDataSource).getConnection();
        Exception exception = Assertions.assertThrows(DaoOperationException.class, () -> {
            courseDao.saveAllBatch(Collections.emptyList());
        });

        String expectedMessage = "Error saving courses";
        String actualMessage = exception.getMessage();
        Assertions.assertEquals(actualMessage, expectedMessage);
    }

    @Test
    void saveAllBatch_shouldSaveAllCourses_whenExample1() {
        List<Course> expected = List.of(new Course(1, "Name1", "Descr1"), new Course(2, "Name2", "Descr2"));
        courseDao.saveAllBatch(expected);
        List<Course> actual = courseDao.findAll();
        Assertions.assertEquals(actual, expected);
    }

    @Test
    void findAll_shouldThrowDaoOperationException_whenDBError() throws SQLException {
        Mockito.doThrow(new SQLException("Mock testing Exception")).when(spyDataSource).getConnection();
        Exception exception = Assertions.assertThrows(DaoOperationException.class, () -> {
            courseDao.findAll();
        });

        String expectedMessage = "Error finding courses";
        String actualMessage = exception.getMessage();
        Assertions.assertEquals(actualMessage, expectedMessage);
    }

    @Test
    void findAll_shouldReturnCorrectListWithCourses_whenExample1() {
        List<Course> expected = List.of(new Course(1, "Name1", "Descr1"), new Course(2, "Name2", "Descr2"));
        courseDao.saveAllBatch(expected);
        List<Course> actual = courseDao.findAll();
        Assertions.assertEquals(actual, expected);
    }

    @Test
    void findAllByStudentId_shouldThrowDaoOperationException_whenDBError() throws SQLException {
        Mockito.doThrow(new SQLException("Mock testing Exception")).when(spyDataSource).getConnection();
        Exception exception = Assertions.assertThrows(DaoOperationException.class, () -> {
            courseDao.findAllByStudentId(TEST_STUDENT_ID);
        });

        String expectedMessage = "Error finding courses by student ID: 1";
        String actualMessage = exception.getMessage();
        Assertions.assertEquals(actualMessage, expectedMessage);
    }

    @Test
    void findAllByStudentId_shouldReturnCorrectList_whenExample1() {
        Course course = new Course(1, "Test1", "Test1");
        List<Course> expected = List.of(course);
        courseDao.saveAllBatch(expected);

        StudentDao studentDao = new StudentDaoImpl(originalDataSource);
        Student student = new Student(1, 0, "FirstName", "LastName");
        studentDao.save(student);
        studentDao.assignToCourse(student.getId(), course.getId());

        List<Course> actual = courseDao.findAllByStudentId(student.getId());
        Assertions.assertEquals(expected, actual);
    }

    static void createTables(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            String createTablesSql = FileReader.readWholeFileFromResources(TABLE_TEST_INITIALIZATION_SQL_FILE);
            statement.execute(createTablesSql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
