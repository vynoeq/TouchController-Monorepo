package top.fifthlight.fastmerger;

import org.objectweb.asm.ClassReader;
import top.fifthlight.fastmerger.pkgdeps.PkgDepsVisitor;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class Main {
    public static void main(String[] args) throws IOException {
        try (var jis = new JarInputStream(new FileInputStream(args[0]))) {
            JarEntry entry;
            while ((entry = jis.getNextJarEntry()) != null) {
                if (!entry.getName().toLowerCase(Locale.ROOT).endsWith(".class")) {
                    continue;
                }
                var classReader = new ClassReader(jis);
                var depsVisitor = new PkgDepsVisitor((className, depName) -> {
                    System.out.println(className + " -> " + depName);
                });
                classReader.accept(depsVisitor, 0);
            }
        }
    }
}