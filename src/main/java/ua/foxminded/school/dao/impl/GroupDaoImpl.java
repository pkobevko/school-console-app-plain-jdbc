package ua.foxminded.school.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ua.foxminded.school.dao.GroupDao;
import ua.foxminded.school.domain.model.Group;

public class GroupDaoImpl implements GroupDao {
    private static final Logger LOGGER = LogManager.getLogger(GroupDaoImpl.class);
    private static final boolean SUCCESSFUL_OPERATION = true;
    private static final boolean FAILED_OPERATION = false;
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
    public boolean saveAllBatch(List<Group> groups) {
        Objects.requireNonNull(groups);
        try (Connection connection = dataSource.getConnection()) {
            saveAllGroupsBatch(groups, connection);
            return SUCCESSFUL_OPERATION;
        } catch (SQLException e) {
            LOGGER.error("Error saving groups using batch", e);
            return FAILED_OPERATION;
        }
    }

    private void saveAllGroupsBatch(List<Group> groups, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_GROUP_SQL)) {
            performBatchInsert(statement, groups);
        }
    }

    private void performBatchInsert(PreparedStatement statement, List<Group> groups) throws SQLException {
        for (Group group : groups) {
            statement.setString(1, group.getName());
            statement.addBatch();
        }
        statement.executeBatch();
    }

    @Override
    public List<Group> findAllByEqualOrLessStudentsCount(int studentsCount) {
        try (Connection connection = dataSource.getConnection()) {
            return findAllGroupsByStudentsCount(connection, studentsCount);
        } catch (SQLException e) {
            LOGGER.error(String.format("Error finding groups by students count: %d", studentsCount), e);
            return Collections.emptyList();
        }
    }

    private List<Group> findAllGroupsByStudentsCount(Connection connection, int studentsCount) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT_ALL_BY_STUDENTS_COUNT_SQL)) {
            statement.setInt(1, studentsCount);
            ResultSet resultSet = statement.executeQuery();
            return collectToList(resultSet);
        }
    }

    private List<Group> collectToList(ResultSet resultSet) throws SQLException {
        List<Group> groups = new ArrayList<>();
        while (resultSet.next()) {
            Group group = createGroupFromResultSetRow(resultSet);
            groups.add(group);
        }
        return groups;
    }

    private Group createGroupFromResultSetRow(ResultSet resultSet) throws SQLException {
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
            LOGGER.error("Error finding groups", e);
            return Collections.emptyList();
        }
    }

    private List<Group> findAllGroups(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(SELECT_ALL_GROUPS_SQL);
            return collectToList(resultSet);
        }
    }
}
