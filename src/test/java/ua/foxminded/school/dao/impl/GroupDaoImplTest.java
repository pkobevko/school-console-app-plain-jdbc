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

import ua.foxminded.school.dao.GroupDao;
import ua.foxminded.school.dao.StudentDao;
import ua.foxminded.school.domain.model.Group;
import ua.foxminded.school.domain.model.Student;
import ua.foxminded.school.util.FileReader;
import ua.foxminded.school.util.JdbcUtil;

class GroupDaoImplTest {
    private static final String TABLE_TEST_INITIALIZATION_SQL_FILE = "test_tables_initialization.sql";
    private static final int TEST_STUDENTS_COUNT = 1;

    private static GroupDao groupDao;
    private static DataSource originalDataSource;
    private static DataSource spyDataSource;

    @BeforeAll
    static void setup() {
        originalDataSource = JdbcUtil.createDefaultInMemoryH2DataSource();
    }

    @BeforeEach
    void init() {
        spyDataSource = Mockito.spy(originalDataSource);
        groupDao = new GroupDaoImpl(spyDataSource);
        createTables(originalDataSource);
    }

    @Test
    void saveAllBatch_shouldThrowNullPointerException_whenPassingNull() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            groupDao.saveAllBatch(null);
        });
    }

    @Test
    void saveAllBatch_shouldReturnFalse_whenDBError() throws SQLException {
        Mockito.doThrow(new SQLException("Mock testing Exception")).when(spyDataSource).getConnection();
        boolean coursesWasSaved = groupDao.saveAllBatch(Collections.emptyList());
        Assertions.assertFalse(coursesWasSaved);
    }

    @Test
    void saveAllBatch_shouldSaveAllGroupsAndReturnTrue_whenExample1() {
        List<Group> expected = List.of(new Group(1, "Name1"));
        boolean coursesWasSaved = groupDao.saveAllBatch(expected);
        List<Group> actual = groupDao.findAll();
        Assertions.assertEquals(expected, actual);
        Assertions.assertTrue(coursesWasSaved);
    }

    @Test
    void findAll_shouldReturnEmptyList_whenDBError() throws SQLException {
        Mockito.doThrow(new SQLException("Mock testing Exception")).when(spyDataSource).getConnection();
        List<Group> actual = groupDao.findAll();
        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    void findAll_shouldReturnCorrectListWithGroups_whenExample1() {
        List<Group> expected = List.of(new Group(1, "Name1"));
        groupDao.saveAllBatch(expected);
        List<Group> actual = groupDao.findAll();
        Assertions.assertEquals(actual, expected);
    }

    @Test
    void findAll_shouldReturnEmptyList_whenThereAreNoCourses() {
        List<Group> actual = groupDao.findAll();
        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    void findAllByEqualOrLessStudentsCount_shouldReturnEmptyList_whenDBError() throws SQLException {
        Mockito.doThrow(new SQLException("Mock testing Exception")).when(spyDataSource).getConnection();
        List<Group> actual = groupDao.findAllByEqualOrLessStudentsCount(TEST_STUDENTS_COUNT);
        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    void findAllByEqualOrLessStudentsCount_shouldReturnCorrectListWithGroups_whenExample1() {
        List<Group> expected = List.of(new Group(1, "Name1"));
        groupDao.saveAllBatch(expected);

        StudentDao studentDao = new StudentDaoImpl(spyDataSource);
        studentDao.save(new Student(1, 1, "FirstName", "LastName"));

        List<Group> actual = groupDao.findAllByEqualOrLessStudentsCount(TEST_STUDENTS_COUNT);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void findAllByEqualOrLessStudentsCount_shouldReturnEmptyList_whenThereAreNoCoursesByGivenCount() {
        List<Group> actual = groupDao.findAllByEqualOrLessStudentsCount(TEST_STUDENTS_COUNT);
        Assertions.assertTrue(actual.isEmpty());
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
