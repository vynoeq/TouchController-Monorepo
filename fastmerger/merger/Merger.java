package top.fifthlight.fastmerger.merger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Merger {
    private final List<PathEntry> classPath;
    private final List<PathEntry> input;
    private final Path output;
    private final int release;

    public record PathEntry(Path jar, Path bdeps) {}

    private Merger(List<PathEntry> classPath, List<PathEntry> input, Path output, int release) {
        this.classPath = classPath;
        this.input = input;
        this.output = output;
        this.release = release;
    }

    public static class Builder {
        private final List<PathEntry> classPath = new ArrayList<>();
        private final List<PathEntry> input = new ArrayList<>();
        private Path output = null;
        private int release = -1;

        public void addClassPath(Path jar,  Path bdeps) {
            classPath.add(new PathEntry(jar, bdeps));
        }

        public void addInput(Path jar, Path bdeps) {
            input.add(new PathEntry(jar, bdeps));
        }

        public void setOutput(Path output) {
            this.output = Objects.requireNonNull(output);
        }

        public void setRelease(int release) {
            if (release < 1) {
                throw new IllegalArgumentException("release must be greater than zero");
            }
            this.release = release;
        }

        public Merger build() {
            return new Merger(classPath, input, output, release);
        }
    }

    private void scanEntry(PathEntry entry, boolean merge) {

    }

    public void run() throws IOException {
        for (var classPathEntry : classPath) {
            scanEntry(classPathEntry, false);
        }
        for (var inputEntry : input) {
            scanEntry(inputEntry, true);
        }
    }
}
