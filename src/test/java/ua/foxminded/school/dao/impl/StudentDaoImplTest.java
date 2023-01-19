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
    void saveAllBatch_shouldReturnFalse_whenDBError() throws SQLException {
        Mockito.doThrow(new SQLException("Mock testing Exception")).when(spyDataSource).getConnection();
        boolean studentsWasSaved = studentDao.saveAllBatch(Collections.emptyList());
        Assertions.assertFalse(studentsWasSaved);
    }

    @Test
    void saveAllBatch_shouldSaveAllStudentsAndReturnTrue_whenExample1() {
        List<Student> expected = List.of(new Student(1, 0, "FirstName", "LastName"));
        boolean studentsWasSaved = studentDao.saveAllBatch(expected);
        List<Student> actual = studentDao.findAll();
        Assertions.assertEquals(expected, actual);
        Assertions.assertTrue(studentsWasSaved);
    }

    @Test
    void assignToCoursesBatch_shouldThrowNullPointerException_whenPassingNull() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            studentDao.assignToCoursesBatch(null);
        });
    }

    @Test
    void assignToCoursesBatch_shouldReturnFalse_whenDBError() throws SQLException {
        Mockito.doThrow(new SQLException("Mock testing Exception")).when(spyDataSource).getConnection();
        boolean studentsWasAssigned = studentDao.assignToCoursesBatch(Collections.emptyMap());
        Assertions.assertFalse(studentsWasAssigned);
    }

    @Test
    void assignToCoursesBatch_shouldAssignStudentsToCoursesAndReturnTrue_whenExample1() {
        Student student = new Student(1, 0, "FirstName", "LastName");
        studentDao.save(student);
        Course course = new Course(1, "Name", "Descr");
        List<Course> expected = List.of(course);
        CourseDao courseDao = new CourseDaoImpl(spyDataSource);
        courseDao.saveAllBatch(expected);

        Map<Student, List<Course>> studentCourses = new HashMap<>();
        studentCourses.put(student, expected);
        boolean studentsWasAssigned = studentDao.assignToCoursesBatch(studentCourses);

        List<Course> actual = courseDao.findAllByStudentId(student.getId());
        Assertions.assertEquals(expected, actual);
        Assertions.assertTrue(studentsWasAssigned);
    }

    @Test
    void findAllByCourseName_shouldThrowNullPointerException_whenPassingNull() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            studentDao.findAllByCourseName(null);
        });
    }

    @Test
    void findAllByCourseName_shouldReturnEmptyList_whenDBError() throws SQLException {
        Mockito.doThrow(new SQLException("Mock testing Exception")).when(spyDataSource).getConnection();
        List<Student> actual = studentDao.findAllByCourseName("CourseName");
        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    void findAllByCourseName_shouldReturnCorrectList_whenExample1() {
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
    void save_shouldReturnFalse_whenDBError() throws SQLException {
        Mockito.doThrow(new SQLException("Mock testing Exception")).when(spyDataSource).getConnection();
        Student student = new Student(1, 0, "FirstName", "LastName");
        boolean studentWasSaved = studentDao.save(student);
        Assertions.assertFalse(studentWasSaved);
    }

    @Test
    void save_shouldSaveStudentAndReturnTrue_whenExample1() {
        Student student = new Student(1, 0, "FirstName", "LastName");
        boolean studentWasSaved = studentDao.save(student);

        List<Student> expected = List.of(student);
        List<Student> actual = studentDao.findAll();
        Assertions.assertEquals(expected, actual);
        Assertions.assertTrue(studentWasSaved);
    }

    @Test
    void deleteById_shouldReturnFalse_whenDBError() throws SQLException {
        Mockito.doThrow(new SQLException("Mock testing Exception")).when(spyDataSource).getConnection();
        int studentId = 7;
        boolean studentWasDeleted = studentDao.deleteById(studentId);
        Assertions.assertFalse(studentWasDeleted);
    }

    @Test
    void deleteById_shouldDeleteStudentAndReturnTrue_whenExample1() {
        Student student = new Student(1, 0, "FirstName", "LastName");
        studentDao.save(student);
        boolean studentWasDeleted = studentDao.deleteById(student.getId());

        List<Student> expected = Collections.emptyList();
        List<Student> actual = studentDao.findAll();
        Assertions.assertEquals(expected, actual);
        Assertions.assertTrue(studentWasDeleted);
    }

    @Test
    void deleteById_shouldReturnFalse_whenStudentIdDoestExistInDb() {
        int studentId = 777;
        boolean studentWasDeleted = studentDao.deleteById(studentId);
        Assertions.assertFalse(studentWasDeleted);
    }

    @Test
    void findAll_shouldReturnEmptyList_whenDBError() throws SQLException {
        Mockito.doThrow(new SQLException("Mock testing Exception")).when(spyDataSource).getConnection();
        List<Student> actual = studentDao.findAll();
        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    void findAll_shouldReturnCorrectListWithGroups_whenExample1() {
        List<Student> expected = List.of(new Student(1, 0, "FirstName", "LastName"));
        studentDao.saveAllBatch(expected);
        List<Student> actual = studentDao.findAll();
        Assertions.assertEquals(actual, expected);
    }

    @Test 
    void findAll_shouldReturnEmptyList_whenThereAreNoStudents() {
        List<Student> actual = studentDao.findAll();
        Assertions.assertTrue(actual.isEmpty());
    }
    @Test
    void assignToCourse_shouldReturnFalse_whenDBError() throws SQLException {
        Mockito.doThrow(new SQLException("Mock testing Exception")).when(spyDataSource).getConnection();
        int studentId = 777;
        int courseId = 777;
        boolean studentWasAssigned = studentDao.assignToCourse(studentId, courseId);
        Assertions.assertFalse(studentWasAssigned);
    }

    @Test
    void assignToCourse_shouldAssignStudentToCourseAndReturnTrue_whenExample1() {
        Student student = new Student(1, 0, "FirstName", "LastName");
        studentDao.save(student);
        Course course = new Course(1, "Name", "Descr");
        List<Course> expected = List.of(course);
        CourseDao courseDao = new CourseDaoImpl(spyDataSource);
        courseDao.saveAllBatch(expected);

        boolean studentWasAssigned = studentDao.assignToCourse(student.getId(), course.getId());

        List<Course> actual = courseDao.findAllByStudentId(student.getId());
        Assertions.assertEquals(expected, actual);
        Assertions.assertTrue(studentWasAssigned);
    }

    @Test
    void assignToCourse_shouldReturnFalse_whenStudentAlreadyHaveGivenCourse() {
        Student student = new Student(1, 0, "FirstName", "LastName");
        studentDao.save(student);
        Course course = new Course(1, "Name", "Descr");
        List<Course> expected = List.of(course);
        CourseDao courseDao = new CourseDaoImpl(spyDataSource);
        courseDao.saveAllBatch(expected);
        studentDao.assignToCourse(student.getId(), course.getId());

        boolean studentWasAssigned = studentDao.assignToCourse(student.getId(), course.getId());
        Assertions.assertFalse(studentWasAssigned);
    }

    @Test
    void deleteFromCourse_shouldReturnFalse_whenDBError() throws SQLException {
        Mockito.doThrow(new SQLException("Mock testing Exception")).when(spyDataSource).getConnection();
        int studentId = 777;
        int courseId = 777;
        boolean studentWasDeletedFromCourse = studentDao.deleteFromCourse(studentId, courseId);
        Assertions.assertFalse(studentWasDeletedFromCourse);
    }

    @Test
    void deleteFromCourse_shouldDeleteStudentFromCourseAndReturnTrue_whenExample1() {
        Student student = new Student(1, 0, "FirstName", "LastName");
        studentDao.save(student);
        Course course = new Course(1, "Name", "Descr");
        List<Course> courseList = List.of(course);
        CourseDao courseDao = new CourseDaoImpl(spyDataSource);
        courseDao.saveAllBatch(courseList);
        studentDao.assignToCourse(student.getId(), course.getId());

        boolean studentWasDeletedFromCourse = studentDao.deleteFromCourse(student.getId(), course.getId());

        List<Course> expected = Collections.emptyList();
        List<Course> actual = courseDao.findAllByStudentId(student.getId());
        Assertions.assertEquals(expected, actual);
        Assertions.assertTrue(studentWasDeletedFromCourse);
    }

    @Test
    void deleteFromCourse_shouldReturnFalse_whenStudentDoesntHaveGivenCourse() {
        Student student = new Student(1, 0, "FirstName", "LastName");
        studentDao.save(student);
        int courseId = 777;

        boolean studentWasDeletedFromCourse = studentDao.deleteFromCourse(student.getId(), courseId);
        Assertions.assertFalse(studentWasDeletedFromCourse);
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
