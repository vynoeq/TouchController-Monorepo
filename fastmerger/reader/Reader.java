package top.fifthlight.fastmerger.reader;

import picocli.CommandLine;
import top.fifthlight.fastmerger.bindeps.BindepsReader;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import static picocli.CommandLine.*;

@Command(name = "scanner", mixinStandardHelpOptions = true)
public class Reader implements Callable<Integer> {
    @Parameters(index = "0", description = ".bdep file to be read")
    Path inputFile;

    @Override
    public Integer call() throws Exception {
        var reader = new BindepsReader(inputFile);
        for (var i = 0; i < reader.getResourceInfoSize(); i++) {
            var entry = reader.getResourceInfoEntry(i);
            System.out.println("Resource name: " + entry.getName().getFullName());
            System.out.printf("    Flag: 0x%02X%n", entry.getFlag());
            System.out.println("    CRC32: " + entry.getCrc32());

            var data = entry.getData();
            if (data != null) {
                System.out.println("    Data (inline): " + data.length + " bytes");
            } else {
                System.out.println("    Data offset: " + entry.getDataOffset());
                System.out.println("    Compressed size: " + entry.getCompressedSize());
                System.out.println("    Uncompressed size: " + entry.getUncompressedSize());
                System.out.println("    Compress method: " + entry.getCompressMethod());
            }
        }

        for (var i = 0; i < reader.getClassInfoSize(); i++) {
            var entry = reader.getClassInfoEntry(i);
            System.out.println("Class name: " + entry.getName().getFullName());
            System.out.println("Release: " + entry.getRelease());

            var superClass = entry.getSuperClass();
            if (superClass == null) {
                System.out.println("    Super class: null");
            } else {
                System.out.println("    Super class: " + superClass.getFullName());
            }

            System.out.println("    Interfaces:");
            for (var interfaceEntry : entry.getInterfaces()) {
                System.out.println("        " + interfaceEntry.getFullName());
            }

            System.out.println("    Annotations:");
            for (var annotationEntry : entry.getAnnotations()) {
                System.out.println("        " + annotationEntry.getFullName());
            }

            System.out.println("    Dependencies:");
            for (var dependency : entry.getDependencies()) {
                System.out.println("        " + dependency.getFullName());
            }

            var resourceInfo = entry.getResourceInfo();
            System.out.println("    Resource:");
            System.out.println("        Name: " + resourceInfo.getName().getFullName());
        }
        return 0;
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new Reader()).execute(args));
    }
}
