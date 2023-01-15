package ua.foxminded.school.exception;

public class DaoOperationException extends RuntimeException {
    private static final long serialVersionUID = -2993241880624730014L;

    public DaoOperationException(String message) {
        super(message);
    }

    public DaoOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
