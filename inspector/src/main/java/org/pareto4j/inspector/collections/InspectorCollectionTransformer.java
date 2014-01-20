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

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: jparent
 * Date: 13/08/13
 * Time: 16:25
 * To change this template use File | Settings | File Templates.
 */
public class InspectorCollectionTransformer implements ClassFileTransformer {
    static String excludes = System.getProperty("inspector.excludes", "");
    static String includes = System.getProperty("inspector.includes", "");
    static boolean debug = System.getProperty("inspector.debug") != null;
    List<Pattern> excludesPatterns = new LinkedList<Pattern>();
    List<Pattern> includesPatterns = new LinkedList<Pattern>();

    public InspectorCollectionTransformer() {
        //
        createPatterns(excludes, excludesPatterns);
        createPatterns(includes, includesPatterns);
        System.err.println(excludesPatterns);
        System.err.println(includesPatterns);
    }

    private void createPatterns(String commaSeperated, List<Pattern> patterns) {
        if (commaSeperated.length() == 0)
            return;

        //
        for (String s : commaSeperated.split(",")) {
            s = s.trim();
            if (s.length() > 0)
                patterns.add(
                        Pattern.compile(
                                convertToRegex(s)
                        )
                );
        }
    }

    String convertToRegex(String pseudo) {
        return pseudo.replace(".", "/").replace("*", ".*");
    }

    public boolean include(String internalClassname) {
        if (!includesPatterns.isEmpty()) {
            boolean included = false;

            if (matches(internalClassname, includesPatterns)) {
                included = true;
            }

            if (!included)
                return false;
        }

        if (!excludesPatterns.isEmpty()) {
            if (matches(internalClassname, excludesPatterns))
                return false;
        }

        return true;
    }

    private boolean matches(String internalClassname, List<Pattern> patterns) {
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(internalClassname);
            if (matcher.find())
                return true;
        }

        return false;
    }


    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        byte[] result = classfileBuffer;

        if (className.startsWith("java/") || className.startsWith("javax/") || className.startsWith("sun/") || className.startsWith("com/sun/")) {
            return classfileBuffer;
        }

        if (className.startsWith("org/pareto4j/inspector/collections")) {
            return classfileBuffer;
        }

        if (!include(className)) {
            return classfileBuffer;
        }

//        System.err.println(className);
        try {
            ClassReader reader = new ClassReader(classfileBuffer);

            ClassWriter writer = new ClassWriter(1);

            ClassVisitor cv = writer;
            if (debug) {
                PrintWriter printWriter = new PrintWriter(System.out);
                cv = new TraceClassVisitor(cv, printWriter);
            }
            cv = new InspectorClassAdapter(cv, className);
//            cv = new TraceClassVisitor(cv, printWriter);
//            cv = new CheckClassAdapter(cv);
            reader.accept(cv, 0);

            result = writer.toByteArray();
        } catch (Throwable e) {
            e.printStackTrace();
        }


        return result;
    }
}
