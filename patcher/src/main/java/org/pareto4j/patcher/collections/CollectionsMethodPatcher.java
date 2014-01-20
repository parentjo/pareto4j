package org.pareto4j.patcher.collections;

import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: jparent
 * Date: 23/12/13
 * Time: 12:33
 * To change this template use File | Settings | File Templates.
 */
public class CollectionsMethodPatcher extends MethodAdapter {
    private String currentClass;

    // Hack
    // classes that extend ArrayList or HashMap should not have
    // the instructions that are the equivalent of the super()
    // changed.
    // These instructions use an INVOKESPECIAL without first
    // performing a NEW. We therefore track calls to NEW using this field
    // seenNew == false -> super() -> no replacement
    //
    boolean seenNew = false;
    private String currentMethod;
    final Map<PatcheableTypes, AtomicInteger> count = new EnumMap<PatcheableTypes, AtomicInteger>(PatcheableTypes.class);
    final EnumSet<PatcheableTypes> toPatch;

    String returnType;
    String lastCreated;


    /**
     * Constructs a new {@link org.objectweb.asm.MethodAdapter} object.
     *
     * @param methodVisitor the code visitor to which this adapter must delegate calls.
     * @param toPatch
     */
    public CollectionsMethodPatcher(MethodVisitor methodVisitor, EnumSet<PatcheableTypes> toPatch) {
        super(methodVisitor);
        this.toPatch = toPatch;
        //
        for (PatcheableTypes delegateTypes : toPatch) {
            count.put(delegateTypes, new AtomicInteger());
        }

    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        if (opcode == Opcodes.NEW) {
            //
            for (PatcheableTypes delegateTypes : toPatch) {
                if (type.equals(delegateTypes.jdk)) {
                    super.visitTypeInsn(Opcodes.NEW, delegateTypes.delegate);
                    count.get(delegateTypes).incrementAndGet();
                    seenNew = true;
                    return;
                }
            }
        }
        super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        if (opcode == Opcodes.INVOKESPECIAL && name.equals("<init>") /*&& seenNew*/) {
            for (PatcheableTypes delegateTypes : toPatch) {
                if (owner.equals(delegateTypes.jdk)) {
                    super.visitMethodInsn(Opcodes.INVOKESPECIAL, delegateTypes.delegate, "<init>", desc);
                    seenNew = false;
                    return;
                }
            }
        }
        super.visitMethodInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        //
        int sum = 0;
        for (PatcheableTypes delegateTypes : toPatch)
            sum += count.get(delegateTypes).get();

        if (sum > 0) {
            System.err.print("---- Mod " + currentClass + ' ' + currentMethod + ' ');
            for (PatcheableTypes delegateTypes : toPatch) {
                int c = count.get(delegateTypes).get();
                if (c > 0) {
                    System.err.print(delegateTypes.name() + ": " + c + ' ');
                }
            }
            System.err.println();
        }
    }

    public void setCurrentClass(String currentClass) {
        this.currentClass = currentClass;
    }

    public void setCurrentMethod(String currentMethod) {
        this.currentMethod = currentMethod;
    }

    public void setReturnType(String s) {
        this.returnType = s.substring(s.indexOf(')')+1);
    }
}
