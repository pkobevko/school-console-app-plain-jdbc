package ua.foxminded.school.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;

import ua.foxminded.school.dao.GroupDao;
import ua.foxminded.school.domain.model.Group;
import ua.foxminded.school.exception.DaoOperationException;

public class GroupDaoImpl implements GroupDao {
    private static final String GROUP_INSERT_SQL = "INSERT INTO groups(name) VALUES (?);";
    private static final String SELECT_BY_STUDENTS_COUNT_SQL = "SELECT groups.id, groups.name "
            + "FROM groups INNER JOIN students ON groups.id = students.group_id "
            + "WHERE groups.id != 0 GROUP BY groups.id HAVING COUNT(groups.id) <= ? ORDER BY groups.id;";

    private final DataSource dataSource;

    public GroupDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void saveAll(List<Group> groups) throws DaoOperationException {
        Objects.requireNonNull(groups);
        try (Connection connection = dataSource.getConnection()) {
            saveAllGroups(groups, connection);
        } catch (SQLException e) {
            throw new DaoOperationException("Error saving groups", e);
        }
    }

    private void saveAllGroups(List<Group> groups, Connection connection) throws SQLException {
        PreparedStatement insertStatement = connection.prepareStatement(GROUP_INSERT_SQL);
        performBatchInsert(insertStatement, groups);
    }

    private void performBatchInsert(PreparedStatement insertStatement, List<Group> groups) throws SQLException {
        for (Group group : groups) {
            fillGroupInsertStatement(group, insertStatement);
            insertStatement.addBatch();
        }
        insertStatement.executeBatch();
    }

    private PreparedStatement fillGroupInsertStatement(Group group, PreparedStatement preparedStatement)
            throws DaoOperationException {
        try {
            preparedStatement.setString(1, group.getName());
            return preparedStatement;
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Cannot fill insert statement for group: %s", group), e);
        }
    }

    @Override
    public List<Group> findAllByStudentsCount(int studentCount) throws DaoOperationException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = prepareSelectByStudentsCountStatement(studentCount, connection);
            ResultSet resultSet = preparedStatement.executeQuery();
            return collectToList(resultSet);
        } catch (SQLException e) {
            throw new DaoOperationException("Cannot find groups by students count", e);
        }
    }

    private PreparedStatement prepareSelectByStudentsCountStatement(int studentCount, Connection connection)
            throws DaoOperationException {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(SELECT_BY_STUDENTS_COUNT_SQL);
            return fillSelectByStudentsCountStatement(preparedStatement, studentCount);
        } catch (SQLException e) {
            throw new DaoOperationException("Cannot prepare select by students count statement", e);
        }
    }

    private PreparedStatement fillSelectByStudentsCountStatement(PreparedStatement preparedStatement, int studentCount)
            throws DaoOperationException {
        try {
            preparedStatement.setInt(1, studentCount);
            return preparedStatement;
        } catch (SQLException e) {
            throw new DaoOperationException("Cannot fill select by student count statement", e);
        }
    }

    private List<Group> collectToList(ResultSet resultSet) throws DaoOperationException {
        List<Group> groupList = new ArrayList<>();
        try {
            while (resultSet.next()) {
                Group group = parseRow(resultSet);
                groupList.add(group);
            }
            return groupList;
        } catch (SQLException e) {
            throw new DaoOperationException("Cannot collect result set to list", e);
        }
    }

    private Group parseRow(ResultSet resultSet) throws DaoOperationException {
        try {
            return createFromResultSet(resultSet);
        } catch (SQLException e) {
            throw new DaoOperationException("Cannot parse row to create group instance", e);
        }
    }

    private Group createFromResultSet(ResultSet resultSet) throws SQLException {
        Group group = new Group();
        group.setId(resultSet.getInt("id"));
        group.setName(resultSet.getString("name"));
        return group;
    }
}
