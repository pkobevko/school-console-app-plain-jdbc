package ua.foxminded.school.dao;

import java.util.List;

import ua.foxminded.school.domain.model.Group;
import ua.foxminded.school.exception.DaoOperationException;

public interface GroupDao {
    void insert(List<Group> groups) throws DaoOperationException;

    void save(Group group) throws DaoOperationException;
}
