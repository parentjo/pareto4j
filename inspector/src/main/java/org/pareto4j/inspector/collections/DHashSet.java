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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: jparent
 * Date: 3/09/13
 * Time: 11:43
 * To change this template use File | Settings | File Templates.
 */
public class DHashSet<E> extends HashSet<E> implements Wrapper {
    public static final DelegateTypes TYPES = DelegateTypes.HASHSET;

    HashSet<E> d;
    public final Location creation = new Location(new Where());

    public DHashSet() {
        super(1);
        d = new HashSet<E>();
        CollectionsData.register(this);
    }

    public DHashSet(Collection<? extends E> c) {
        super(1);
        d = new HashSet<E>();
        d.addAll(c);
        CollectionsData.register(this);
    }

    public DHashSet(int initialCapacity, float loadFactor) {
        super(1);
        d = new HashSet<E>(initialCapacity, loadFactor);
        CollectionsData.register(this);
    }

    public DHashSet(int initialCapacity) {
        super(1);
        d = new HashSet<E>(initialCapacity);
        CollectionsData.register(this);
    }

    @Override
    public String toString() {
        return d.toString();
    }

    @Override
    public Iterator<E> iterator() {
        return d.iterator();
    }

    @Override
    public int size() {
        return d.size();
    }

    @Override
    public boolean isEmpty() {
        return d.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return d.contains(o);
    }

    public boolean add(E e) {
        return d.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return d.remove(o);
    }

    @Override
    public void clear() {
        d.clear();
    }

    @Override
    public Object clone() {
        DHashSet r = new DHashSet<E>();
        r.d = (HashSet) d.clone();
        return r;
    }

    @Override
    public boolean equals(Object o) {
        return d.equals(o);
    }

    @Override
    public int hashCode() {
        return d.hashCode();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return d.removeAll(c);
    }

    @Override
    public Object[] toArray() {
        return d.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return d.toArray(a);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return d.containsAll(c);
    }

    public boolean addAll(Collection<? extends E> c) {
        return d.addAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return d.retainAll(c);
    }

    public Object getOriginal() {
        return d;
    }

    public Location getCreation() {
        return creation;
    }

    public DelegateTypes getType() {
        return TYPES;
    }

    public int getCapacity() {
        return getCapacity(this);
    }

    static Class cHS = HashSet.class;

    public static int getCapacity(HashSet s) {
        try {
            Field f = cHS.getDeclaredField("map");
            f.setAccessible(true);
            HashMap m = (HashMap) f.get(s);
            return DHashMap.getCapacity(m);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return -1;
    }
}
