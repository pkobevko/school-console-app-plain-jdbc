package ua.foxminded.school.dao;

import java.util.List;
import java.util.Optional;

import ua.foxminded.school.domain.model.Course;

public interface CourseDao {
    boolean saveAllBatch(List<Course> courses);

    List<Course> findAll();

    List<Course> findAllByStudentId(int studentId);

    Optional<Course> findByName(String courseName);
}
