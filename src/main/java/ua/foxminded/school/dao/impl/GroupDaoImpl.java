package ua.foxminded.school.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;

import ua.foxminded.school.dao.GroupDao;
import ua.foxminded.school.domain.model.Group;
import ua.foxminded.school.exception.DaoOperationException;

public class GroupDaoImpl implements GroupDao {
    private static final String INSERT_GROUP_SQL = "INSERT INTO groups(name) VALUES (?);";
    private static final String SELECT_ALL_BY_STUDENTS_COUNT_SQL = "SELECT groups.id, groups.name "
            + "FROM groups LEFT JOIN students ON groups.id = students.group_id "
            + "WHERE groups.id != 0 GROUP BY groups.id HAVING COUNT(students.group_id) <= ? ORDER BY groups.id;";
    private static final String SELECT_ALL_GROUPS_SQL = "SELECT * FROM groups WHERE groups.id != 0;";

    private final DataSource dataSource;

    public GroupDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void saveAllBatch(List<Group> groups) {
        Objects.requireNonNull(groups);
        try (Connection connection = dataSource.getConnection()) {
            saveAllGroups(groups, connection);
        } catch (SQLException e) {
            throw new DaoOperationException("Error saving groups using batch", e);
        }
    }

    private void saveAllGroups(List<Group> groups, Connection connection) throws SQLException {
        PreparedStatement insertStatement = connection.prepareStatement(INSERT_GROUP_SQL);
        performBatchInsert(insertStatement, groups);
    }

    private void performBatchInsert(PreparedStatement insertStatement, List<Group> groups) throws SQLException {
        for (Group group : groups) {
            insertStatement.setString(1, group.getName());
            insertStatement.addBatch();
        }
        insertStatement.executeBatch();
    }

    @Override
    public List<Group> findAllByEqualOrLessStudentsCount(int studentCount) {
        try (Connection connection = dataSource.getConnection()) {
            return findAllGroupsByStudentsCount(connection, studentCount);
        } catch (SQLException e) {
            throw new DaoOperationException("Error finding groups by students count", e);
        }
    }

    private List<Group> findAllGroupsByStudentsCount(Connection connection, int studentCount) throws SQLException {
        PreparedStatement preparedStatement = prepareSelectByStudentsCountStatement(studentCount, connection);
        ResultSet resultSet = preparedStatement.executeQuery();
        return collectToList(resultSet);
    }

    private PreparedStatement prepareSelectByStudentsCountStatement(int studentCount, Connection connection) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_BY_STUDENTS_COUNT_SQL);
            preparedStatement.setInt(1, studentCount);
            return preparedStatement;
        } catch (SQLException e) {
            throw new DaoOperationException("Cannot prepare select-by-students-count-statement", e);
        }
    }

    private List<Group> collectToList(ResultSet resultSet) {
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
            return createGroupFromResultSet(resultSet);
        } catch (SQLException e) {
            throw new DaoOperationException("Cannot parse row to create group instance", e);
        }
    }

    private Group createGroupFromResultSet(ResultSet resultSet) throws SQLException {
        Group group = new Group();
        group.setId(resultSet.getInt("id"));
        group.setName(resultSet.getString("name"));
        return group;
    }

    @Override
    public List<Group> findAll() {
        try (Connection connection = dataSource.getConnection()) {
            return findAllGroups(connection);
        } catch (SQLException e) {
            throw new DaoOperationException("Error finding groups", e);
        }
    }

    private List<Group> findAllGroups(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(SELECT_ALL_GROUPS_SQL);
        return collectToList(resultSet);
    }
}
