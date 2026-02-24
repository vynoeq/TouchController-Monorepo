package top.fifthlight.fastmerger.scanner.classdeps;

import top.fifthlight.fastmerger.scanner.pathmap.PathMap;

public record ClassInfo(
        PathMap pathMap,
        PathMap.Entry entry,
        int accessFlag,
        PathMap.Entry superClass,
        PathMap.Entry[] interfaces,
        PathMap.Entry[] annotations,
        PathMap.Entry[] dependencies
) {
    public String getFullName() {
        return entry.fullName();
    }
}
