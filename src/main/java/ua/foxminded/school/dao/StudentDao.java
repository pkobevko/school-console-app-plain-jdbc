package ua.foxminded.school.dao;

import java.util.List;
import java.util.Map;

import ua.foxminded.school.domain.model.Course;
import ua.foxminded.school.domain.model.Student;
import ua.foxminded.school.exception.DaoOperationException;

public interface StudentDao {
    void insert(List<Student> students) throws DaoOperationException;

    void assignToCourses(Map<Student, List<Course>> studentsCourses) throws DaoOperationException;
}
