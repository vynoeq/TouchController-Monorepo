package top.fifthlight.fastmerger.bindeps;

public class BindepsConstants {
    private BindepsConstants() {
    }

    /**
     * File magic. 'BINDEPS' + 0x03
     */
    public static final byte[] MAGIC = new byte[]{'B', 'I', 'N', 'D', 'E', 'P', 'S', 0x03};

    /**
     * File version. Current version is 3.
     */
    public static final int VERSION = 3;

    /**
     * Size of file header.
     */
    public static final int HEADER_SIZE = 48; // 8(magic) + 4(version) + 4(string pool size) + 4(resource info size) + 4(class info size) + 4(heap size) + 20(padding)

    /**
     * Size of a string pool record.
     */
    public static final int STRING_RECORD_SIZE = 24; // 8(hash) + 4(parent) + 4(offset) + 2(name len) + 2(full name len) + 4(padding)

    /**
     * Size of a resource info record.
     */
    public static final int RESOURCE_RECORD_SIZE = 32; // 4(flag) + 4(name) + 4(crc32) + 4(data offset) + 4(compressed size) + 4(uncompressed size) + 2(compress method) + 6(padding)

    /**
     * Size of a class info record.
     */
    public static final int CLASS_RECORD_SIZE = 48;  // 11 ints: name, super, access, resource index, release + 3 x (offset, count) + 8(padding)

    /**
     * This resource is stored in heap.
     */
    @SuppressWarnings("PointlessBitwiseExpression")
    public static final int RESOURCE_FLAG_INLINE = 1 << 0;

    /**
     * This resource is a class file. RESOURCE_FLAG_RESOURCE must not be set.
     */
    public static final int RESOURCE_FLAG_CLASS = 1 << 1;

    /**
     * This resource is a resource file. RESOURCE_FLAG_CLASS must not be set.
     */
    public static final int RESOURCE_FLAG_RESOURCE = 1 << 2;

    /**
     * This resource is a MANIFEST.MF file. RESOURCE_FLAG_RESOURCE must be set.
     */
    public static final int RESOURCE_FLAG_MANIFEST = 1 << 3;

    /**
     * This resource is an SPI config file. RESOURCE_FLAG_RESOURCE must be set.
     */
    public static final int RESOURCE_FLAG_SPI = 1 << 4;

    /**
     * This resource is a proguard config file. RESOURCE_FLAG_RESOURCE must be set.
     */
    public static final int RESOURCE_FLAG_PROGUARD = 1 << 5;

    /**
     * This resource is a JAR signature file. RESOURCE_FLAG_RESOURCE must be set.
     */
    public static final int RESOURCE_FLAG_SIGNATURE = 1 << 6;

    public static final int RESOURCE_FLAG_MAX = RESOURCE_FLAG_SIGNATURE;
}
