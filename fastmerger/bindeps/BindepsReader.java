package top.fifthlight.fastmerger.bindeps;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.*;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class BindepsReader {
    private final ByteBuffer dataBuffer;

    private final int stringPoolSize;
    private final int resourceInfoSize;
    private final int classInfoSize;
    private final int resourceInfoOffset;
    private final int classInfoOffset;

    public BindepsReader(Path inputPath) throws IOException {
        var buffer = ByteBuffer.allocateDirect(BindepsConstants.HEADER_SIZE);
        try (var channel = FileChannel.open(inputPath, StandardOpenOption.READ)) {
            while (buffer.hasRemaining()) {
                if (channel.read(buffer) == -1) {
                    throw new IOException("Unexpected EOF in header");
                }
            }
            buffer.flip();

            for (var i = 0; i < BindepsConstants.MAGIC.length; i++) {
                var b = buffer.get();
                if (b != BindepsConstants.MAGIC[i]) {
                    throw new IOException("Invalid magic at byte %d: expected 0x%02X, got 0x%02X".formatted(i, BindepsConstants.MAGIC[i], b));
                }
            }

            var version = buffer.getInt();
            if (version != BindepsConstants.VERSION) {
                throw new IOException("Invalid version: expected %d, got %d".formatted(BindepsConstants.VERSION, version));
            }

            stringPoolSize = buffer.getInt();
            if (stringPoolSize < 0) {
                throw new IOException("Invalid string pool size: %d".formatted(stringPoolSize));
            }
            resourceInfoSize = buffer.getInt();
            if (resourceInfoSize < 0) {
                throw new IOException("Invalid resource info size: %d".formatted(resourceInfoSize));
            }
            classInfoSize = buffer.getInt();
            if (classInfoSize < 0) {
                throw new IOException("Invalid class info size: %d".formatted(classInfoSize));
            }

            var heapSize = buffer.getInt();
            if (heapSize < 0) {
                throw new IOException("Invalid heap size: %d".formatted(heapSize));
            }

            resourceInfoOffset = BindepsConstants.STRING_RECORD_SIZE * stringPoolSize;
            classInfoOffset = resourceInfoOffset + BindepsConstants.RESOURCE_RECORD_SIZE * resourceInfoSize;

            buffer.position(buffer.position() + 20); // Skip padding
            var dataSize = BindepsConstants.STRING_RECORD_SIZE * stringPoolSize + BindepsConstants.RESOURCE_RECORD_SIZE * resourceInfoSize + BindepsConstants.CLASS_RECORD_SIZE * classInfoSize + heapSize;

            ByteBuffer dataBuffer;
            try {
                dataBuffer = channel.map(FileChannel.MapMode.READ_ONLY, BindepsConstants.HEADER_SIZE, dataSize);
            } catch (IllegalArgumentException | UnsupportedOperationException | IOException e) {
                // Fallback to reading buffer into memory
                dataBuffer = ByteBuffer.allocateDirect(dataSize);
                channel.position(BindepsConstants.HEADER_SIZE);
                while (dataBuffer.hasRemaining()) {
                    if (channel.read(dataBuffer) == -1) {
                        throw new IOException("Unexpected EOF in data");
                    }
                }
                dataBuffer.flip();
            }
            this.dataBuffer = dataBuffer.asReadOnlyBuffer();
        }
    }

    private static String decodeCharBuffer(ByteBuffer buffer, int offset, int length) {
        return StandardCharsets.UTF_8.decode(buffer.slice(offset, length)).toString();
    }

    public String readHeapString(int offset, int length) {
        return decodeCharBuffer(dataBuffer, offset, length);
    }

    public IntBuffer readHeapIntBuffer(int offset, int length) {
        return dataBuffer.slice(offset, length * 4).order(ByteOrder.BIG_ENDIAN).asReadOnlyBuffer().asIntBuffer();
    }

    public int readHeapInt(int offset) {
        return dataBuffer.getInt(offset);
    }

    public byte[] readHeapBytes(int offset, int length) {
        var bytes = new byte[length];
        dataBuffer.slice(offset, length).get(bytes);
        return bytes;
    }

    public class StringPoolReader {
        public long getHash(int offset) {
            return BindepsReader.this.dataBuffer.getLong(offset);
        }

        public int getParentIndex(int offset) {
            return BindepsReader.this.dataBuffer.getInt(offset + 8);
        }

        public int getHeapOffset(int offset) {
            return BindepsReader.this.dataBuffer.getInt(offset + 12) - BindepsConstants.HEADER_SIZE;
        }

        public int getNameLength(int offset) {
            return Short.toUnsignedInt(BindepsReader.this.dataBuffer.getShort(offset + 16));
        }

        public int getFullNameLength(int offset) {
            return Short.toUnsignedInt(BindepsReader.this.dataBuffer.getShort(offset + 18));
        }
    }

    public final StringPoolReader stringPoolReader = new StringPoolReader();

    public int getStringPoolOffset(int index) {
        return BindepsConstants.STRING_RECORD_SIZE * index;
    }

    public class StringPoolEntry {
        private final int index;

        private final long hash;
        private final int parentIndex;
        private final int heapOffset;
        private final int nameLength;
        private final int fullNameLength;

        private StringPoolEntry parent = null;
        private String name = null;
        private String fullName = null;

        public StringPoolEntry(int index) {
            var reader = BindepsReader.this.stringPoolReader;
            var offset = getStringPoolOffset(index);
            this.index = index;
            this.hash = reader.getHash(offset);
            this.parentIndex = reader.getParentIndex(offset);
            this.heapOffset = reader.getHeapOffset(offset);
            this.nameLength = reader.getNameLength(offset);
            this.fullNameLength = reader.getFullNameLength(offset);
        }

        public long getHash() {
            return hash;
        }

        public int getIndex() {
            return index;
        }

        public int getParentIndex() {
            return parentIndex;
        }

        public StringPoolEntry getParent() {
            if (parent == null) {
                parent = new StringPoolEntry(parentIndex);
            }
            return parent;
        }

        public String getName() {
            if (name == null) {
                name = readHeapString(heapOffset, nameLength);
            }
            return name;
        }

        public String getFullName() {
            if (fullName == null) {
                fullName = readHeapString(heapOffset + nameLength, fullNameLength);
            }
            return fullName;
        }
    }

    public int getStringPoolSize() {
        return stringPoolSize;
    }

    public StringPoolEntry getStringPoolEntry(int index) {
        return new StringPoolEntry(index);
    }

    public class ResourceInfoReader {
        public int getFlag(int offset) {
            return BindepsReader.this.dataBuffer.getInt(offset);
        }

        public int getNameIndex(int offset) {
            return BindepsReader.this.dataBuffer.getInt(offset + 4);
        }

        public int getCrc32(int offset) {
            return BindepsReader.this.dataBuffer.getInt(offset + 8);
        }

        public int getDataOffset(int offset) {
            return BindepsReader.this.dataBuffer.getInt(offset + 12);
        }

        public int getCompressedSize(int offset) {
            return BindepsReader.this.dataBuffer.getInt(offset + 16);
        }

        public int getUncompressedSize(int offset) {
            return BindepsReader.this.dataBuffer.getInt(offset + 20);
        }

        public short getCompressMethod(int offset) {
            return BindepsReader.this.dataBuffer.getShort(offset + 24);
        }
    }

    public final ResourceInfoReader resourceInfoReader = new ResourceInfoReader();

    public int getResourceInfoOffset(int index) {
        return resourceInfoOffset + BindepsConstants.RESOURCE_RECORD_SIZE * index;
    }

    public int getResourceInfoSize() {
        return resourceInfoSize;
    }

    public class ResourceInfoEntry {
        private final int index;

        private final int flag;
        private final int nameIndex;
        private final int crc32;
        private final int dataOffset;
        private final int compressedSize;
        private final int uncompressedSize;
        private final short compressMethod;

        private StringPoolEntry name = null;
        private byte[] data = null;

        public ResourceInfoEntry(int index) {
            this.index = index;
            var reader = BindepsReader.this.resourceInfoReader;
            var offset = getResourceInfoOffset(index);
            this.flag = reader.getFlag(offset);
            this.nameIndex = reader.getNameIndex(offset);
            this.crc32 = reader.getCrc32(offset);
            this.dataOffset = reader.getDataOffset(offset);
            this.compressedSize = reader.getCompressedSize(offset);
            this.uncompressedSize = reader.getUncompressedSize(offset);
            this.compressMethod = reader.getCompressMethod(offset);
        }

        public int getIndex() {
            return index;
        }

        public int getFlag() {
            return flag;
        }

        public StringPoolEntry getName() {
            if (name == null) {
                name = new StringPoolEntry(nameIndex);
            }
            return name;
        }

        public int getCrc32() {
            return crc32;
        }

        public int getDataOffset() {
            return dataOffset;
        }

        public int getCompressedSize() {
            return compressedSize;
        }

        public int getUncompressedSize() {
            return uncompressedSize;
        }

        public short getCompressMethod() {
            return compressMethod;
        }

        public byte[] getData() {
            if (data == null) {
                if ((flag & BindepsConstants.RESOURCE_FLAG_INLINE) == 0) {
                    return null;
                }
                var heapOffset = dataOffset - BindepsConstants.HEADER_SIZE;
                data = readHeapBytes(heapOffset, uncompressedSize);
            }
            return data;
        }
    }

    public ResourceInfoEntry getResourceInfoEntry(int index) {
        return new ResourceInfoEntry(index);
    }

    public class ClassInfoReader {
        public int getNameIndex(int offset) {
            return BindepsReader.this.dataBuffer.getInt(offset);
        }

        public int getSuperIndex(int offset) {
            return BindepsReader.this.dataBuffer.getInt(offset + 4);
        }

        public int getAccess(int offset) {
            return BindepsReader.this.dataBuffer.getInt(offset + 8);
        }

        public int getResourceIndex(int offset) {
            return BindepsReader.this.dataBuffer.getInt(offset + 12);
        }

        public int getRelease(int offset) {
            return BindepsReader.this.dataBuffer.getInt(offset + 16);
        }

        public int getInterfaceOffset(int offset) {
            return BindepsReader.this.dataBuffer.getInt(offset + 20);
        }

        public int getInterfaceCount(int offset) {
            return BindepsReader.this.dataBuffer.getInt(offset + 24);
        }

        public int getAnnotationOffset(int offset) {
            return BindepsReader.this.dataBuffer.getInt(offset + 28);
        }

        public int getAnnotationCount(int offset) {
            return BindepsReader.this.dataBuffer.getInt(offset + 32);
        }

        public int getDependenciesOffset(int offset) {
            return BindepsReader.this.dataBuffer.getInt(offset + 36);
        }

        public int getDependenciesCount(int offset) {
            return BindepsReader.this.dataBuffer.getInt(offset + 40);
        }
    }

    public final ClassInfoReader classInfoReader = new ClassInfoReader();

    public int getClassInfoOffset(int index) {
        return classInfoOffset + BindepsConstants.CLASS_RECORD_SIZE * index;
    }

    public class ClassInfoEntry {
        private static final IntBuffer EMPTY_INT_BUFFER = IntBuffer.wrap(new int[0]).asReadOnlyBuffer();

        private final int index;

        private final int nameIndex;
        private final int superIndex;
        private final int access;
        private final int resourceIndex;
        private final int release;
        private final int interfaceOffset;
        private final int interfaceCount;
        private final int annotationOffset;
        private final int annotationCount;
        private final int dependenciesOffset;
        private final int dependenciesCount;

        private StringPoolEntry name = null;
        private StringPoolEntry superClass = null;
        private IntBuffer interfaceIndices = null;
        private IntBuffer annotationIndices = null;
        private IntBuffer dependenciesIndices = null;
        private StringPoolEntry[] interfaces = null;
        private StringPoolEntry[] annotations = null;
        private StringPoolEntry[] dependencies = null;
        private ResourceInfoEntry resource = null;

        public ClassInfoEntry(int index) {
            this.index = index;
            var reader = BindepsReader.this.classInfoReader;
            var offset = getClassInfoOffset(index);
            this.nameIndex = reader.getNameIndex(offset);
            this.superIndex = reader.getSuperIndex(offset);
            this.access = reader.getAccess(offset);
            this.resourceIndex = reader.getResourceIndex(offset);
            this.release = reader.getRelease(offset);
            this.interfaceOffset = reader.getInterfaceOffset(offset);
            this.interfaceCount = reader.getInterfaceCount(offset);
            this.annotationOffset = reader.getAnnotationOffset(offset);
            this.annotationCount = reader.getAnnotationCount(offset);
            this.dependenciesOffset = reader.getDependenciesOffset(offset);
            this.dependenciesCount = reader.getDependenciesCount(offset);
        }

        public int getIndex() {
            return index;
        }

        public int getNameIndex() {
            return nameIndex;
        }

        public StringPoolEntry getName() {
            if (name == null) {
                name = new StringPoolEntry(nameIndex);
            }
            return name;
        }

        public int getSuperIndex() {
            return superIndex;
        }

        @Nullable
        public StringPoolEntry getSuperClass() {
            if (superIndex == -1) {
                return null;
            }
            if (superClass == null) {
                superClass = new StringPoolEntry(superIndex);
            }
            return superClass;
        }

        public int getAccess() {
            return access;
        }

        public int getResourceIndex() {
            return resourceIndex;
        }

        @Nullable
        public ResourceInfoEntry getResourceInfo() {
            if (resource == null) {
                resource = getResourceInfoEntry(resourceIndex);
            }
            return resource;
        }


        public int getRelease() {
            return release;
        }

        public IntBuffer getInterfaceIndices() {
            if (interfaceIndices == null) {
                if (interfaceCount == 0) {
                    interfaceIndices = EMPTY_INT_BUFFER;
                } else {
                    interfaceIndices = readHeapIntBuffer(interfaceOffset - BindepsConstants.HEADER_SIZE, interfaceCount);
                }
            }
            return interfaceIndices;
        }

        public StringPoolEntry[] getInterfaces() {
            if (interfaces == null) {
                var interfaceIndices = getInterfaceIndices();
                interfaces = new StringPoolEntry[interfaceCount];
                for (var i = 0; i < interfaceCount; i++) {
                    interfaces[i] = new StringPoolEntry(interfaceIndices.get(i));
                }
            }
            return interfaces;
        }

        public IntBuffer getAnnotationIndices() {
            if (annotationIndices == null) {
                if (annotationCount == 0) {
                    annotationIndices = EMPTY_INT_BUFFER;
                } else {
                    annotationIndices = readHeapIntBuffer(annotationOffset - BindepsConstants.HEADER_SIZE, annotationCount);
                }
            }
            return annotationIndices;
        }

        public StringPoolEntry[] getAnnotations() {
            if (annotations == null) {
                var annotationIndices = getAnnotationIndices();
                annotations = new StringPoolEntry[annotationCount];
                for (var i = 0; i < annotationCount; i++) {
                    annotations[i] = new StringPoolEntry(annotationIndices.get(i));
                }
            }
            return annotations;
        }

        public IntBuffer getDependenciesIndices() {
            if (dependenciesIndices == null) {
                if (dependenciesCount == 0) {
                    dependenciesIndices = EMPTY_INT_BUFFER;
                } else {
                    dependenciesIndices = readHeapIntBuffer(dependenciesOffset - BindepsConstants.HEADER_SIZE, dependenciesCount);
                }
            }
            return dependenciesIndices;
        }

        public StringPoolEntry[] getDependencies() {
            if (dependencies == null) {
                var dependenciesIndices = getDependenciesIndices();
                dependencies = new StringPoolEntry[dependenciesCount];
                for (var i = 0; i < dependenciesCount; i++) {
                    dependencies[i] = new StringPoolEntry(dependenciesIndices.get(i));
                }
            }
            return dependencies;
        }
    }

    public int getClassInfoSize() {
        return classInfoSize;
    }

    public ClassInfoEntry getClassInfoEntry(int index) {
        return new ClassInfoEntry(index);
    }
}
