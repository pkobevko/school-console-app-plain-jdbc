package ua.foxminded.school.dao;

import java.util.List;

import ua.foxminded.school.domain.model.Group;

public interface GroupDao {
    void saveAllBatch(List<Group> groups);

    List<Group> findAllByStudentsCount(int studentCount);
}
