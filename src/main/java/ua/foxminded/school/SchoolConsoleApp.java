package ua.foxminded.school;

import javax.sql.DataSource;

import ua.foxminded.school.ui.UserInterface;
import ua.foxminded.school.util.JdbcUtil;
import ua.foxminded.school.util.SchoolDbInitializer;
import ua.foxminded.school.util.data.Data;

public class SchoolConsoleApp {
    public static void main(String[] args) {
        DataSource dataSource = JdbcUtil.createDefaultPostgresDataSource();
        SchoolDbInitializer schoolDbInitializer = new SchoolDbInitializer(dataSource);
        schoolDbInitializer.init();

        Data testData = new Data();
        JdbcUtil.insertTestDataInDatabase(testData, dataSource);

        UserInterface userInterface = new UserInterface(dataSource);
        userInterface.run();
    }
}
