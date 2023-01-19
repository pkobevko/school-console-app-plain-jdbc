package ua.foxminded.school.exception;

import java.io.IOException;

public class FileReaderException extends IOException {
    private static final long serialVersionUID = -7021284237329629776L;

    public FileReaderException(String message, Exception e) {
        super(message, e);
    }
}