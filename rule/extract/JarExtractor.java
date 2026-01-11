package top.fifthlight.fabazel.jarextractor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.jar.JarFile;

public class JarExtractor {
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            throw new IllegalArgumentException("Usage: <input_jar> <entry_path> <output_file>");
        }

        var jarPath = Paths.get(args[0]);
        var entryPath = args[1];
        var outputPath = Paths.get(args[2]);

        try {
            Files.createDirectories(outputPath.getParent());
        } catch (IOException e) {
            throw new RuntimeException("Failed to create output directories", e);
        }

        try (var jar = new JarFile(jarPath.toFile())) {
            var entry = jar.getJarEntry(entryPath);
            if (entry == null) {
                throw new RuntimeException("Entry '" + entryPath + "' not found in JAR");
            }

            try (var input = jar.getInputStream(entry);
                 var output = Files.newOutputStream(outputPath)) {
                input.transferTo(output);
            }
        }
    }
}
