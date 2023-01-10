package ua.foxminded.school.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.sql.DataSource;

import ua.foxminded.school.dao.StudentDao;
import ua.foxminded.school.domain.model.Course;
import ua.foxminded.school.domain.model.Student;
import ua.foxminded.school.exception.DaoOperationException;

public class StudentDaoImpl implements StudentDao {
    private static final String STUDENT_INSERT_SQL = "INSERT INTO students(group_id, first_name, last_name) VALUES (?,?,?);";
    private static final String STUDENTS_COURSES_INSERT_SQL = "INSERT INTO students_courses(student_id, course_id) VALUES (?,?);";

    private final DataSource dataSource;

    public StudentDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(List<Student> students) throws DaoOperationException {
        Objects.requireNonNull(students);
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement insertStatement = connection.prepareStatement(STUDENT_INSERT_SQL);
            performBatchStudentsInsert(insertStatement, students);
        } catch (SQLException e) {
            throw new DaoOperationException("Error saving students", e);
        }
    }

    private void performBatchStudentsInsert(PreparedStatement insertStatement, List<Student> students)
            throws SQLException {
        for (Student student : students) {
            fillStudentInsertStatement(student, insertStatement);
            insertStatement.addBatch();
        }
        insertStatement.executeBatch();
    }

    private void fillStudentInsertStatement(Student student, PreparedStatement preparedStatement)
            throws DaoOperationException {
        try {
            preparedStatement.setInt(1, student.getGroupId());
            preparedStatement.setString(2, student.getFirstName());
            preparedStatement.setString(3, student.getLastName());
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Cannot fill insert statement for student: %s", student), e);
        }
    }

    @Override
    public void assignToCourses(Map<Student, List<Course>> studentsCourses) throws DaoOperationException {
        Objects.requireNonNull(studentsCourses);
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement assignStatement = connection.prepareStatement(STUDENTS_COURSES_INSERT_SQL);
            performBatchStudentsCoursesInsert(assignStatement, studentsCourses);
        } catch (SQLException e) {
            throw new DaoOperationException("Error assigning students to courses", e);
        }

    }

    private void performBatchStudentsCoursesInsert(PreparedStatement assignStatement,
            Map<Student, List<Course>> studentsCourses) throws SQLException {
        for (Map.Entry<Student, List<Course>> entry : studentsCourses.entrySet()) {
            Student student = entry.getKey();
            List<Course> courses = entry.getValue();
            for (Course course : courses) {
                fillStudentsCoursesInsertStatement(student, course, assignStatement);
                assignStatement.addBatch();
            }
        }
        assignStatement.executeBatch();
    }

    private void fillStudentsCoursesInsertStatement(Student student, Course course, PreparedStatement assignStatement)
            throws DaoOperationException {
        try {
            assignStatement.setInt(1, student.getId());
            assignStatement.setInt(2, course.getId());
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Cannot fill assign statement for student: %s", student), e);
        }
    }
}
