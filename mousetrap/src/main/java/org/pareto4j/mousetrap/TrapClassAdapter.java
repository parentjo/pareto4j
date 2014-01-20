package org.pareto4j.mousetrap;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * Created with IntelliJ IDEA.
 * User: jparent
 * Date: 13/08/13
 * Time: 16:54
 * To change this template use File | Settings | File Templates.
 */
public class TrapClassAdapter extends ClassAdapter {
    String currentClass;

    public TrapClassAdapter(ClassVisitor cv, String className) {
        super(cv);
        currentClass = className;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
    }


    @Override
    public MethodVisitor visitMethod(int access, String name, String signature, String desc, String[] exceptions) {
        TrapMethodVisitor methodVisitor = new TrapMethodVisitor(super.visitMethod(access, name, signature, desc, exceptions));

        methodVisitor.setCurrentClass(currentClass);

        methodVisitor.setCurrentMethod(name + '.' + signature);
        return methodVisitor;
    }
}
