package org.pareto4j.mousetrap;

import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.EnumMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jparent
 * Date: 13/08/13
 * Time: 16:58
 * To change this template use File | Settings | File Templates.
 */
public class TrapMethodVisitor extends MethodAdapter {

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

    public TrapMethodVisitor(MethodVisitor methodVisitor) {
        super(methodVisitor);
    }


    public void setCurrentClass(String currentClass) {
        this.currentClass = currentClass;
    }

    public String getCurrentClass() {
        return currentClass;
    }

    public void setCurrentMethod(String currentMethod) {
        this.currentMethod = currentMethod;
    }

    public String getCurrentMethod() {
        return currentMethod;
    }

    static class Counter {
        int value = 0;

        public void inc() {
            value++;
        }
    }
}
