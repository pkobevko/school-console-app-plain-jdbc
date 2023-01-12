package ua.foxminded.school.dao;

import java.util.List;

import ua.foxminded.school.domain.model.Group;
import ua.foxminded.school.exception.DaoOperationException;

public interface GroupDao {
    void saveAll(List<Group> groups) throws DaoOperationException;

    List<Group> findAllByStudentsCount(int studentCount) throws DaoOperationException;
}
