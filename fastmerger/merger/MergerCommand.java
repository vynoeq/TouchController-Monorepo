package top.fifthlight.fastmerger.merger;

import picocli.CommandLine;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import static picocli.CommandLine.*;

@Command(name = "Merger", description = "Merge JARs with bdeps files.", mixinStandardHelpOptions = true)
public class MergerCommand implements Callable<Integer> {
    @Option(names = {"-c", "--classpath"}, description = "Add an classpath JAR entry with .bdeps file", arity = "2")
    List<Path[]> classPath;

    @Option(names = {"-i", "--input"}, description = "Add an input JAR entry with .bdeps file", arity = "2")
    List<Path[]> input;

    @Option(names = {"-o", "--output"}, description = "Specify output JAR file")
    Path output;

    @Option(names = {"-r", "--release"}, description = "Specify target Java version, used by Multi-Release JAR", required = true)
    Integer release;

    @Override
    public Integer call() throws Exception {
        var builder = new Merger.Builder();
        for (var classPathEntry : classPath) {
            builder.addClassPath(classPathEntry[0], classPathEntry[1]);
        }
        for (var inputEntry : input) {
            builder.addInput(inputEntry[0], inputEntry[1]);
        }
        builder.setOutput(output);
        var merger = builder.build();
        merger.run();
        return 0;
    }

    public static int invoke(PrintWriter out, String... args) {
        return new CommandLine(new MergerCommand()).execute(args);
    }

    public static void main(String... args) {
        System.exit(invoke(new PrintWriter(System.out), args));
    }
}
