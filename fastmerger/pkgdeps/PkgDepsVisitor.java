package top.fifthlight.fastmerger.pkgdeps;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.*;

import java.util.Objects;

public class PkgDepsVisitor extends ClassVisitor {
    private final Consumer pkgDepsConsumer;

    @FunctionalInterface
    public interface Consumer {
        void acceptClassDependency(String className, String dependencyName);
    }

    public PkgDepsVisitor(@NotNull Consumer pkgDepsConsumer) {
        super(Opcodes.ASM9);
        this.pkgDepsConsumer = Objects.requireNonNull(pkgDepsConsumer);
    }

    public PkgDepsVisitor(@NotNull ClassVisitor classVisitor, Consumer pkgDepsConsumer) {
        super(Opcodes.ASM9, classVisitor);
        this.pkgDepsConsumer = Objects.requireNonNull(pkgDepsConsumer);
    }

    @Nullable
    private String className;

    private void visitType(String type) {
        if (className == null) {
            throw new IllegalStateException("No class name");
        }
        pkgDepsConsumer.acceptClassDependency(className, type);
    }

    private void visitDesc(String descriptor) {
        if (descriptor == null) {
            return;
        }
        var type = Type.getMethodType(descriptor);
        visitDesc(type);
    }

    private void visitDesc(Type type) {
        System.out.println("Descriptor: " + type);
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

    private void visitSignature(String signature) {
        if (signature == null) {
            return;
        }
        System.out.println("Signature: " + signature);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        className = name;
        if (superName != null) {
            visitSignature(superName);
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
            PkgDepsVisitor.this.visitMethodDesc(descriptor);
            super.visitEnum(name, descriptor, value);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String descriptor) {
            PkgDepsVisitor.this.visitMethodDesc(descriptor);
            return super.visitAnnotation(name, descriptor);
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            return new PkgDepsAnnotationVisitor(super.visitArray(name));
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        visitMethodDesc(descriptor);
        return new PkgDepsAnnotationVisitor(super.visitAnnotation(descriptor, visible));
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        visitMethodDesc(descriptor);
        return new PkgDepsAnnotationVisitor(super.visitTypeAnnotation(typeRef, typePath, descriptor, visible));
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
    public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature) {
        // TODO
        visitDesc(descriptor);
        return super.visitRecordComponent(name, descriptor, signature);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        visitDesc(descriptor);
        visitSignature(signature);
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
            PkgDepsVisitor.this.visitDesc(descriptor);
            return new PkgDepsAnnotationVisitor(super.visitAnnotation(descriptor, visible));
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
            PkgDepsVisitor.this.visitDesc(descriptor);
            return new PkgDepsAnnotationVisitor(super.visitParameterAnnotation(parameter, descriptor, visible));
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
            // TODO
            PkgDepsVisitor.this.visitDesc(descriptor);
            return new PkgDepsAnnotationVisitor(super.visitTypeAnnotation(typeRef, typePath, descriptor, visible));
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
            PkgDepsVisitor.this.visitDesc(descriptor);
            super.visitFieldInsn(opcode, owner, name, descriptor);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            PkgDepsVisitor.this.visitMethodDesc(descriptor);
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }

        @Override
        public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
            PkgDepsVisitor.this.visitDesc(descriptor);
            super.visitMultiANewArrayInsn(descriptor, numDimensions);
        }

        @Override
        public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
            PkgDepsVisitor.this.visitMethodDesc(descriptor);
            super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
        }

        @Override
        public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
            // TODO
            PkgDepsVisitor.this.visitDesc(descriptor);
            return super.visitInsnAnnotation(typeRef, typePath, descriptor, visible);
        }

        @Override
        public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
            PkgDepsVisitor.this.visitType(type);
            super.visitTryCatchBlock(start, end, handler, type);
        }

        @Override
        public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
            PkgDepsVisitor.this.visitDesc(descriptor);
            super.visitLocalVariable(name, descriptor, signature, start, end, index);
        }

        @Override
        public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String descriptor, boolean visible) {
            // TODO
            PkgDepsVisitor.this.visitDesc(descriptor);
            return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible);
        }
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        visitDesc(descriptor);
        visitSignature(signature);
        if (exceptions != null) {
            for (var exception : exceptions) {
                visitType(exception);
            }
        }
        return new PkgDepsMethodVisitor(super.visitMethod(access, name, descriptor, signature, exceptions));
    }
}
