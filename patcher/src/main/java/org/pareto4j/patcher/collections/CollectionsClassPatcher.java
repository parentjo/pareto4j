package org.pareto4j.patcher.collections;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.EnumSet;

/**
 * Created with IntelliJ IDEA.
 * User: jparent
 * Date: 23/12/13
 * Time: 12:33
 * To change this template use File | Settings | File Templates.
 */
public class CollectionsClassPatcher extends ClassAdapter {
    String currentClass;
    private EnumSet<PatcheableTypes> toPatch;

    /**
     * Constructs a new {@link org.objectweb.asm.ClassAdapter} object.
     *
     * @param cv        the class visitor to which this adapter must delegate calls.
     * @param className
     */
    public CollectionsClassPatcher(ClassVisitor cv, String className) {
        super(cv);
        currentClass = className;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        CollectionMethodAnalyser methodAnalyser = new CollectionMethodAnalyser(access, name, desc, signature, exceptions, toPatch);
        CollectionsMethodPatcher methodVisitor = new CollectionsMethodPatcher(super.visitMethod(access, name, signature, desc, exceptions), toPatch);

        methodVisitor.setCurrentClass(currentClass);
        methodVisitor.setReturnType(signature);
        methodVisitor.setCurrentMethod(name + '.' + signature);
        return methodVisitor;
    }
}
