package ua.foxminded.school.dao;

import java.util.List;
import java.util.Map;

import ua.foxminded.school.domain.model.Course;
import ua.foxminded.school.domain.model.Student;
import ua.foxminded.school.exception.DaoOperationException;

public interface StudentDao {
    void saveAll(List<Student> students) throws DaoOperationException;

    void assignToCourses(Map<Student, List<Course>> studentsCourses) throws DaoOperationException;

    List<Student> findByCourseName(String courseName) throws DaoOperationException;

    void save(Student student) throws DaoOperationException;

    void deleteById(int studentId) throws DaoOperationException;

    List<Student> findAll() throws DaoOperationException;

    void assignToCourse(int studentId, int courseId) throws DaoOperationException;

    void deleteFromCourse(int studentId, int courseId) throws DaoOperationException;
}
