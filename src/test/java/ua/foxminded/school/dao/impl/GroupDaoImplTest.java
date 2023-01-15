package ua.foxminded.school.dao.impl;

import java.sql.Connection;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import ua.foxminded.school.dao.GroupDao;
import ua.foxminded.school.util.FileReader;
import ua.foxminded.school.util.JdbcUtil;

class GroupDaoImplTest {
    private static final String TABLE_TEST_INITIALIZATION_SQL_FILE = "test_table_initialization.sql";

    private static GroupDao groupDao;
    private static DataSource originalDataSource;
    private static DataSource spyDataSource;

    @BeforeEach
    void init() {
        originalDataSource = JdbcUtil.createDefaultInMemoryH2DataSource();
        spyDataSource = Mockito.spy(originalDataSource);
        groupDao = new GroupDaoImpl(spyDataSource);
        createTables(originalDataSource);
    }

    @Test
    void test() {

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
