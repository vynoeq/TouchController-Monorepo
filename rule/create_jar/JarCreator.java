package top.fifthlight.fabazel.jarcreator;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public class JarCreator {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: JarCreator <output-jar> [--entry path file]...");
            System.exit(1);
        }

        var outputPath = args[0];
        var outputJarPath = Paths.get(outputPath);

        try (var jarOut = new JarOutputStream(new FileOutputStream(outputJarPath.toFile()))) {
            for (var i = 1; i < args.length; i += 3) {
                if (i + 1 >= args.length) {
                    throw new IllegalArgumentException("Missing file for path: " + args[i]);
                }

                if (!args[i].equals("--entry")) {
                    throw new IllegalArgumentException("Expected --entry, got: " + args[i]);
                }

                var entryPath = args[i + 1];
                if (i + 2 >= args.length) {
                    throw new IllegalArgumentException("Missing file for entry: " + entryPath);
                }

                var filePath = args[i + 2];
                var inputPath = Paths.get(filePath);

                addEntryToJar(jarOut, entryPath, inputPath);
            }
        } catch (Exception e) {
            System.err.println("Error creating JAR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void addEntryToJar(JarOutputStream jarOut, String entryPath, Path filePath) throws IOException {
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("File not found: " + filePath);
        }

        var entry = new JarEntry(entryPath);
        entry.setCreationTime(FileTime.fromMillis(0));
        entry.setLastAccessTime(FileTime.fromMillis(0));
        entry.setLastModifiedTime(FileTime.fromMillis(0));
        entry.setTimeLocal(LocalDateTime.ofEpochSecond(0L, 0, ZoneOffset.UTC));
        jarOut.putNextEntry(entry);
        try (var inputStream = Files.newInputStream(filePath)) {
            inputStream.transferTo(jarOut);
        }
        jarOut.closeEntry();
    }
}