package ua.foxminded.school.ui;

import java.util.Scanner;

import javax.sql.DataSource;

import ua.foxminded.school.dao.CourseDao;
import ua.foxminded.school.dao.GroupDao;
import ua.foxminded.school.dao.StudentDao;
import ua.foxminded.school.dao.impl.CourseDaoImpl;
import ua.foxminded.school.dao.impl.GroupDaoImpl;
import ua.foxminded.school.dao.impl.StudentDaoImpl;

public class UserInterface {
    private DataSource dataSource;
    private final Scanner scanner;
    private CourseDao courseDao;
    private GroupDao groupDao;
    private StudentDao studentDao;

    public UserInterface(DataSource dataSource) {
        scanner = new Scanner(System.in);
        courseDao = new CourseDaoImpl(dataSource);
        groupDao = new GroupDaoImpl(dataSource);
        studentDao = new StudentDaoImpl(dataSource);
    }

    public void run() {

    }
}
