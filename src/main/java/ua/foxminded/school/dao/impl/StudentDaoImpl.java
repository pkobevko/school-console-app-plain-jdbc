package ua.foxminded.school.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.sql.DataSource;

import ua.foxminded.school.dao.StudentDao;
import ua.foxminded.school.domain.model.Course;
import ua.foxminded.school.domain.model.Student;
import ua.foxminded.school.exception.DaoOperationException;

public class StudentDaoImpl implements StudentDao {
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
    public void saveAllBatch(List<Student> students) {
        Objects.requireNonNull(students);
        try (Connection connection = dataSource.getConnection()) {
            saveAllStudents(students, connection);
        } catch (SQLException e) {
            throw new DaoOperationException("Error saving students", e);
        }
    }

    private void saveAllStudents(List<Student> students, Connection connection) throws SQLException {
        PreparedStatement insertStatement = connection.prepareStatement(INSERT_STUDENT_SQL);
        performBatchStudentsInsert(insertStatement, students);
    }

    private void performBatchStudentsInsert(PreparedStatement insertStatement, List<Student> students)
            throws SQLException {
        for (Student student : students) {
            fillStudentInsertStatement(student, insertStatement);
            insertStatement.addBatch();
        }
        insertStatement.executeBatch();
    }

    private PreparedStatement fillStudentInsertStatement(Student student, PreparedStatement insertStatement) {
        try {
            insertStatement.setInt(1, student.getGroupId());
            insertStatement.setString(2, student.getFirstName());
            insertStatement.setString(3, student.getLastName());
            return insertStatement;
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Cannot fill insert-statement for student: %s", student), e);
        }
    }

    @Override
    public void assignToCoursesBatch(Map<Student, List<Course>> studentsCourses) {
        Objects.requireNonNull(studentsCourses);
        try (Connection connection = dataSource.getConnection()) {
            assignStudentsToCourses(studentsCourses, connection);
        } catch (SQLException e) {
            throw new DaoOperationException("Error assigning students to courses", e);
        }

    }

    private void assignStudentsToCourses(Map<Student, List<Course>> studentsCourses, Connection connection)
            throws SQLException {
        PreparedStatement assignStatement = connection.prepareStatement(INSERT_STUDENTS_COURSES_SQL);
        performBatchStudentsCoursesInsert(assignStatement, studentsCourses);
    }

    private void performBatchStudentsCoursesInsert(PreparedStatement assignStatement,
            Map<Student, List<Course>> studentsCourses) throws SQLException {
        for (Map.Entry<Student, List<Course>> entry : studentsCourses.entrySet()) {
            Student student = entry.getKey();
            List<Course> courses = entry.getValue();
            for (Course course : courses) {
                fillStudentsCoursesInsertStatement(student.getId(), course.getId(), assignStatement);
                assignStatement.addBatch();
            }
        }
        assignStatement.executeBatch();
    }

    private PreparedStatement fillStudentsCoursesInsertStatement(int studentId, int courseId,
            PreparedStatement assignStatement) {
        try {
            assignStatement.setInt(1, studentId);
            assignStatement.setInt(2, courseId);
            return assignStatement;
        } catch (SQLException e) {
            throw new DaoOperationException(
                    String.format("Cannot fill assign-statement for student with ID: %d to course with ID: %d",
                            studentId, courseId),
                    e);
        }
    }

    @Override
    public List<Student> findAllByCourseName(String courseName) {
        Objects.requireNonNull(courseName);
        try (Connection connection = dataSource.getConnection()) {
            return findAllStudentsByCourseName(courseName, connection);
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Error finding students by course name: %s", courseName), e);
        }
    }

    private List<Student> findAllStudentsByCourseName(String courseName, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = prepareFindByCourseNameStatement(connection, courseName);
        ResultSet resultSet = preparedStatement.executeQuery();
        return collectToList(resultSet);
    }

    private PreparedStatement prepareFindByCourseNameStatement(Connection connection, String courseName) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(SELECT_STUDENTS_BY_COURSE_NAME_SQL);
            preparedStatement.setString(1, courseName);
            return preparedStatement;
        } catch (SQLException e) {
            throw new DaoOperationException("Cannot prepare find-by-course-name-statement", e);
        }
    }

    private List<Student> collectToList(ResultSet resultSet) throws DaoOperationException {
        List<Student> students = new ArrayList<>();
        try {
            while (resultSet.next()) {
                Student student = parseRow(resultSet);
                students.add(student);
            }
            return students;
        } catch (SQLException e) {
            throw new DaoOperationException("Cannot collect result set to list", e);
        }
    }

    private Student parseRow(ResultSet resultSet) throws DaoOperationException {
        try {
            return createFromResultSet(resultSet);
        } catch (SQLException e) {
            throw new DaoOperationException("Cannot parse row to create student instance", e);
        }
    }

    private Student createFromResultSet(ResultSet resultSet) throws SQLException {
        Student student = new Student();
        student.setId(resultSet.getInt("id"));
        student.setGroupId(resultSet.getInt("group_id"));
        student.setFirstName(resultSet.getString("first_name"));
        student.setLastName(resultSet.getString("last_name"));
        return student;
    }

    @Override
    public void save(Student student) {
        Objects.requireNonNull(student);
        try (Connection connection = dataSource.getConnection()) {
            saveStudent(student, connection);
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Error saving student: %s", student), e);
        }
    }

    private void saveStudent(Student student, Connection connection) throws SQLException {
        PreparedStatement insertStatement = prepareInsertStatement(connection, student);
        executeUpdate(insertStatement, "Student was not created");
        int id = fetchGeneratedId(insertStatement);
        student.setId(id);
    }

    private PreparedStatement prepareInsertStatement(Connection connection, Student student) {
        try {
            PreparedStatement insertStatement = connection.prepareStatement(INSERT_STUDENT_WITHOUT_GROUP_SQL,
                    PreparedStatement.RETURN_GENERATED_KEYS);
            return fillStudentInsertStatement(student, insertStatement);
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Cannot prepare statement to insert student: %s", student),
                    e);
        }
    }

    private int fetchGeneratedId(PreparedStatement insertStatement) throws SQLException {
        ResultSet generatedKeys = insertStatement.getGeneratedKeys();
        if (generatedKeys.next()) {
            return generatedKeys.getInt("id");
        } else {
            throw new DaoOperationException("Cannot obtain student ID");
        }
    }

    private void executeUpdate(PreparedStatement preparedStatement, String errorMessage) throws SQLException {
        int rowsAffected = preparedStatement.executeUpdate();
        if (rowsAffected == 0) {
            throw new DaoOperationException(errorMessage);
        }
    }

    @Override
    public void deleteById(int studentId) {
        try (Connection connection = dataSource.getConnection()) {
            deleteStudentById(studentId, connection);
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Error deleting student with ID: %d", studentId), e);
        }
    }

    private void deleteStudentById(int studentId, Connection connection) throws SQLException {
        PreparedStatement deleteByIdStatement = prepareDeleteByIdStatement(studentId, connection);
        executeUpdate(deleteByIdStatement, String.format("Does not exist student with given ID: %d", studentId));
    }

    private PreparedStatement prepareDeleteByIdStatement(int studentId, Connection connection) {
        try {
            PreparedStatement deleteStatement = connection.prepareStatement(DELETE_STUDENT_BY_ID_SQL,
                    PreparedStatement.RETURN_GENERATED_KEYS);
            deleteStatement.setInt(1, studentId);
            return deleteStatement;
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Cannot prepare delete-statement for ID: %d", studentId), e);
        }
    }

    @Override
    public List<Student> findAll() {
        try (Connection connection = dataSource.getConnection()) {
            return findAllStudents(connection);
        } catch (SQLException e) {
            throw new DaoOperationException("Error finding students", e);
        }
    }

    private List<Student> findAllStudents(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(SELECT_ALL_STUDENTS_SQL);
        return collectToList(resultSet);
    }

    @Override
    public void assignToCourse(int studentId, int courseId) {
        try (Connection connection = dataSource.getConnection()) {
            assignStudentToCourse(studentId, courseId, connection);
        } catch (SQLException e) {
            throw new DaoOperationException(
                    String.format("Error assigning student with ID: %d to course with ID: %d", studentId, courseId), e);
        }
    }

    private void assignStudentToCourse(int studentId, int courseId, Connection connection) throws SQLException {
        PreparedStatement assignStatement = prepareAssignStatement(studentId, courseId, connection);
        executeUpdate(assignStatement, String.format("Cannot assign student with ID: %d to course with ID: %d", studentId, courseId));
    }

    private PreparedStatement prepareAssignStatement(int studentId, int courseId, Connection connection) {
        try {
            PreparedStatement assignStatement = connection.prepareStatement(INSERT_STUDENTS_COURSES_SQL,
                    PreparedStatement.RETURN_GENERATED_KEYS);
            return fillStudentsCoursesInsertStatement(studentId, courseId, assignStatement);
        } catch (SQLException e) {
            throw new DaoOperationException(
                    String.format("Cannot prepare statement to assign student with ID: %d to course with ID: %d",
                            studentId, courseId),
                    e);
        }
    }

    @Override
    public void deleteFromCourse(int studentId, int courseId) {
        try (Connection connection = dataSource.getConnection()) {
            deleteStudentFromCourse(studentId, courseId, connection);
        } catch (SQLException e) {
            throw new DaoOperationException(
                    String.format("Error deleting student with ID: %d from course with ID: %d", studentId, courseId),
                    e);
        }
    }

    private void deleteStudentFromCourse(int studentId, int courseId, Connection connection) throws SQLException {
        PreparedStatement deleteFromCourseStatement = prepareDeleteStudentFromCourseStatement(studentId, courseId,
                connection);
        executeUpdate(deleteFromCourseStatement,
                String.format("Student with ID: %d was not deleted from course with ID: %d", studentId, courseId));
    }

    private PreparedStatement prepareDeleteStudentFromCourseStatement(int studentId, int courseId,
            Connection connection) {
        try {
            PreparedStatement deleteFromCourseStatement = connection.prepareStatement(DELETE_STUDENT_FROM_COURSE_SQL,
                    PreparedStatement.RETURN_GENERATED_KEYS);
            return fillDeleteFromCourseStatement(deleteFromCourseStatement, studentId, courseId);
        } catch (SQLException e) {
            throw new DaoOperationException(
                    String.format("Cannot prepare delete-from-course-statement for ID: %d", studentId), e);
        }
    }

    private PreparedStatement fillDeleteFromCourseStatement(PreparedStatement deleteFromCourseStatement, int studentId,
            int courseId) {
        try {
            deleteFromCourseStatement.setInt(1, studentId);
            deleteFromCourseStatement.setInt(2, courseId);
            return deleteFromCourseStatement;
        } catch (SQLException e) {
            throw new DaoOperationException(
                    String.format("Cannot fill delete from course statement for ID: %d", studentId), e);
        }
    }
}
