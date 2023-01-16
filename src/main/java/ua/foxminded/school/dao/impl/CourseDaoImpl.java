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

import ua.foxminded.school.dao.CourseDao;
import ua.foxminded.school.domain.model.Course;
import ua.foxminded.school.exception.DaoOperationException;

public class CourseDaoImpl implements CourseDao {
    private static final String INSERT_COURSE_SQL = "INSERT INTO courses(name, description) VALUES (?,?);";
    private static final String SELECT_ALL_COURSES_SQL = "SELECT * FROM courses;";
    private static final String SELECT_ALL_BY_STUDENT_ID_SQL = "SELECT courses.id, courses.name, courses.description "
            + "FROM students_courses INNER JOIN courses ON courses.id = students_courses.course_id "
            + "WHERE student_id = ?;";

    private final DataSource dataSource;

    public CourseDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void saveAllBatch(List<Course> courses) {
        Objects.requireNonNull(courses);
        try (Connection connection = dataSource.getConnection()) {
            saveAllCourses(courses, connection);
        } catch (SQLException e) {
            throw new DaoOperationException("Error saving courses", e);
        }
    }

    private void saveAllCourses(List<Course> courses, Connection connection) throws SQLException {
        PreparedStatement insertStatement = connection.prepareStatement(INSERT_COURSE_SQL);
        performBatchInsert(insertStatement, courses);
    }

    private void performBatchInsert(PreparedStatement insertStatement, List<Course> courses) throws SQLException {
        for (Course course : courses) {
            fillInsertCourseStatement(course, insertStatement);
            insertStatement.addBatch();
        }
        insertStatement.executeBatch();
    }

    private void fillInsertCourseStatement(Course course, PreparedStatement preparedStatement) {
        try {
            preparedStatement.setString(1, course.getName());
            preparedStatement.setString(2, course.getDescription());
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Cannot fill insert-statement for course: %s", course), e);
        }
    }

    @Override
    public List<Course> findAll() {
        try (Connection connection = dataSource.getConnection()) {
            return findAllCourses(connection);
        } catch (SQLException e) {
            throw new DaoOperationException("Error finding courses", e);
        }
    }

    private List<Course> findAllCourses(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(SELECT_ALL_COURSES_SQL);
        return collectToList(resultSet);
    }

    private List<Course> collectToList(ResultSet resultSet) {
        List<Course> courses = new ArrayList<>();
        try {
            while (resultSet.next()) {
                Course course = parseRow(resultSet);
                courses.add(course);
            }
            return courses;
        } catch (SQLException e) {
            throw new DaoOperationException("Cannot collect result set to list", e);
        }
    }

    private Course parseRow(ResultSet resultSet) {
        try {
            return createCourseFromResultSet(resultSet);
        } catch (SQLException e) {
            throw new DaoOperationException("Cannot parse row to create course instance", e);
        }
    }

    private Course createCourseFromResultSet(ResultSet resultSet) throws SQLException {
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
            throw new DaoOperationException(String.format("Error finding courses by student ID: %d", studentId), e);
        }
    }

    private List<Course> findAllCoursesByStudentId(Connection connection, int studentId) throws SQLException {
        PreparedStatement preparedStatement = prepareFindByStudentIdStatement(connection, studentId);
        ResultSet resultSet = preparedStatement.executeQuery();
        return collectToList(resultSet);
    }

    private PreparedStatement prepareFindByStudentIdStatement(Connection connection, int studentId) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_BY_STUDENT_ID_SQL);
            preparedStatement.setInt(1, studentId);
            return preparedStatement;
        } catch (SQLException e) {
            throw new DaoOperationException(
                    String.format("Cannot prepare find-by-student-ID-statement for ID: %d", studentId), e);
        }
    }
}
