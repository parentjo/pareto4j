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
public class InspectorClassAdapter extends ClassAdapter {
    String currentClass;

    public InspectorClassAdapter(ClassVisitor cv, String className) {
        super(cv);
        currentClass = className;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        for (DelegateTypes delegateTypes : Profile.modify) {
            if (superName.equals(delegateTypes.jdk)) {
                System.out.println("PATCH SUPER " + delegateTypes + " for " + name);
                super.visit(version, access, name, signature, delegateTypes.delegate, interfaces);
                return;
            }
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }


    @Override
    public MethodVisitor visitMethod(int access, String name, String signature, String desc, String[] exceptions) {
        InspectorMethodVisitor methodVisitor = new InspectorMethodVisitor(super.visitMethod(access, name, signature, desc, exceptions));

        methodVisitor.setCurrentClass(currentClass);

        methodVisitor.setCurrentMethod(name + '.' + signature);
        return methodVisitor;
    }
}
