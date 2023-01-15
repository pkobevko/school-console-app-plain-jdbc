package ua.foxminded.school.dao;

import java.util.List;

import ua.foxminded.school.domain.model.Course;

public interface CourseDao {
    void saveAllBatch(List<Course> courses);

    List<Course> findAll();

    List<Course> findAllByStudentId(int studentId);
}
