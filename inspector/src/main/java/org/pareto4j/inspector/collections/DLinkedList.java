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

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: gebruiker
 * Date: 8/12/13
 * Time: 8:05
 * To change this template use File | Settings | File Templates.
 */
public class DLinkedList<E> extends LinkedList<E> implements Wrapper<LinkedList> {
    public static final DelegateTypes TYPE = DelegateTypes.LINKEDLIST;
    MyLL<E> d;
    public final Location creation = Location.create(new Where());

    static class MyLL<E> extends LinkedList<E> {
        MyLL() {
        }

        MyLL(Collection<? extends E> c) {
            super(c);
        }



        @Override
        protected void removeRange(int fromIndex, int toIndex) {
            super.removeRange(fromIndex, toIndex);
        }
    }

    public DLinkedList() {
        super();
        this.d = new MyLL<E>();
        CollectionsData.register(this);
    }

    public DLinkedList(Collection<? extends E> c) {
        super();
        this.d = new MyLL<E>();
        d.addAll(c);
        CollectionsData.register(this);
    }

    public boolean add(E e) {
        return d.add(e);
    }

    public void addLast(E e) {
        d.addLast(e);
    }

    public boolean offerFirst(E e) {
        return d.offerFirst(e);
    }

    public E set(int index, E element) {
        return d.set(index, element);
    }

    public boolean offerLast(E e) {
        return d.offerLast(e);
    }

    public void add(int index, E element) {
        d.add(index, element);
    }

    public boolean offer(E e) {
        return d.offer(e);
    }

    public void push(E e) {
        d.push(e);
    }

    public void addFirst(E e) {
        d.addFirst(e);
    }

    @Override
    public boolean isEmpty() {
        return d.isEmpty();
    }

    @Override
    public E getFirst() {
        return d.getFirst();
    }

    @Override
    public Iterator<E> iterator() {
        return d.iterator();
    }

    @Override
    public E getLast() {
        return d.getLast();
    }

    @Override
    public E removeFirst() {
        return d.removeFirst();
    }

    @Override
    public E removeLast() {
        return d.removeLast();
    }

    public boolean containsAll(Collection<?> c) {
        return d.containsAll(c);
    }

    @Override
    public boolean contains(Object o) {
        return d.contains(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        return d.listIterator();
    }

    @Override
    public int size() {
        return d.size();
    }

    public boolean removeAll(Collection<?> c) {
        return d.removeAll(c);
    }

    @Override
    public boolean remove(Object o) {
        return d.remove(o);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return d.subList(fromIndex, toIndex);
    }

    public boolean retainAll(Collection<?> c) {
        return d.retainAll(c);
    }

    public boolean addAll(Collection<? extends E> c) {
        return d.addAll(c);
    }

    @Override
    public boolean equals(Object o) {
        return d.equals(o);
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        return d.addAll(index, c);
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
    public void removeRange(int fromIndex, int toIndex) {
        d.removeRange(fromIndex, toIndex);
    }

    @Override
    public void clear() {
        d.clear();
    }

    @Override
    public E get(int index) {
        return d.get(index);
    }

    @Override
    public E remove(int index) {
        return d.remove(index);
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
    public E peek() {
        return d.peek();
    }

    @Override
    public E element() {
        return d.element();
    }

    @Override
    public E poll() {
        return d.poll();
    }

    @Override
    public E remove() {
        return d.remove();
    }

    @Override
    public E peekFirst() {
        return d.peekFirst();
    }

    @Override
    public E peekLast() {
        return d.peekLast();
    }

    @Override
    public E pollFirst() {
        return d.pollFirst();
    }

    @Override
    public E pollLast() {
        return d.pollLast();
    }

    @Override
    public E pop() {
        return d.pop();
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        return d.removeFirstOccurrence(o);
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        return d.removeLastOccurrence(o);
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return d.listIterator(index);
    }

    @Override
    public Iterator<E> descendingIterator() {
        return d.descendingIterator();
    }

    @Override
    public Object clone() {
        DLinkedList c = new DLinkedList();
        c.d = (MyLL) d.clone();
        return c;
    }

    @Override
    public Object[] toArray() {
        return d.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return d.toArray(a);
    }

    public LinkedList getOriginal() {
        return d;
    }

    public Location getCreation() {
        return creation;
    }

    public DelegateTypes getType() {
        return TYPE;
    }

    public int getCapacity() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        s.writeObject(d);
    }

    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        d = (MyLL<E>) s.readObject();
    }
}
