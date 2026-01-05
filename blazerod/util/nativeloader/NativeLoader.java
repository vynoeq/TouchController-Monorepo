package top.fifthlight.blazerod.util.nativeloader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class NativeLoader {
    private NativeLoader() {
    }

    private static final List<Path> androidPaths = List.of(
            Path.of("/", "system", "build.prop"),
            Path.of("/", "system", "bin", "app_process"),
            Path.of("/", "system", "framework", "framework.jar")
    );

    public static void load(ClassLoader classLoader, String targetName, String libName) throws IOException, LinkageError, UnsupportedOperationException {
        var systemName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        var systemArch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);

        String system;
        String extension;
        if (systemName.startsWith("linux")) {
            var isAndroid = false;
            for (var path : androidPaths) {
                try {
                    if (Files.exists(path)) {
                        isAndroid = true;
                        break;
                    }
                } catch (SecurityException ex) {
                }
            }

            if (isAndroid) {
                system = "android";
            } else {
                system = "linux";
            }
            extension = "so";
        } else if (systemName.startsWith("windows")) {
            system = "windows";
            extension = "dll";
        } else if (systemName.contains("android")) {
            // Most OpenJDK on Android declare themselves as Linux, but just in case
            system = "android";
            extension = "so";
        } else {
            throw new UnsupportedOperationException("Unsupported system: " + systemName);
        }

        var arch = switch (systemArch) {
            case "amd64", "x86_64" -> "x86_64";
            case "arm64", "aarch64" -> "aarch64";
            default -> null;
        };
        if (arch == null) {
            throw new UnsupportedOperationException("Unsupported architecture: " + systemArch);
        }

        var resourcePath = "%s_%s_%s/lib%s.%s".formatted(targetName, system, arch, libName, extension);
        try (var libraryUrl = classLoader.getResourceAsStream(resourcePath)) {
            if (libraryUrl == null) {
                throw new IOException("Failed to find physics library: " + resourcePath);
            }

            var outputPath = Files.createTempFile(libName + "_", "." + extension);

            Files.copy(libraryUrl, outputPath, StandardCopyOption.REPLACE_EXISTING);
            try {
                // Set file to read only after extracting
                if ("windows".equals(system)) {
                    var attributeView = Files.getFileAttributeView(outputPath, DosFileAttributeView.class);
                    attributeView.setReadOnly(true);
                } else {
                    var attributeView = Files.getFileAttributeView(outputPath, PosixFileAttributeView.class);
                    // 500
                    attributeView.setPermissions(Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_EXECUTE));
                }
            } catch (Exception ignored) {
            }

            System.load(outputPath.toAbsolutePath().toString());
            try {
                Files.delete(outputPath);
            } catch (Exception ignored) {
                // On Windows, the file is locked.
            }
        }
    }
}
