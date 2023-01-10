package ua.foxminded.school.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ua.foxminded.school.exception.FileReaderException;

public class FileReader {
    public static String readWholeFileFromResources(String fileName) throws FileReaderException {
        Path filePath = createPathFromFileName(fileName);
        try (Stream<String> fileLinesStream = openFileLinesStream(filePath)) {
            return fileLinesStream.collect(Collectors.joining("\n"));
        }
    }

    private static Stream<String> openFileLinesStream(Path filePath) throws FileReaderException {
        try {
            return Files.lines(filePath);
        } catch (IOException e) {
            throw new FileReaderException("Cannot create stream of file lines!", e);
        }
    }

    private static Path createPathFromFileName(String fileName) throws FileReaderException {
        Objects.requireNonNull(fileName);
        URL fileUrl = FileReader.class.getClassLoader().getResource(fileName);
        try {
            return Paths.get(fileUrl.toURI());
        } catch (URISyntaxException e) {
            throw new FileReaderException("Invalid file URL", e);
        }
    }
}
