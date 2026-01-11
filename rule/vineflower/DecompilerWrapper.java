import net.fabricmc.fernflower.api.IFabricJavadocProvider;
import net.fabricmc.loom.decompilers.vineflower.TinyJavadocProvider;
import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.decompiler.PrintStreamLogger;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

@Command
public class DecompilerWrapper implements Callable<Integer> {
    @Option(names = "-m", description = "Mappings file, must be in tiny format")
    File mappings;
    @Parameters
    File[] inputFiles;
    @Parameters
    File outputFile;

    private static class InMemorySaver implements IResultSaver {
        public final ConcurrentHashMap<String, String> classes = new ConcurrentHashMap<>();

        @Override
        public void saveFolder(String path) {
        }

        @Override
        public void copyFile(String source, String path, String entryName) {
        }

        @Override
        public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {
        }

        @Override
        public void createArchive(String path, String archiveName, Manifest manifest) {
        }

        @Override
        public void saveDirEntry(String path, String archiveName, String entryName) {
        }

        @Override
        public void copyEntry(String source, String path, String archiveName, String entry) {
        }

        @Override
        public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content) {
            classes.put(entryName, content);
        }

        @Override
        public void closeArchive(String path, String archiveName) {
        }
    }

    private static void setJarEntryTime(JarEntry entry) {
        entry.setTime(0L);
        entry.setCreationTime(FileTime.fromMillis(0L));
        entry.setLastModifiedTime(FileTime.fromMillis(0L));
    }

    @Override
    public Integer call() {
        try {
            Map<String, Object> options = new HashMap<>(
                    Map.of(
                            IFernflowerPreferences.DECOMPILE_GENERIC_SIGNATURES, "1",
                            IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1",
                            IFernflowerPreferences.REMOVE_SYNTHETIC, "1",
                            IFernflowerPreferences.LOG_LEVEL, "warn",
                            // Bazel tends to run decompilers in parallel
                            IFernflowerPreferences.THREADS, String.valueOf(Math.min(4, Runtime.getRuntime().availableProcessors())),
                            IFernflowerPreferences.INDENT_STRING, "\t"
                    )
            );
            if (mappings != null) {
                options.put(IFabricJavadocProvider.PROPERTY_NAME, new TinyJavadocProvider(mappings));
            }

            try (var saver = new InMemorySaver()) {
                var vineFlower = new Fernflower(saver, options, new PrintStreamLogger(System.err));
                for (var inputFile : inputFiles) {
                    vineFlower.addSource(inputFile);
                }
                try {
                    vineFlower.decompileContext();
                } finally {
                    vineFlower.clearContext();
                }

                try (var jos = new JarOutputStream(new FileOutputStream(outputFile))) {
                    saver.classes.entrySet().stream()
                            .sorted(Map.Entry.comparingByKey())
                            .forEachOrdered(entry -> {
                                var newEntry = new JarEntry(entry.getKey());
                                setJarEntryTime(newEntry);
                                try {
                                    jos.putNextEntry(newEntry);
                                    jos.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                                    jos.closeEntry();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                }
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return 1;
        }
    }

    public static void main(String[] args) {
        var exitCode = new CommandLine(new DecompilerWrapper()).execute(args);
        System.exit(exitCode);
    }
}
