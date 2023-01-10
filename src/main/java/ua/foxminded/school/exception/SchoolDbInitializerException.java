package ua.foxminded.school.exception;

public class SchoolDbInitializerException extends Exception {
    private static final long serialVersionUID = -3559019717015623357L;

    public SchoolDbInitializerException(String message, Throwable cause) {
        super(message, cause);
    }

    public SchoolDbInitializerException(String message) {
        super(message);
    }
}
