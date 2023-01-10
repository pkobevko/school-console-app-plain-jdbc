package ua.foxminded.school;

import javax.sql.DataSource;

import ua.foxminded.school.exception.DaoOperationException;
import ua.foxminded.school.exception.SchoolDbInitializerException;
import ua.foxminded.school.ui.UserInterface;
import ua.foxminded.school.util.JdbcUtil;
import ua.foxminded.school.util.SchoolDbInitializer;
import ua.foxminded.school.util.data.Data;

public class SchoolConsoleApp {
    public static void main(String[] args) {
        DataSource dataSource = JdbcUtil.createDefaultPostgresDataSource();
        SchoolDbInitializer schoolDbInitializer = new SchoolDbInitializer(dataSource);
        try {
            schoolDbInitializer.init();

            Data data = new Data();
            JdbcUtil.insertDataInDatabase(data, dataSource);

            UserInterface userInterface = new UserInterface(dataSource);
            userInterface.run();
        } catch (SchoolDbInitializerException | DaoOperationException e) {
            e.printStackTrace();
        }
    }
}
