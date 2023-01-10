package ua.foxminded.school.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;

import ua.foxminded.school.dao.CourseDao;
import ua.foxminded.school.domain.model.Course;
import ua.foxminded.school.exception.DaoOperationException;

public class CourseDaoImpl implements CourseDao {
    private static final String COURSE_INSERT_SQL = "INSERT INTO courses(name, description) VALUES (?,?);";

    private final DataSource dataSource;

    public CourseDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(List<Course> courses) throws DaoOperationException {
        Objects.requireNonNull(courses);
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement insertStatement = connection.prepareStatement(COURSE_INSERT_SQL);
            performBatchInsert(insertStatement, courses);
        } catch (SQLException e) {
            throw new DaoOperationException("Error saving courses", e);
        }
    }

    private void performBatchInsert(PreparedStatement insertStatement, List<Course> courses) throws SQLException {
        for (Course course : courses) {
            fillCourseStatement(course, insertStatement);
            insertStatement.addBatch();
        }
        insertStatement.executeBatch();
    }

    private void fillCourseStatement(Course course, PreparedStatement preparedStatement) throws DaoOperationException {
        try {
            preparedStatement.setString(1, course.getName());
            preparedStatement.setString(2, course.getDescription());
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Cannot fill statement for course: %s", course), e);
        }
    }

    @Override
    public void save(Course course) throws DaoOperationException {
        // TODO Auto-generated method stub

    }
}
