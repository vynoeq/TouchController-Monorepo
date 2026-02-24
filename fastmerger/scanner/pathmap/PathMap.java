package top.fifthlight.fastmerger.scanner.pathmap;

import net.jpountz.xxhash.XXHash64;
import net.jpountz.xxhash.XXHashFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class PathMap {
    private final ConcurrentHashMap<String, EntryImpl> rootEntries = new ConcurrentHashMap<>();
    private final AtomicInteger size = new AtomicInteger(0);

    public interface Entry {
        long hash();

        String name();

        byte[] nameBytes();

        byte[] fullNameBytes();

        String fullName();

        @Nullable
        PathMap.Entry parentEntry();

        @NotNull
        Map<String, ? extends Entry> entries();
    }

    private record EntryImpl(
            long hash,
            String name,
            String fullName,
            byte[] nameBytes,
            byte[] fullNameBytes,
            @Nullable PathMap.EntryImpl parentEntry,
            ConcurrentHashMap<String, EntryImpl> entries
    ) implements Entry {
        private static final XXHash64 HASHER = XXHashFactory.fastestInstance().hash64();

        public static EntryImpl of(String name, String fullName, @Nullable PathMap.EntryImpl parentEntry) {
            var nameBytes = name.getBytes(StandardCharsets.UTF_8);
            var fullNameBytes = fullName.getBytes(StandardCharsets.UTF_8);
            var hash = HASHER.hash(fullNameBytes, 0, nameBytes.length, 0);
            return new EntryImpl(hash, name, fullName, nameBytes, fullNameBytes, parentEntry, new ConcurrentHashMap<>());
        }

        @Override
        public int hashCode() {
            return fullName.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof EntryImpl entry) {
                return entry.fullName.equals(fullName);
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return "EntryImpl{" +
                    "nameBytes=" + Arrays.toString(nameBytes) +
                    ", fullName='" + fullName + '\'' +
                    ", name='" + name + '\'' +
                    ", hash=" + hash +
                    ", parentEntry=" + parentEntry +
                    '}';
        }
    }

    private EntryImpl insertEntry(@Nullable PathMap.EntryImpl parentEntry, String name, Supplier<String> fullName) {
        if (parentEntry == null) {
            return rootEntries.computeIfAbsent(name, k -> {
                size.incrementAndGet();
                return EntryImpl.of(k, fullName.get(), null);
            });
        } else {
            return parentEntry.entries.computeIfAbsent(name, k -> {
                size.incrementAndGet();
                return EntryImpl.of(k, fullName.get(), parentEntry);
            });
        }
    }

    public Entry getOrCreate(String name) {
        if (Objects.requireNonNull(name).isEmpty()) {
            throw new IllegalArgumentException("Input name is empty");
        }
        EntryImpl currentEntry = null;
        // split() will run in fast path
        var segments = name.split("/");
        for (var i = 0; i < segments.length; i++) {
            var segment = segments[i];
            var segmentIndex = i;
            currentEntry = insertEntry(currentEntry, segment, () -> {
                if (segmentIndex == segments.length - 1) {
                    return name;
                } else {
                    return String.join("/", Arrays.copyOf(segments, segmentIndex + 1));
                }
            });
        }
        return currentEntry;
    }

    public record Result(Map<String, ? extends Entry> rootEntries, int size) {
        @Nullable
        public PathMap.Entry get(String name) {
            if (name == null || name.isEmpty()) {
                return null;
            }
            Entry currentEntry = null;
            var segments = name.split("/");
            for (var segment : segments) {
                if (currentEntry == null) {
                    currentEntry = rootEntries.get(segment);
                } else {
                    currentEntry = currentEntry.entries().get(segment);
                }
            }
            return currentEntry;
        }
    }

    public Result finish() {
        return new Result(Collections.unmodifiableMap(rootEntries), size.get());
    }
}
