package util;

import java.nio.file.Path;

public class Paths {
    public static Path getRootPath(String sourcePath) {
        Path path = Path.of(sourcePath);

        return path.getParent().getParent();
    }
}
