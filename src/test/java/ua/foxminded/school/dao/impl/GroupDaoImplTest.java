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
import ua.foxminded.school.exception.DaoOperationException;
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
    void saveAllBatch_shouldThrowDaoOperationException_whenDBError() throws SQLException {
        Mockito.doThrow(new SQLException("Mock testing Exception")).when(spyDataSource).getConnection();
        Exception exception = Assertions.assertThrows(DaoOperationException.class, () -> {
            groupDao.saveAllBatch(Collections.emptyList());
        });

        String expectedMessage = "Error saving groups using batch";
        String actualMessage = exception.getMessage();
        Assertions.assertEquals(actualMessage, expectedMessage);
    }

    @Test
    void saveAllBatch_shouldSaveAllGroups_whenExample1() {
        List<Group> expected = List.of(new Group(1, "Name1"));
        groupDao.saveAllBatch(expected);
        List<Group> actual = groupDao.findAll();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void findAll_shouldThrowDaoOperationException_whenDBError() throws SQLException {
        Mockito.doThrow(new SQLException("Mock testing Exception")).when(spyDataSource).getConnection();
        Exception exception = Assertions.assertThrows(DaoOperationException.class, () -> {
            groupDao.findAll();
        });

        String expectedMessage = "Error finding groups";
        String actualMessage = exception.getMessage();
        Assertions.assertEquals(actualMessage, expectedMessage);
    }
    
    @Test
    void findAll_shouldReturnCorrectListWithGroups_whenExample1() {
        List<Group> expected = List.of(new Group(1, "Name1"));
        groupDao.saveAllBatch(expected);
        List<Group> actual = groupDao.findAll();
        Assertions.assertEquals(actual, expected);
    }
    
    @Test
    void findAllByStudentsCount_shouldThrowDaoOperationException_whenDBError() throws SQLException {
        Mockito.doThrow(new SQLException("Mock testing Exception")).when(spyDataSource).getConnection();
        Exception exception = Assertions.assertThrows(DaoOperationException.class, () -> {
            groupDao.findAllByEqualOrLessStudentsCount(TEST_STUDENTS_COUNT);
        });

        String expectedMessage = "Error finding groups by students count";
        String actualMessage = exception.getMessage();
        Assertions.assertEquals(actualMessage, expectedMessage);
    }
    
    @Test
    void findAllByStudentsCount_shouldReturnCorrectListWithGroups_whenExample1() {
        List<Group> expected = List.of(new Group(1, "Name1"));
        groupDao.saveAllBatch(expected);
        
        StudentDao studentDao = new StudentDaoImpl(spyDataSource);
        studentDao.save(new Student(1, 1, "FirstName", "LastName"));
        
        List<Group> actual = groupDao.findAllByEqualOrLessStudentsCount(TEST_STUDENTS_COUNT);
        Assertions.assertEquals(expected, actual);
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
