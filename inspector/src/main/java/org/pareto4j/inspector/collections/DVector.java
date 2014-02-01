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
 * Time: 14:54
 * To change this template use File | Settings | File Templates.
 */
public class DVector<E> extends Vector<E> implements Wrapper {
    public static final DelegateTypes TYPES = DelegateTypes.VECTOR;
    public final Location creation = Location.create(new Where());

    MyV<E> d;

    static class MyV<E>  extends Vector<E> {
        MyV(int initialCapacity, int capacityIncrement) {
            super(initialCapacity, capacityIncrement);
        }

        MyV(int initialCapacity) {
            super(initialCapacity);
        }

        MyV() {
        }

        MyV(Collection c) {
            super(c);
        }

        @Override
        protected synchronized void removeRange(int fromIndex, int toIndex) {
            super.removeRange(fromIndex, toIndex);    //To change body of overridden methods use File | Settings | File Templates.
        }
    }

    public DVector(int initialCapacity, int capacityIncrement) {
        d = new MyV(initialCapacity, capacityIncrement);
        CollectionsData.register(this);
    }

    public DVector(int initialCapacity) {
        d = new MyV(initialCapacity);
        CollectionsData.register(this);
    }

    public DVector() {
        d = new MyV();
        CollectionsData.register(this);
    }

    public DVector(Collection c) {
        d = new MyV();
        d.addAll(c);
        CollectionsData.register(this);
    }

    @Override
    public void copyInto(Object[] anArray) {
        d.copyInto(anArray);
    }

    @Override
    public void trimToSize() {
        d.trimToSize();
    }

    @Override
    public void ensureCapacity(int minCapacity) {
        d.ensureCapacity(minCapacity);
    }

    @Override
    public void setSize(int newSize) {
        d.setSize(newSize);
    }

    @Override
    public int capacity() {
        return d.capacity();
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
    public Enumeration elements() {
        return d.elements();
    }

    @Override
    public boolean contains(Object o) {
        return d.contains(o);
    }

    @Override
    public int indexOf(Object o) {
        return d.indexOf(o);
    }

    @Override
    public int indexOf(Object o, int index) {
        return d.indexOf(o, index);
    }

    @Override
    public int lastIndexOf(Object o) {
        return d.lastIndexOf(o);
    }

    @Override
    public int lastIndexOf(Object o, int index) {
        return d.lastIndexOf(o, index);
    }

    @Override
    public E elementAt(int index) {
        return d.elementAt(index);
    }

    @Override
    public E firstElement() {
        return d.firstElement();
    }

    @Override
    public E lastElement() {
        return d.lastElement();
    }

    @Override
    public void setElementAt(E obj, int index) {
        d.setElementAt(obj, index);
    }

    @Override
    public void removeElementAt(int index) {
        d.removeElementAt(index);
    }

    @Override
    public void insertElementAt(E obj, int index) {
        d.insertElementAt(obj, index);
    }

    @Override
    public void addElement(E obj) {
        d.addElement(obj);
    }

    @Override
    public boolean removeElement(Object obj) {
        return d.removeElement(obj);
    }

    @Override
    public void removeAllElements() {
        d.removeAllElements();
    }

    @Override
    public Object clone() {
        DVector r = new DVector();
        r.d = (MyV) d.clone();
        return r;
    }

    @Override
    public Object[] toArray() {
        return d.toArray();
    }

    public <E> E[] toArray(E[] a) {
        return d.toArray(a);
    }

    @Override
    public E get(int index) {
        return d.get(index);
    }

    @Override
    public E set(int index, E element) {
        return d.set(index, element);
    }

    @Override
    public boolean add(E o) {
        return d.add(o);
    }

    @Override
    public boolean remove(Object o) {
        return d.remove(o);
    }

    @Override
    public void add(int index, E element) {
        d.add(index, element);
    }

    @Override
    public E remove(int index) {
        return d.remove(index);
    }

    @Override
    public void clear() {
        d.clear();
    }

    public boolean containsAll(Collection<?> c) {
        return d.containsAll(c);
    }

    @Override
    public boolean addAll(Collection c) {
        return d.addAll(c);
    }

    public boolean removeAll(Collection<?> c) {
        return d.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return d.retainAll(c);
    }

    @Override
    public boolean addAll(int index, Collection c) {
        return d.addAll(index, c);
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
    public String toString() {
        return d.toString();
    }

    @Override
    public List subList(int fromIndex, int toIndex) {
        return d.subList(fromIndex, toIndex);
    }

    @Override
    public void removeRange(int fromIndex, int toIndex) {
        d.removeRange(fromIndex, toIndex);
    }

    @Override
    public Iterator iterator() {
        return d.iterator();
    }

    @Override
    public ListIterator listIterator() {
        return d.listIterator();
    }

    @Override
    public ListIterator listIterator(int index) {
        return d.listIterator(index);
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

    public int getCapacity() {
        return elementData.length;
    }

    static Class VEC = Vector.class;

    public static int getCapacity(Vector s) {
        try {
            Field f = VEC.getDeclaredField("elementData");
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
