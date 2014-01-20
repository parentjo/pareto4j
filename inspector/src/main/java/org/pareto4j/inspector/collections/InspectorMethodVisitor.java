/*
 * Copyright 2014 Johan Parent
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.pareto4j.inspector.collections;

import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: jparent
 * Date: 13/08/13
 * Time: 16:58
 * To change this template use File | Settings | File Templates.
 */
public class InspectorMethodVisitor extends MethodAdapter {

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
    Map<DelegateTypes, Counter> count = new EnumMap<DelegateTypes, Counter>(DelegateTypes.class);

    public InspectorMethodVisitor(MethodVisitor methodVisitor) {
        super(methodVisitor);
        //
        for (DelegateTypes delegateTypes :Profile.modify) {
            count.put(delegateTypes, new Counter());
        }

    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        if (opcode == Opcodes.NEW) {
            //
            for (DelegateTypes delegateTypes : Profile.modify) {
                if (type.equals(delegateTypes.jdk)) {
                    super.visitTypeInsn(Opcodes.NEW, delegateTypes.delegate);
                    count.get(delegateTypes).inc();
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
            for (DelegateTypes delegateTypes : Profile.modify) {
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
        for (DelegateTypes delegateTypes : Profile.modify)
            sum += count.get(delegateTypes).value;

        if (sum > 0 )  {
            System.err.print("---- Mod " + currentClass + ' ' + currentMethod + ' ');
            for (DelegateTypes delegateTypes : Profile.modify) {
                int c = count.get(delegateTypes).value;
                if (c > 0) {
                    System.err.print(delegateTypes.name() +": " + c + ' ');
                }
            }
            System.err.println();
        }
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
