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
 * Date: 13/08/13
 * Time: 15:02
 * To change this template use File | Settings | File Templates.
 */
public class DArrayList<E> extends ArrayList<E> implements List<E>, Wrapper<ArrayList<E>> {
    public static final DelegateTypes TYPE = DelegateTypes.ARRAYLIST;
    MyAL<E> d = new MyAL<E>();
    public final Location creation = Location.create(new Where());

    static class MyAL<E> extends ArrayList<E> {
        public MyAL(int initialCapacity) {
            super(initialCapacity);    //To change body of overridden methods use File | Settings | File Templates.
        }

        public MyAL() {
            super();    //To change body of overridden methods use File | Settings | File Templates.
        }

        public MyAL(Collection c) {
            super(c);    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        protected void removeRange(int fromIndex, int toIndex) {
            super.removeRange(fromIndex, toIndex);    //To change body of overridden methods use File | Settings | File Templates.
        }
    }

    public DArrayList(int initialCapacity) {
        this.d = new MyAL<E>(initialCapacity);
        CollectionsData.register(this);
    }

    public DArrayList() {
        this.d = new MyAL<E>();
        CollectionsData.register(this);
    }

    public DArrayList(Collection<? extends E> c) {
        this.d = new MyAL<E>();
        d.addAll(c);
        CollectionsData.register(this);
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

    @Override
    public int indexOf(Object o) {
        return d.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return d.lastIndexOf(o);
    }

    @Override
    public Object clone() {
        DArrayList r = new DArrayList();
        r.d = ((MyAL) d.clone());

        return r;
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
    public E get(int index) {
        return d.get(index);
    }

    public E set(int index, E element) {
        return d.set(index, element);
    }

    public boolean add(E e) {
        return d.add(e);
    }

    public void add(int index, E element) {
        d.add(index, element);
    }

    @Override
    public E remove(int index) {
        return d.remove(index);
    }

    @Override
    public boolean remove(Object o) {
        return d.remove(o);
    }

    @Override
    public void clear() {
        d.clear();
    }

    public boolean addAll(Collection<? extends E> c) {
        return d.addAll(c);
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        return d.addAll(index, c);
    }

    @Override
    public Iterator<E> iterator() {
        return d.iterator();
    }

    @Override
    public ListIterator<E> listIterator() {
        return d.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return d.listIterator(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return d.subList(fromIndex, toIndex);
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
    public boolean containsAll(Collection<?> c) {
        return d.containsAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return d.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return d.retainAll(c);
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        d.removeRange(fromIndex, toIndex);
    }

    @Override
    public String toString() {
        return d.toString();
    }

    public ArrayList<E> getOriginal() {
        return d;
    }

    public Location getCreation() {
        return creation;
    }

    public DelegateTypes getType() {
        return TYPE;
    }

    private static Class c = ArrayList.class;

    public int getCapacity() {
        return getCapacity(this);
    }

    static int getCapacity(ArrayList l) {
        try {
            Field f = c.getDeclaredField("elementData");
            f.setAccessible(true);
            Object[] elementData = (Object[]) f.get(l);
            return elementData.length;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return -1;
    }

    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        s.writeObject(d);
    }

    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        d = (MyAL<E>) s.readObject();
    }
}
