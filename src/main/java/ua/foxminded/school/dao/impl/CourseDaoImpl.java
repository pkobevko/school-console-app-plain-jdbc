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
import java.util.Optional;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ua.foxminded.school.dao.CourseDao;
import ua.foxminded.school.domain.model.Course;

public class CourseDaoImpl implements CourseDao {
    private static final Logger LOGGER = LogManager.getLogger(CourseDaoImpl.class);
    private static final boolean SUCCESSFUL_OPERATION = true;
    private static final boolean FAILED_OPERATION = false;
    private static final String INSERT_COURSE_SQL = "INSERT INTO courses(name, description) VALUES (?,?);";
    private static final String SELECT_ALL_COURSES_SQL = "SELECT * FROM courses;";
    private static final String SELECT_ALL_BY_STUDENT_ID_SQL = "SELECT courses.id, courses.name, courses.description "
            + "FROM students_courses INNER JOIN courses ON courses.id = students_courses.course_id "
            + "WHERE student_id = ?;";
    private static final String SELECT_ALL_BY_COURSE_NAME_SQL = "SELECT * FROM courses WHERE courses.name = ?;";

    private final DataSource dataSource;

    public CourseDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public boolean saveAllBatch(List<Course> courses) {
        Objects.requireNonNull(courses);
        try (Connection connection = dataSource.getConnection()) {
            saveAllCoursesBatch(courses, connection);
            return SUCCESSFUL_OPERATION;
        } catch (SQLException e) {
            LOGGER.error("Error saving courses with batch", e);
            return FAILED_OPERATION;
        }
    }

    private void saveAllCoursesBatch(List<Course> courses, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_COURSE_SQL)) {
            performBatchInsert(statement, courses);
        }
    }

    private void performBatchInsert(PreparedStatement statement, List<Course> courses) throws SQLException {
        for (Course course : courses) {
            fillCourseInsertStatement(course, statement);
            statement.addBatch();
        }
        statement.executeBatch();
    }

    private void fillCourseInsertStatement(Course course, PreparedStatement statement) throws SQLException {
        statement.setString(1, course.getName());
        statement.setString(2, course.getDescription());
    }

    @Override
    public List<Course> findAll() {
        try (Connection connection = dataSource.getConnection()) {
            return findAllCourses(connection);
        } catch (SQLException e) {
            LOGGER.error("Error finding courses", e);
            return Collections.emptyList();
        }
    }

    private List<Course> findAllCourses(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(SELECT_ALL_COURSES_SQL);
            return collectToList(resultSet);
        }
    }

    private List<Course> collectToList(ResultSet resultSet) throws SQLException {
        List<Course> courses = new ArrayList<>();
        while (resultSet.next()) {
            Course course = createCourseFromResultSetRow(resultSet);
            courses.add(course);
        }
        return courses;
    }

    private Course createCourseFromResultSetRow(ResultSet resultSet) throws SQLException {
        Course course = new Course();
        course.setId(resultSet.getInt("id"));
        course.setName(resultSet.getString("name"));
        course.setDescription(resultSet.getString("description"));
        return course;
    }

    @Override
    public List<Course> findAllByStudentId(int studentId) {
        try (Connection connection = dataSource.getConnection()) {
            return findAllCoursesByStudentId(connection, studentId);
        } catch (SQLException e) {
            LOGGER.error(String.format("Error finding courses by student ID: %d", studentId), e);
            return Collections.emptyList();
        }
    }

    private List<Course> findAllCoursesByStudentId(Connection connection, int studentId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT_ALL_BY_STUDENT_ID_SQL)) {
            statement.setInt(1, studentId);
            ResultSet resultSet = statement.executeQuery();
            return collectToList(resultSet);
        }
    }

    @Override
    public Optional<Course> findByName(String courseName) {
        Objects.requireNonNull(courseName);
        try (Connection connection = dataSource.getConnection()) {
            return findCourseByName(courseName, connection);
        } catch (SQLException e) {
            LOGGER.error(String.format("Error finding course by name: %s", courseName), e);
            return Optional.empty();
        }
    }

    private Optional<Course> findCourseByName(String courseName, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT_ALL_BY_COURSE_NAME_SQL)) {
            statement.setString(1, courseName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Course course = createCourseFromResultSetRow(resultSet);
                return Optional.of(course);
            } else {
                return Optional.empty();
            }
        }
    }
}
