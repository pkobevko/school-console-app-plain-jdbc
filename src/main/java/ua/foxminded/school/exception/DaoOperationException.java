package ua.foxminded.school.exception;

import java.sql.SQLException;

public class DaoOperationException extends SQLException {
    private static final long serialVersionUID = -2993241880624730014L;

    public DaoOperationException(String message) {
        super(message);
    }

    public DaoOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
