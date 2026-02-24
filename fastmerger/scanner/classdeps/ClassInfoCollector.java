package top.fifthlight.fastmerger.scanner.classdeps;

import top.fifthlight.fastmerger.scanner.pathmap.PathMap;

import java.util.ArrayList;
import java.util.HashSet;

public class ClassInfoCollector implements ClassInfoVisitor.Consumer {
    private final PathMap pathMap;
    private PathMap.Entry entry;
    private int accessFlag;
    private PathMap.Entry superClass;
    private final ArrayList<PathMap.Entry> interfaces = new ArrayList<>();
    private final HashSet<PathMap.Entry> annotations = new HashSet<>();
    private final HashSet<PathMap.Entry> dependencies = new HashSet<>(16);

    public ClassInfoCollector(PathMap pathMap) {
        this.pathMap = pathMap;
    }

    @Override
    public void acceptClassInfo(String className, int accessFlag, String superClass) {
        entry = pathMap.getOrCreate(className);
        this.accessFlag = accessFlag;
        if (superClass != null) {
            this.superClass = pathMap.getOrCreate(superClass);
        }
    }

    @Override
    public void acceptInterface(String interfaceName) {
        interfaces.add(pathMap.getOrCreate(interfaceName));
    }

    @Override
    public void acceptAnnotation(String annotationName) {
        annotations.add(pathMap.getOrCreate(annotationName));
    }

    @Override
    public void acceptClassDependency(String dependencyName) {
        dependencies.add(pathMap.getOrCreate(dependencyName));
    }

    public ClassInfo getClassInfo() {
        return new ClassInfo(
                pathMap, entry, accessFlag, superClass,
                interfaces.toArray(new PathMap.Entry[0]),
                annotations.toArray(new PathMap.Entry[0]),
                dependencies.toArray(new PathMap.Entry[0])
        );
    }
}
