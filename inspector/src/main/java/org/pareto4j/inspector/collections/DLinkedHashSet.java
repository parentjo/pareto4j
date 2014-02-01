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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * Created with IntelliJ IDEA.
 * User: jparent
 * Date: 9/09/13
 * Time: 9:57
 * To change this template use File | Settings | File Templates.
 */
public class DLinkedHashSet<E> extends LinkedHashSet<E> implements Wrapper {
    public static final DelegateTypes TYPES = DelegateTypes.LINKEDHASHSET;

    LinkedHashSet<E> d;
    public final Location creation = Location.create(new Where());

    public DLinkedHashSet() {
        d = new LinkedHashSet<E>();
        CollectionsData.register(this);
    }

    public DLinkedHashSet(Collection<? extends E> c) {
        super(0);
        d = new LinkedHashSet<E>();
        d.addAll(c);
        CollectionsData.register(this);
    }

    public DLinkedHashSet(int initialCapacity, float loadFactor) {
        super(0);
        d = new LinkedHashSet<E>(initialCapacity, loadFactor);
        CollectionsData.register(this);
    }

    public DLinkedHashSet(int initialCapacity) {
        super(0);
        d = new LinkedHashSet<E>(initialCapacity);
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
        DLinkedHashSet r = new DLinkedHashSet<E>();
        r.d = (LinkedHashSet) d.clone();
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
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
