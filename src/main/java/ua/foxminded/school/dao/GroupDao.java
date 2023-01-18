package ua.foxminded.school.dao;

import java.util.List;

import ua.foxminded.school.domain.model.Group;

public interface GroupDao {
    boolean saveAllBatch(List<Group> groups);

    List<Group> findAllByEqualOrLessStudentsCount(int studentsCount);

    List<Group> findAll();
}
