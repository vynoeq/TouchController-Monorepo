package top.fifthlight.fastmerger.scanner.classdeps;

import org.objectweb.asm.ClassReader;
import top.fifthlight.fastmerger.scanner.pathmap.PathMap;

import java.util.concurrent.*;

public class ClassDepsScanner {
    public static ClassInfo scan(PathMap pathMap, byte[] content) {
        var classReader = new ClassReader(content);
        var collector = new ClassInfoCollector(pathMap);
        classReader.accept(new ClassInfoVisitor(collector), ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return collector.getClassInfo();
    }
}
