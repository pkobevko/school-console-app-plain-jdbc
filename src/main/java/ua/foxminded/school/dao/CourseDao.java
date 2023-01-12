package ua.foxminded.school.dao;

import java.util.List;

import ua.foxminded.school.domain.model.Course;
import ua.foxminded.school.exception.DaoOperationException;

public interface CourseDao {
    void saveAll(List<Course> courses) throws DaoOperationException;

    List<Course> findAll() throws DaoOperationException;

    List<Course> findAllByStudentId(int studentId) throws DaoOperationException;
}
