package top.fifthlight.mergetools.merger.plugin.expectactual;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import top.fifthlight.mergetools.merger.api.MergeEntry;
import top.fifthlight.mergetools.merger.api.Plugin;
import top.fifthlight.mergetools.merger.api.PreprocessEnvironment;
import top.fifthlight.mergetools.processor.ActualData;
import top.fifthlight.mergetools.processor.ExpectData;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class ExpectActualPlugin implements Plugin {
    @Override
    public int priority() {
        return 1000;
    }

    private static final String expectPrefix = "META-INF/expects/";
    private static final String actualPrefix = "META-INF/actuals/";
    private final ObjectMapper mapper = new ObjectMapper();
    private final HashMap<String, ExpectData> expectDataMap = new HashMap<>();
    private final HashMap<String, ActualData> actualDataMap = new HashMap<>();
    private final HashSet<String> factoryClasses = new HashSet<>();

    private static String internalNameToPath(String internalName) {
        if (!internalName.startsWith("L") || !internalName.endsWith(";")) {
            throw new IllegalArgumentException("Invalid binary name: " + internalName);
        }
        return internalName.substring(1, internalName.length() - 1);
    }

    private record MethodPair(String parameterTypes, String name) {
        public MethodPair(ExpectData.Constructor constructor) {
            this(Arrays.stream(constructor.parameters()).map(ExpectData.Constructor.Parameter::type).collect(Collectors.joining()), constructor.name());
        }

        public MethodPair(ActualData.Constructor constructor) {
            this(Arrays.stream(constructor.parameters()).map(ActualData.Constructor.Parameter::type).collect(Collectors.joining()), constructor.name());
        }
    }

    private record ExpectManifest(ExpectActualPlugin plugin, String interfaceFullQualifiedName,
                                  ExpectData expectData) implements MergeEntry {
        @Override
        public void write(OutputStream output) throws Exception {
            var actualData = plugin.actualDataMap.get(this.interfaceFullQualifiedName());
            var expectBinaryName = expectData.interfaceName();
            var interfaceTypeName = internalNameToPath(expectBinaryName);

            var expectConstructors = Arrays.stream(expectData.constructors()).collect(Collectors.toMap(
                    MethodPair::new,
                    constructor -> constructor,
                    (a, b) -> {
                        throw new IllegalStateException("Duplicate expect constructors: " + a + ", " + b);
                    }
            ));
            var actualConstructors = Arrays.stream(actualData.constructors()).collect(Collectors.toMap(
                    MethodPair::new,
                    constructor -> constructor,
                    (a, b) -> {
                        throw new IllegalStateException("Duplicate actual constructors: " + a + ", " + b);
                    }
            ));

            // Time of ASM magic
            var classWriter = new ClassWriter(0);
            classWriter.visit(Opcodes.V17, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, interfaceTypeName + "Factory", null, "java/lang/Object", null);
            for (var expectConstructorPair : expectConstructors.entrySet()) {
                var methodPair = expectConstructorPair.getKey();
                var expectConstructor = expectConstructorPair.getValue();
                var actualConstructor = actualConstructors.get(methodPair);
                if (actualConstructor == null) {
                    throw new IllegalStateException("No actual constructor found for method pair " + methodPair);
                }

                var generatedMethodDescriptor = "(" + methodPair.parameterTypes() + ")" + expectBinaryName;
                var methodVisitor = classWriter.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, expectConstructor.name(), generatedMethodDescriptor, null, null);
                methodVisitor.visitCode();

                var actualClassName = internalNameToPath(actualData.implementationName());
                switch (actualConstructor.type()) {
                    case CONSTRUCTOR -> {
                        methodVisitor.visitTypeInsn(Opcodes.NEW, actualClassName);
                        methodVisitor.visitInsn(Opcodes.DUP);
                    }
                    case STATIC_METHOD -> {
                    }
                }

                var parameters = expectConstructor.parameters();
                var variableLabels = new Label[parameters.length];
                for (var i = 0; i < parameters.length; i++) {
                    var parameter = parameters[i];

                    var label = new Label();
                    variableLabels[i] = label;
                    methodVisitor.visitLabel(label);

                    var objType = parameter.type().charAt(0);
                    switch (objType) {
                        case 'L' -> methodVisitor.visitVarInsn(Opcodes.ALOAD, i);
                        case 'Z', 'B', 'S', 'C', 'I' -> methodVisitor.visitVarInsn(Opcodes.ILOAD, i);
                        case 'J' -> methodVisitor.visitVarInsn(Opcodes.LLOAD, i);
                        case 'F' -> methodVisitor.visitVarInsn(Opcodes.FLOAD, i);
                        case 'D' -> methodVisitor.visitVarInsn(Opcodes.DLOAD, i);
                    }
                }

                switch (actualConstructor.type()) {
                    case CONSTRUCTOR -> {
                        var constructorDescriptor = "(" + methodPair.parameterTypes() + ")V";
                        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, actualClassName, "<init>", constructorDescriptor, false);
                    }
                    case STATIC_METHOD -> {
                        var actualMethodDescriptor = "(" + methodPair.parameterTypes() + ")" + actualConstructor.returnType();
                        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, actualClassName, expectConstructor.name(), actualMethodDescriptor, false);
                    }
                }
                var endLabel = new Label();
                methodVisitor.visitLabel(endLabel);
                methodVisitor.visitInsn(Opcodes.ARETURN);

                for (var i = 0; i < parameters.length; i++) {
                    var parameter = parameters[i];
                    methodVisitor.visitLocalVariable(parameter.name(), parameter.type(), null, variableLabels[i], endLabel, i);
                }
                methodVisitor.visitMaxs(parameters.length + 2, parameters.length);

                methodVisitor.visitEnd();
            }
            classWriter.visitEnd();

            var classData = classWriter.toByteArray();
            output.write(classData);
        }
    }

    @Override
    public boolean processJarEntry(JarFile file, JarEntry entry, PreprocessEnvironment environment) throws IOException {
        var name = entry.getName();
        if (name.startsWith(expectPrefix) && name.endsWith(".json")) {
            try (var inputStream = new BufferedInputStream(file.getInputStream(entry));
                 var reader = new InputStreamReader(inputStream)) {
                var expectData = mapper.readValue(reader, ExpectData.class);
                var interfaceFullQualifiedName = name.substring(expectPrefix.length(), name.length() - ".json".length());
                var interfaceClassPath = internalNameToPath(expectData.interfaceName());
                var interfaceFactoryPath = interfaceClassPath + "Factory.class";
                expectDataMap.put(interfaceFullQualifiedName, expectData);
                factoryClasses.add(interfaceFactoryPath);
                // If the factory class already exists, it will be overwritten
                environment.putMergeEntry(interfaceFactoryPath, new ExpectManifest(this, interfaceFullQualifiedName, expectData));
            }
            return true;
        }
        if (name.startsWith(actualPrefix) && name.endsWith(".json")) {
            try (var inputStream = new BufferedInputStream(file.getInputStream(entry));
                 var reader = new InputStreamReader(inputStream)) {
                var actualData = mapper.readValue(reader, ActualData.class);
                var interfaceFullQualifiedName = name.substring(actualPrefix.length(), name.length() - ".json".length());
                if (actualDataMap.containsKey(interfaceFullQualifiedName)) {
                    throw new IllegalStateException("Duplicate actual expectData: " + interfaceFullQualifiedName);
                }
                actualDataMap.put(interfaceFullQualifiedName, actualData);
            }
            return true;
        }
        // Filter generated factory classes out in case expect manifest entries are processed first
        return factoryClasses.contains(name);
    }

    @Override
    public void preSorting(Map<String, MergeEntry> mergeEntries, Map<String, String> manifestEntries) {
        for (var expectEntry : expectDataMap.entrySet()) {
            var key = expectEntry.getKey();
            var actualData = actualDataMap.get(key);
            if (actualData == null) {
                throw new IllegalStateException("Missing actual class for: " + key);
            }

            var actualSpiFactoryPath = internalNameToPath(actualData.spiFactoryName()) + ".class";
            if (!mergeEntries.containsKey(actualSpiFactoryPath)) {
                throw new IllegalStateException("Missing actual spi factory: " + actualSpiFactoryPath);
            }
            mergeEntries.remove(actualSpiFactoryPath);

            var spiManifestPath = "META-INF/services/" + key + "$Factory";
            if (!mergeEntries.containsKey(spiManifestPath)) {
                throw new IllegalStateException("Missing spi manifest: " + spiManifestPath);
            }
            mergeEntries.remove(spiManifestPath);
        }
    }
}
