package ua.foxminded.school.dao;

import java.util.List;
import java.util.Map;

import ua.foxminded.school.domain.model.Course;
import ua.foxminded.school.domain.model.Student;

public interface StudentDao {
    boolean saveAllBatch(List<Student> students);

    boolean assignToCoursesBatch(Map<Student, List<Course>> studentsCourses);

    List<Student> findAllByCourseName(String courseName);

    boolean save(Student student);

    boolean deleteById(int studentId);

    List<Student> findAll();

    boolean assignToCourse(int studentId, int courseId);

    boolean deleteFromCourse(int studentId, int courseId);
}
