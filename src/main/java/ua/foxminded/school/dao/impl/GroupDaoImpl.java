package ua.foxminded.school.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;

import ua.foxminded.school.dao.GroupDao;
import ua.foxminded.school.domain.model.Group;
import ua.foxminded.school.exception.DaoOperationException;

public class GroupDaoImpl implements GroupDao {
    private static final String GROUP_INSERT_SQL = "INSERT INTO groups(name) VALUES (?);";

    private final DataSource dataSource;

    public GroupDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(List<Group> groups) throws DaoOperationException {
        Objects.requireNonNull(groups);
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement insertStatement = connection.prepareStatement(GROUP_INSERT_SQL);
            performBatchInsert(insertStatement, groups);
        } catch (SQLException e) {
            throw new DaoOperationException("Error saving groups", e);
        }
    }

    private void performBatchInsert(PreparedStatement insertStatement, List<Group> groups) throws SQLException {
        for (Group group : groups) {
            fillGroupStatement(group, insertStatement);
            insertStatement.addBatch();
        }
        insertStatement.executeBatch();
    }

    @Override
    public void save(Group group) throws DaoOperationException {
        Objects.requireNonNull(group);
        try (Connection connection = dataSource.getConnection()) {
            saveGroup(group, connection);
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Error saving group: %s", group), e);
        }
    }

    private void saveGroup(Group group, Connection connection) throws SQLException {
        PreparedStatement insertStatement = prepareInsertStatement(group, connection);
        insertStatement.executeUpdate();
        int id = fetchGeneratedId(insertStatement);
        group.setId(id);
    }

    private int fetchGeneratedId(PreparedStatement insertStatement) throws SQLException {
        ResultSet generatedKeys = insertStatement.getGeneratedKeys();
        if (generatedKeys.next()) {
            return generatedKeys.getInt(1);
        } else {
            throw new DaoOperationException("Cannot obtain group ID");
        }
    }

    private PreparedStatement prepareInsertStatement(Group group, Connection connection) throws DaoOperationException {
        try {
            PreparedStatement insertStatement = connection.prepareStatement(GROUP_INSERT_SQL,
                    PreparedStatement.RETURN_GENERATED_KEYS);
            fillGroupStatement(group, insertStatement);
            return insertStatement;
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Cannot prepare statement for group: %s", group), e);
        }
    }

    private void fillGroupStatement(Group group, PreparedStatement preparedStatement) throws DaoOperationException {
        try {
            preparedStatement.setString(1, group.getName());
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Cannot fill statement for group: %s", group), e);
        }
    }
}
