package ua.foxminded.school.dao;

import java.util.List;
import java.util.Map;

import ua.foxminded.school.domain.model.Course;
import ua.foxminded.school.domain.model.Student;

public interface StudentDao {
    void saveAllBatch(List<Student> students);

    void assignToCoursesBatch(Map<Student, List<Course>> studentsCourses);

    List<Student> findAllByCourseName(String courseName);

    void save(Student student);

    void deleteById(int studentId);

    List<Student> findAll();

    void assignToCourse(int studentId, int courseId);

    void deleteFromCourse(int studentId, int courseId);
}
