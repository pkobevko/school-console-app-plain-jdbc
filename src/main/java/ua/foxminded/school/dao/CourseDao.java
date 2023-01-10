package ua.foxminded.school.dao;

import java.util.List;

import ua.foxminded.school.domain.model.Course;
import ua.foxminded.school.exception.DaoOperationException;

public interface CourseDao {
    void insert(List<Course> courses) throws DaoOperationException;

    void save(Course course) throws DaoOperationException;
}
