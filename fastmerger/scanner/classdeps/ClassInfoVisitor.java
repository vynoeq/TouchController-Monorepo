package top.fifthlight.fastmerger.scanner.classdeps;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.*;

public class ClassInfoVisitor extends ClassVisitor {
    private final Consumer consumer;

    public interface Consumer {
        void acceptClassInfo(String className, int accessFlag, String superClass);

        void acceptInterface(String interfaceName);

        void acceptAnnotation(String annotationName);

        void acceptClassDependency(String dependencyName);
    }

    public ClassInfoVisitor(@NotNull Consumer consumer) {
        super(Opcodes.ASM9);
        this.consumer = Objects.requireNonNull(consumer);
    }

    public ClassInfoVisitor(@NotNull ClassVisitor classVisitor, Consumer consumer) {
        super(Opcodes.ASM9, classVisitor);
        this.consumer = Objects.requireNonNull(consumer);
    }

    private String selfName;

    private void visitType(String type) {
        if (type == null) {
            return;
        }
        if (type.equals(selfName)) {
            return;
        }
        consumer.acceptClassDependency(type);
    }

    private void visitDesc(Type type) {
        if (type.getSort() != Type.OBJECT) {
            return;
        }
        visitType(type.getInternalName());
    }

    private void visitDesc(String descriptor) {
        if (descriptor == null) {
            return;
        }
        var type = Type.getType(descriptor);
        visitDesc(type);
    }

    private void visitMethodDesc(String descriptor) {
        if (descriptor == null) {
            return;
        }
        var type = Type.getMethodType(descriptor);
        var returnType = type.getReturnType();
        for (var argType : type.getArgumentTypes()) {
            visitDesc(argType);
        }
        visitDesc(returnType);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName,
                      String[] interfaces) {
        selfName = name;
        consumer.acceptClassInfo(name, access, superName);
        if (superName != null) {
            visitType(superName);
        }
        if (interfaces != null) {
            for (var interfaceName : interfaces) {
                consumer.acceptInterface(interfaceName);
                visitType(interfaceName);
            }
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitNestHost(String nestHost) {
        visitType(nestHost);
        super.visitNestHost(nestHost);
    }

    @Override
    public void visitOuterClass(String owner, String name, String descriptor) {
        visitType(owner);
        visitMethodDesc(descriptor);
        super.visitOuterClass(owner, name, descriptor);
    }

    private class PkgDepsAnnotationVisitor extends AnnotationVisitor {
        protected PkgDepsAnnotationVisitor(AnnotationVisitor annotationVisitor) {
            super(Opcodes.ASM9, annotationVisitor);
        }

        @Override
        public void visitEnum(String name, String descriptor, String value) {
            ClassInfoVisitor.this.visitDesc(descriptor);
            super.visitEnum(name, descriptor, value);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String descriptor) {
            ClassInfoVisitor.this.visitDesc(descriptor);
            return super.visitAnnotation(name, descriptor);
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            return new PkgDepsAnnotationVisitor(super.visitArray(name));
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        var type = Type.getType(descriptor);
        if (type.getSort() == Type.OBJECT) {
            var internalName = type.getInternalName();
            visitType(internalName);
            consumer.acceptAnnotation(internalName);
        }
        return new PkgDepsAnnotationVisitor(super.visitAnnotation(descriptor, visible));
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(
            int typeRef, TypePath typePath, String descriptor, boolean visible) {
        visitDesc(descriptor);
        return new PkgDepsAnnotationVisitor(
                super.visitTypeAnnotation(typeRef, typePath, descriptor, visible));
    }

    @Override
    public void visitNestMember(String nestMember) {
        visitType(nestMember);
        super.visitNestMember(nestMember);
    }

    @Override
    public void visitPermittedSubclass(String permittedSubclass) {
        visitType(permittedSubclass);
        super.visitPermittedSubclass(permittedSubclass);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        visitType(name);
        visitType(outerName);
        super.visitInnerClass(name, outerName, innerName, access);
    }

    @Override
    public RecordComponentVisitor visitRecordComponent(
            String name, String descriptor, String signature) {
        visitDesc(descriptor);
        return super.visitRecordComponent(name, descriptor, signature);
    }

    @Override
    public FieldVisitor visitField(
            int access, String name, String descriptor, String signature, Object value) {
        visitDesc(descriptor);
        return super.visitField(access, name, descriptor, signature, value);
    }

    private class PkgDepsMethodVisitor extends MethodVisitor {
        protected PkgDepsMethodVisitor(MethodVisitor methodVisitor) {
            super(Opcodes.ASM9, methodVisitor);
        }

        @Override
        public AnnotationVisitor visitAnnotationDefault() {
            return new PkgDepsAnnotationVisitor(super.visitAnnotationDefault());
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            ClassInfoVisitor.this.visitDesc(descriptor);
            return new PkgDepsAnnotationVisitor(super.visitAnnotation(descriptor, visible));
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(
                int parameter, String descriptor, boolean visible) {
            ClassInfoVisitor.this.visitDesc(descriptor);
            return new PkgDepsAnnotationVisitor(
                    super.visitParameterAnnotation(parameter, descriptor, visible));
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(
                int typeRef, TypePath typePath, String descriptor, boolean visible) {
            ClassInfoVisitor.this.visitDesc(descriptor);
            return new PkgDepsAnnotationVisitor(
                    super.visitTypeAnnotation(typeRef, typePath, descriptor, visible));
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
            ClassInfoVisitor.this.visitType(owner);
            ClassInfoVisitor.this.visitDesc(descriptor);
            super.visitFieldInsn(opcode, owner, name, descriptor);
        }

        @Override
        public void visitMethodInsn(
                int opcode, String owner, String name, String descriptor, boolean isInterface) {
            if (owner.startsWith("[")) {
                var type = Type.getType(owner);
                var elementType = type.getElementType();
                if (elementType.getSort() == Type.OBJECT) {
                    ClassInfoVisitor.this.visitDesc(elementType);
                }
            } else {
                ClassInfoVisitor.this.visitType(owner);
            }
            ClassInfoVisitor.this.visitMethodDesc(descriptor);
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }

        @Override
        public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
            ClassInfoVisitor.this.visitDesc(descriptor);
            super.visitMultiANewArrayInsn(descriptor, numDimensions);
        }

        @Override
        public void visitInvokeDynamicInsn(String name, String descriptor,
                                           Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
            ClassInfoVisitor.this.visitMethodDesc(descriptor);
            super.visitInvokeDynamicInsn(
                    name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
        }

        @Override
        public AnnotationVisitor visitInsnAnnotation(
                int typeRef, TypePath typePath, String descriptor, boolean visible) {
            ClassInfoVisitor.this.visitDesc(descriptor);
            return super.visitInsnAnnotation(typeRef, typePath, descriptor, visible);
        }

        @Override
        public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
            ClassInfoVisitor.this.visitType(type);
            super.visitTryCatchBlock(start, end, handler, type);
        }

        @Override
        public void visitLocalVariable(
                String name, String descriptor, String signature, Label start, Label end, int index) {
            ClassInfoVisitor.this.visitDesc(descriptor);
            super.visitLocalVariable(name, descriptor, signature, start, end, index);
        }

        @Override
        public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath,
                                                              Label[] start, Label[] end, int[] index, String descriptor, boolean visible) {
            ClassInfoVisitor.this.visitDesc(descriptor);
            return super.visitLocalVariableAnnotation(
                    typeRef, typePath, start, end, index, descriptor, visible);
        }
    }

    @Override
    public MethodVisitor visitMethod(
            int access, String name, String descriptor, String signature, String[] exceptions) {
        visitDesc(descriptor);
        if (exceptions != null) {
            for (var exception : exceptions) {
                visitType(exception);
            }
        }
        return new PkgDepsMethodVisitor(
                super.visitMethod(access, name, descriptor, signature, exceptions));
    }
}
