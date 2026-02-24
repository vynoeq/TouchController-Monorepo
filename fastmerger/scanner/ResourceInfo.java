package top.fifthlight.fastmerger.scanner;

import top.fifthlight.fastmerger.scanner.pathmap.PathMap;

public record ResourceInfo(int flag, PathMap.Entry name, int crc32, int dataOffset, int compressedSize,
                           int uncompressedSize, short compressMethod, byte[] data) {
}
