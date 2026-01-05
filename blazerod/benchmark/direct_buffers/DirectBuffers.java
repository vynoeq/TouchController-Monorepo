package top.fifthlight.blazerod.benchmark.directbuffers;

import org.openjdk.jmh.annotations.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class DirectBuffers {
    private static final int BUFFER_SIZE = 4096;

    @State(Scope.Benchmark)
    public static class DirectBufferState implements AutoCloseable {
        private final ByteBuffer buffer = BufferGenerator.generateBufferDirect(1, BUFFER_SIZE).order(ByteOrder.nativeOrder());
        public final FloatBuffer floatBuffer = buffer.asFloatBuffer();

        @Override
        public void close() {
            BufferGenerator.releaseDirectBuffer(buffer);
        }
    }

    @TearDown
    public void tearDownDirectBuffer(DirectBufferState state) {
        state.close();
    }

    @State(Scope.Benchmark)
    public static class HeapBufferState {
        private final float[] buffer = BufferGenerator.generateByteBufferHeap(1, BUFFER_SIZE);
        public final FloatBuffer floatBuffer = FloatBuffer.wrap(buffer);
    }

    float directValue;
    @Benchmark
    public void testReadDirectBuffer(DirectBufferState state) {
        for (var i = 0; i < BUFFER_SIZE; i++) {
            directValue = state.floatBuffer.get(i);
        }
    }

    float heapValue;
    @Benchmark
    public void testReadHeapBuffer(HeapBufferState state) {
        for (var i = 0; i < BUFFER_SIZE; i++) {
            heapValue = state.floatBuffer.get(i);
        }
    }
}
