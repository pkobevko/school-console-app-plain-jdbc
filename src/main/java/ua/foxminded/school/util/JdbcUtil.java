package ua.foxminded.school.util;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.postgresql.ds.PGSimpleDataSource;

import ua.foxminded.school.dao.CourseDao;
import ua.foxminded.school.dao.GroupDao;
import ua.foxminded.school.dao.StudentDao;
import ua.foxminded.school.dao.impl.CourseDaoImpl;
import ua.foxminded.school.dao.impl.GroupDaoImpl;
import ua.foxminded.school.dao.impl.StudentDaoImpl;
import ua.foxminded.school.domain.model.Course;
import ua.foxminded.school.domain.model.Group;
import ua.foxminded.school.domain.model.Student;
import ua.foxminded.school.exception.DaoOperationException;
import ua.foxminded.school.util.data.Data;

public class JdbcUtil {
    private static String DEFAULT_DATABASE_NAME = "school_db";
    private static String DEFAULT_USERNAME = "postgres";
    private static String DEFAULT_PASSWORD = "1234";

    public static DataSource createDefaultPostgresDataSource() {
        String url = formatPostgresDbUrl(DEFAULT_DATABASE_NAME);
        return createPostgresDataSource(url, DEFAULT_USERNAME, DEFAULT_PASSWORD);
    }

    private static String formatPostgresDbUrl(String databaseName) {
        return String.format("jdbc:postgresql://localhost:5432/%s", databaseName);
    }

    private static DataSource createPostgresDataSource(String url, String username, String pass) {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(url);
        dataSource.setUser(username);
        dataSource.setPassword(pass);
        return dataSource;
    }

    public static void insertTestDataInDatabase(Data data, DataSource dataSource) throws DaoOperationException {
        try {
            List<Group> groups = data.getGroups();
            GroupDao groupDao = new GroupDaoImpl(dataSource);
            groupDao.saveAll(groups);

            List<Course> courses = data.getCourses();
            CourseDao courseDao = new CourseDaoImpl(dataSource);
            courseDao.saveAll(courses);

            List<Student> students = data.getStudents(groups);
            Map<Student, List<Course>> studentsCourses = data.getStudentsCourses(students, courses);
            StudentDao studentDao = new StudentDaoImpl(dataSource);
            studentDao.saveAll(students);
            studentDao.assignToCourses(studentsCourses);
        } catch (DaoOperationException e) {
            throw new DaoOperationException("Error inserting data in database:" + e.getMessage(), e);
        }
    }
}
