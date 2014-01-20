package org.pareto4j.patcher.collections;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
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
 * Date: 23/12/13
 * Time: 12:10
 * To change this template use File | Settings | File Templates.
 */
public class CollectionsPatcher implements ClassFileTransformer {
    static String excludes = System.getProperty("patcher.excludes", "");
    static String includes = System.getProperty("patcher.includes", "");
    static boolean debug = System.getProperty("patcher.debug") != null;
    List<Pattern> excludesPatterns = new LinkedList<Pattern>();
    List<Pattern> includesPatterns = new LinkedList<Pattern>();

    public CollectionsPatcher() {
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

        if (className.startsWith("org/pareto4j")) {
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
            cv = new CollectionsClassPatcher(cv, className);
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
