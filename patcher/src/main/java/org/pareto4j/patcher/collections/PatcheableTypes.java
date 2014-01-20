package org.pareto4j.patcher.collections;

import org.pareto4j.collections.*;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: jparent
 * Date: 23/12/13
 * Time: 12:42
 * To change this template use File | Settings | File Templates.
 */
public enum  PatcheableTypes {
    HASHSET(HashSet.class, ParetoHashSet.class),
    HASHMAP(HashMap.class, ParetoHashMap.class),
    LINKEDHASHSET(LinkedHashSet.class, ParetoLinkedHashSet.class),
    LINKEDHASHMAP(LinkedHashMap.class, ParetoLinkedHashMap.class),
    ARRAYLIST(ArrayList.class, ParetoArrayList.class),
    LINKEDLIST(LinkedList.class, ParetoLinkedList.class);

    final String jdk;
    final String delegate;
    final String simpleName;

    private PatcheableTypes(Class<?> from, Class<?> to) {
        this(internal(from), internal(to));
    }

    private static String internal(Class<?> c) {
        return internal(c.getCanonicalName());
    }

    private static String internal(String s) {
        return s.replace('.', '/');
    }

    private PatcheableTypes(String from, String to) {
        this.jdk = from;
        this.delegate = to;
        simpleName = name().toLowerCase();
    }


    @Override
    public String toString() {
        return "DelegateTypes{" +
                "jdk='" + jdk + '\'' +
                ", delegate='" + delegate + '\'' +
                '}';
    }

    }
