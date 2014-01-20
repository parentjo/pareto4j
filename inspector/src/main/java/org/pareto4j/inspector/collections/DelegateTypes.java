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

/**
 * Created with IntelliJ IDEA.
 * User: gebruiker
 * Date: 19/11/13
 * Time: 7:10
 * To change this template use File | Settings | File Templates.
 */
public enum DelegateTypes {

    HASHSET("java/util/HashSet", "org/pareto4j/inspector/collections/DHashSet"),
    HASHMAP("java/util/HashMap", "org/pareto4j/inspector/collections/DHashMap"),
    LINKEDHASHSET("java/util/LinkedHashSet", "org/pareto4j/inspector/collections/DLinkedHashSet"),
    LINKEDHASHMAP("java/util/LinkedHashMap", "org/pareto4j/inspector/collections/DLinkedHashMap"),
    LINKEDLIST("java/util/LinkedList", "org/pareto4j/inspector/collections/DLinkedList"),
    VECTOR("java/util/Vector", "org/pareto4j/inspector/collections/DVector"),
    ARRAYLIST("java/util/ArrayList", "org/pareto4j/inspector/collections/DArrayList"),
    HASHTABLE("java/util/Hashtable", "org/pareto4j/inspector/collections/DHashtable"),
    CONCURRENTHASHMAP("java/util/concurrent/ConcurrentHashMap", "org/pareto4j/inspector/collections/DConcurrentHashMap");

    final String jdk;
    final String delegate;
    final String simpleName;

    private DelegateTypes(String from, String to) {
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
