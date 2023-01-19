package ua.foxminded.school.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import ua.foxminded.school.exception.FileReaderException;
import ua.foxminded.school.exception.SchoolDbInitializerException;

public class SchoolDbInitializer {
    private static final String TABLE_INITIALIZATION_SQL_FILE = "tables_initialization.sql";

    private DataSource dataSource;

    public SchoolDbInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void init() throws SchoolDbInitializerException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            String createTablesSql = FileReader.readWholeFileFromResources(TABLE_INITIALIZATION_SQL_FILE);
            statement.execute(createTablesSql);
        } catch (FileReaderException e) {
            throw new SchoolDbInitializerException("Cannot read initialization file", e);
        } catch (SQLException e) {
            throw new SchoolDbInitializerException("Database initialization error", e);
        }
    }
}
