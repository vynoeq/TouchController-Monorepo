package top.fifthlight.blazerod.benchmark.directbuffers;

import top.fifthlight.blazerod.util.nativeloader.NativeLoader;

import java.io.IOException;
import java.nio.ByteBuffer;

public class BufferGenerator {
    static {
        try {
            NativeLoader.load(BufferGenerator.class.getClassLoader(), "buffer_generator_native", "buffer_generator_native");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static native ByteBuffer generateBufferDirect(int seed, int floats);
    public static native void releaseDirectBuffer(ByteBuffer buffer);
    public static native float[] generateByteBufferHeap(int seed, int floats);
}
