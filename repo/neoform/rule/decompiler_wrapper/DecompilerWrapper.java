import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.jar.*;

public class DecompilerWrapper {
    private static void setJarEntryTime(JarEntry entry) {
        entry.setTime(0L);
        entry.setCreationTime(FileTime.fromMillis(0L));
        entry.setLastModifiedTime(FileTime.fromMillis(0L));
    }

    public static void main(String[] args) throws Exception {
        String realMainClass = args[0];
        String outputPathStr = args[1];
        String[] originalArgs = Arrays.copyOfRange(args, 2, args.length);

        Class<?> clazz = Class.forName(realMainClass);
        Method mainMethod = clazz.getMethod("main", String[].class);
        mainMethod.invoke(null, (Object) originalArgs);

        Path path = Paths.get(outputPathStr);
        Path tempPath = Paths.get(outputPathStr + ".tmp");

        try (JarFile jarFile = new JarFile(path.toFile());
             JarOutputStream jos = new JarOutputStream(new FileOutputStream(tempPath.toFile()))) {

            List<JarEntry> entries = Collections.list(jarFile.entries());
            entries.sort(Comparator.comparing(JarEntry::getName));

            for (JarEntry entry : entries) {
                JarEntry newEntry = new JarEntry(entry.getName());
                setJarEntryTime(newEntry);

                jos.putNextEntry(newEntry);
                try (InputStream is = jarFile.getInputStream(entry)) {
                    byte[] buffer = new byte[65536];
                    int read;
                    while ((read = is.read(buffer)) != -1) {
                        jos.write(buffer, 0, read);
                    }
                }
                jos.closeEntry();
            }
        }

        Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING);
    }
}
