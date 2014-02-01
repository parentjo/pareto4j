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

import java.lang.reflect.Field;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: jparent
 * Date: 9/12/13
 * Time: 14:55
 * To change this template use File | Settings | File Templates.
 */
public class DHashtable<K,V> extends Hashtable<K,V> implements Wrapper  {
    public static final DelegateTypes TYPES = DelegateTypes.HASHTABLE;
    public final Location creation = Location.create(new Where());

    Hashtable d;

    public DHashtable(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        d = new Hashtable(initialCapacity, loadFactor);
        CollectionsData.register(this);
    }

    public DHashtable(int initialCapacity) {
        super(initialCapacity);
        d = new Hashtable(initialCapacity);
        CollectionsData.register(this);
    }

    public DHashtable() {
        d = new Hashtable();
        CollectionsData.register(this);
    }

    public DHashtable(Map<? extends K, ? extends V> t) {
        super();
        d = new Hashtable();
        d.putAll(t);
        CollectionsData.register(this);
    }

    public Object getOriginal() {
        return d;
    }

    public Location getCreation() {
        return creation;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public DelegateTypes getType() {
        return TYPES;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int size() {
        return d.size();
    }

    public int getCapacity() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isEmpty() {
        return d.isEmpty();
    }

    @Override
    public Enumeration keys() {
        return d.keys();
    }

    @Override
    public Enumeration elements() {
        return d.elements();
    }

    @Override
    public boolean contains(Object value) {
        return d.contains(value);
    }

    @Override
    public boolean containsValue(Object value) {
        return d.containsValue(value);
    }

    @Override
    public boolean containsKey(Object key) {
        return d.containsKey(key);
    }

    @Override
    public V get(Object key) {
        return (V) d.get(key);
    }

    public V put(Object key, Object value) {
        return (V) d.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return (V) d.remove(key);
    }

    public void putAll(Map t) {
        d.putAll(t);
    }

    @Override
    public void clear() {
        d.clear();
    }

    @Override
    public Object clone() {
        return d.clone();
    }

    @Override
    public String toString() {
        return d.toString();
    }

    @Override
    public Set keySet() {
        return d.keySet();
    }

    @Override
    public Set<Map.Entry<K,V>> entrySet() {
        return d.entrySet();
    }

    @Override
    public Collection values() {
        return d.values();
    }

    @Override
    public boolean equals(Object o) {
        return d.equals(o);
    }

    @Override
    public int hashCode() {
        return d.hashCode();
    }

    static Class HT = Hashtable.class;

    public static int getCapacity(Hashtable s) {
        try {
            Field f = HT.getDeclaredField("table");
            f.setAccessible(true);
            Object[] els = (Object[]) f.get(s);
            return els.length;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return -1;
    }

}
