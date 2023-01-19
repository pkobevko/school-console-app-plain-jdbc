package ua.foxminded.school.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ua.foxminded.school.dao.StudentDao;
import ua.foxminded.school.domain.model.Course;
import ua.foxminded.school.domain.model.Student;

public class StudentDaoImpl implements StudentDao {
    private static final Logger LOGGER = LogManager.getLogger(StudentDaoImpl.class);
    private static final boolean SUCCESSFUL_OPERATION = true;
    private static final boolean FAILED_OPERATION = false;
    private static final String INSERT_STUDENT_SQL = "INSERT INTO students(group_id, first_name, last_name) VALUES (?,?,?);";
    private static final String INSERT_STUDENTS_COURSES_SQL = "INSERT INTO students_courses(student_id, course_id) VALUES (?,?);";
    private static final String SELECT_STUDENTS_BY_COURSE_NAME_SQL = "SELECT students.id, students.group_id, students.first_name, students.last_name "
            + "FROM students_courses INNER JOIN students ON students.id = students_courses.student_id "
            + "INNER JOIN courses ON courses.id = students_courses.course_id WHERE courses.name = ?;";
    private static final String INSERT_STUDENT_WITHOUT_GROUP_SQL = "INSERT INTO students(group_id, first_name, last_name) VALUES (?, ?, ?);";
    private static final String DELETE_STUDENT_BY_ID_SQL = "DELETE FROM students WHERE students.id = ?;";
    private static final String SELECT_ALL_STUDENTS_SQL = "SELECT * FROM students;";
    private static final String DELETE_STUDENT_FROM_COURSE_SQL = "DELETE FROM students_courses WHERE student_id = ? AND course_id = ?";

    private final DataSource dataSource;

    public StudentDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public boolean saveAllBatch(List<Student> students) {
        Objects.requireNonNull(students);
        try (Connection connection = dataSource.getConnection()) {
            saveAllStudents(students, connection);
            return SUCCESSFUL_OPERATION;
        } catch (SQLException e) {
            LOGGER.error("Error saving students", e);
            return FAILED_OPERATION;
        }
    }

    private void saveAllStudents(List<Student> students, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_STUDENT_SQL)) {
            performBatchStudentsInsert(statement, students);
        }
    }

    private void performBatchStudentsInsert(PreparedStatement statement, List<Student> students) throws SQLException {
        for (Student student : students) {
            fillStudentInsertStatement(student, statement);
            statement.addBatch();
        }
        statement.executeBatch();
    }

    private PreparedStatement fillStudentInsertStatement(Student student, PreparedStatement statement)
            throws SQLException {
        statement.setInt(1, student.getGroupId());
        statement.setString(2, student.getFirstName());
        statement.setString(3, student.getLastName());
        return statement;
    }

    @Override
    public boolean assignToCoursesBatch(Map<Student, List<Course>> studentsCourses) {
        Objects.requireNonNull(studentsCourses);
        try (Connection connection = dataSource.getConnection()) {
            assignStudentsToCourses(studentsCourses, connection);
            return SUCCESSFUL_OPERATION;
        } catch (SQLException e) {
            LOGGER.error("Error assigning students to courses", e);
            return FAILED_OPERATION;
        }
    }

    private void assignStudentsToCourses(Map<Student, List<Course>> studentsCourses, Connection connection)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_STUDENTS_COURSES_SQL)) {
            performBatchStudentsCoursesInsert(statement, studentsCourses);
        }
    }

    private void performBatchStudentsCoursesInsert(PreparedStatement statement,
            Map<Student, List<Course>> studentsCourses) throws SQLException {
        for (Map.Entry<Student, List<Course>> entry : studentsCourses.entrySet()) {
            Student student = entry.getKey();
            List<Course> courses = entry.getValue();
            for (Course course : courses) {
                fillStudentsCoursesInsertStatement(student.getId(), course.getId(), statement);
                statement.addBatch();
            }
        }
        statement.executeBatch();
    }

    private void fillStudentsCoursesInsertStatement(int studentId, int courseId, PreparedStatement statement)
            throws SQLException {
        statement.setInt(1, studentId);
        statement.setInt(2, courseId);
    }

    @Override
    public List<Student> findAllByCourseName(String courseName) {
        Objects.requireNonNull(courseName);
        try (Connection connection = dataSource.getConnection()) {
            return findAllStudentsByCourseName(courseName, connection);
        } catch (SQLException e) {
            LOGGER.error(String.format("Error finding students by course name: %s", courseName), e);
            return Collections.emptyList();
        }
    }

    private List<Student> findAllStudentsByCourseName(String courseName, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT_STUDENTS_BY_COURSE_NAME_SQL)) {
            statement.setString(1, courseName);
            ResultSet resultSet = statement.executeQuery();
            return collectToList(resultSet);
        }
    }

    private List<Student> collectToList(ResultSet resultSet) throws SQLException {
        List<Student> students = new ArrayList<>();
        while (resultSet.next()) {
            Student student = createStudentFromResultSetRow(resultSet);
            students.add(student);
        }
        return students;
    }

    private Student createStudentFromResultSetRow(ResultSet resultSet) throws SQLException {
        Student student = new Student();
        student.setId(resultSet.getInt("id"));
        student.setGroupId(resultSet.getInt("group_id"));
        student.setFirstName(resultSet.getString("first_name"));
        student.setLastName(resultSet.getString("last_name"));
        return student;
    }

    @Override
    public boolean save(Student student) {
        Objects.requireNonNull(student);
        try (Connection connection = dataSource.getConnection()) {
            return saveStudent(student, connection);
        } catch (SQLException e) {
            LOGGER.error(String.format("Error saving student: %s", student), e);
            return FAILED_OPERATION;
        }
    }

    private boolean saveStudent(Student student, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_STUDENT_WITHOUT_GROUP_SQL,
                PreparedStatement.RETURN_GENERATED_KEYS);) {
            fillStudentInsertStatement(student, statement);
            if (!executeUpdate(statement, "Student was not created")) {
                return FAILED_OPERATION;
            }

            if (!fetchGeneratedId(statement, student)) {
                return FAILED_OPERATION;
            }
            return SUCCESSFUL_OPERATION;
        }
    }

    private boolean fetchGeneratedId(PreparedStatement statement, Student student) throws SQLException {
        ResultSet generatedKeys = statement.getGeneratedKeys();
        if (generatedKeys.next()) {
            int id = generatedKeys.getInt("id");
            student.setId(id);
            return true;
        } else {
            LOGGER.error(String.format("Cannot obtain ID for student: %s", student));
            return false;
        }
    }

    private boolean executeUpdate(PreparedStatement statement, String errorMessage) throws SQLException {
        int rowsAffected = statement.executeUpdate();
        if (rowsAffected == 0) {
            LOGGER.error(errorMessage);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean deleteById(int studentId) {
        try (Connection connection = dataSource.getConnection()) {
            return deleteStudentById(studentId, connection);
        } catch (SQLException e) {
            LOGGER.error(String.format("Error deleting student with ID: %d", studentId), e);
            return FAILED_OPERATION;
        }
    }

    private boolean deleteStudentById(int studentId, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_STUDENT_BY_ID_SQL,
                PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, studentId);
            if (!executeUpdate(statement, String.format("Does not exist student with given ID: %d", studentId))) {
                return FAILED_OPERATION;
            }
            return SUCCESSFUL_OPERATION;
        }
    }

    @Override
    public List<Student> findAll() {
        try (Connection connection = dataSource.getConnection()) {
            return findAllStudents(connection);
        } catch (SQLException e) {
            LOGGER.error("Error finding students", e);
            return Collections.emptyList();
        }
    }

    private List<Student> findAllStudents(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(SELECT_ALL_STUDENTS_SQL);
            return collectToList(resultSet);
        }
    }

    @Override
    public boolean assignToCourse(int studentId, int courseId) {
        try (Connection connection = dataSource.getConnection()) {
            return assignStudentToCourse(studentId, courseId, connection);
        } catch (SQLException e) {
            LOGGER.error(
                    String.format("Error assigning student with ID: %d to course with ID: %d", studentId, courseId), e);
            return FAILED_OPERATION;
        }
    }

    private boolean assignStudentToCourse(int studentId, int courseId, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_STUDENTS_COURSES_SQL,
                PreparedStatement.RETURN_GENERATED_KEYS)) {
            fillStudentsCoursesInsertStatement(studentId, courseId, statement);

            if (!executeUpdate(statement,
                    String.format("Cannot assign student with ID: %d to course with ID: %d", studentId, courseId))) {
                return FAILED_OPERATION;
            }
            return SUCCESSFUL_OPERATION;
        }
    }

    @Override
    public boolean deleteFromCourse(int studentId, int courseId) {
        try (Connection connection = dataSource.getConnection()) {
            return deleteStudentFromCourse(studentId, courseId, connection);
        } catch (SQLException e) {
            LOGGER.error(
                    String.format("Error deleting student with ID: %d from course with ID: %d", studentId, courseId),
                    e);
            return FAILED_OPERATION;
        }
    }

    private boolean deleteStudentFromCourse(int studentId, int courseId, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_STUDENT_FROM_COURSE_SQL,
                PreparedStatement.RETURN_GENERATED_KEYS)) {
            fillDeleteFromCourseStatement(statement, studentId, courseId);

            if (!executeUpdate(statement, String.format("Student with ID: %d was not deleted from course with ID: %d",
                    studentId, courseId))) {
                return FAILED_OPERATION;
            }
            return SUCCESSFUL_OPERATION;
        }
    }

    private PreparedStatement fillDeleteFromCourseStatement(PreparedStatement deleteFromCourseStatement, int studentId,
            int courseId) throws SQLException {
        deleteFromCourseStatement.setInt(1, studentId);
        deleteFromCourseStatement.setInt(2, courseId);
        return deleteFromCourseStatement;
    }
}
