package org.pareto4j.patcher.collections;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: gebruiker
 * Date: 23/12/13
 * Time: 21:20
 * To change this template use File | Settings | File Templates.
 */
public class CollectionMethodAnalyser extends MethodNode {
    final EnumSet<PatcheableTypes> toPatch;
    AbstractInsnNode a;
    int pos = 0;
    private List<Integer> toReplace = new LinkedList<Integer>();
    String returnType;

    public CollectionMethodAnalyser(int access, String name, String desc, String signature, String[] exceptions, EnumSet<PatcheableTypes> toPatch) {
        super(access, name, desc, signature, exceptions);
        this.toPatch = toPatch;

        returnType = Type.getReturnType(desc).getDescriptor();
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        super.visitLocalVariable(name, desc, signature, start, end, index);
    }

    @Override
    public void visitEnd() {
        analyze();
        if (!toReplace.isEmpty())
            modify();
    }

    private void modify() {
        // Replace NEW & INVOKESPECIAL
    }

    public void analyze() {
        int candidate = 0;
        a = super.instructions.getFirst();
        //
        while (a != null) {
            if (a.getType() == AbstractInsnNode.METHOD_INSN && a.getOpcode() == Opcodes.INVOKESPECIAL) {
                // mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V");
                MethodInsnNode methodInsnNode = (MethodInsnNode) a;
                PatcheableTypes patcheableType = isPatcheable(methodInsnNode.owner);
                if (patcheableType != null) {
                    candidate = pos;
                    if (canReplace(a, patcheableType))
                        add(candidate);
                }
            }
            //
            a = a.getNext();
            pos++;
        }
        //
        super.visitEnd();
    }

    private void add(int candidate) {
        toReplace.add(candidate);
    }

    private boolean canReplace(AbstractInsnNode a, PatcheableTypes patcheableType) {
        // Next can be:
        // - nothing special
        // - a return        mv.visitInsn(RETURN);  Check signature!
        //
        // - field assignment  Check field signature in call
        //                    mv.visitFieldInsn(PUTFIELD, "MyObject", "al", "Ljava/util/ArrayList;");
        //
        // - var assignment   mv.visitVarInsn(ASTORE, 1);
        //                    ....
        //                    mv.visitLocalVariable("aList", "Ljava/util/ArrayList;", null, l1, l2, 1);
        //
        a = a.getNext();
        pos++;
        if (a != null) {
            switch (a.getOpcode()) {
                case Opcodes.PUTFIELD:
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) a;
                    return fieldInsnNode.desc.equals("L" + patcheableType.jdk + ";");

                case Opcodes.ARETURN:
                    return returnType.equals("L" + patcheableType.jdk + ";");

                case Opcodes.ASTORE:
                    VarInsnNode varInsnNode = (VarInsnNode) a;
                    LocalVariableNode localVariableNode;
                    List<LocalVariableNode> localVariableNodes = new LinkedList<LocalVariableNode>();
                    for (LocalVariableNode variableNode : ((List<LocalVariableNode>) localVariables)) {
                        if (variableNode.index == variableNode.index)
                            localVariableNodes.add(variableNode);
                    }
                    if (localVariableNodes.size() > 1)
                        throw new IllegalStateException("Not supported yet :(");
                    localVariableNode = localVariableNodes.get(0);
                    //
                    return  localVariableNode.desc.equals("L" + patcheableType.jdk + ";");
            }
        }
        return false;  //To change body of created methods use File | Settings | File Templates.
    }

    // True is the type is being replaced
    private PatcheableTypes isPatcheable(String owner) {
        for (PatcheableTypes patcheableType : toPatch) {
            if (patcheableType.jdk.endsWith(owner))
                return patcheableType;
        }
        return null;
    }
}
