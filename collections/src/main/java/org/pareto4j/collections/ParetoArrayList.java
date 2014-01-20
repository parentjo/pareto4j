package org.pareto4j.collections;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Array;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: jparent
 * Date: 23/12/13
 * Time: 12:49
 * To change this template use File | Settings | File Templates.
 */
public class ParetoArrayList<E> implements List<E>, Externalizable, ParetoCollectionsConstants {
    static final ParetoCollectionsStatistics paretoStatistics = ParetoCollectionsStatistics.instance;

    Object e = NOTHING;
    protected List<E> delegate;

    public ParetoArrayList() {
        paretoStatistics.paretoListTotal.incrementAndGet();
        paretoStatistics.paretoListEmpty.incrementAndGet();
    }


    public ParetoArrayList(int size) {
        paretoStatistics.paretoListTotal.incrementAndGet();
        if (size <= 1) {
            paretoStatistics.paretoListEmpty.incrementAndGet();
        } else {
            paretoStatistics.paretoListGeneral.incrementAndGet();
            delegate = makeList(size);
        }
    }

    public ParetoArrayList(List<E> in) {
        this(in.size());

        addAll(in);
    }

    public int size() {
        if (delegate != null)
            return delegate.size();

        return e == NOTHING ? 0 : 1;
    }

    public boolean isEmpty() {
        if (delegate != null)
            return delegate.isEmpty();

        return e == NOTHING;
    }

    public boolean contains(Object o) {
        if (delegate != null) {
            return delegate.contains(o);
        }

        if (e == NOTHING)
            return false;

        return o == e || (e != null && e.equals(o));
    }

    public Iterator<E> iterator() {
        if (delegate != null)
            return delegate.iterator();

        return new ParetoIterator<E>(e) {
            @Override
            public void remove() {
                super.remove();
                ParetoArrayList.this.clear();
            }
        };
    }

    public Object[] toArray() {
        if (delegate != null)
            return delegate.toArray();

        Object[] ret = new Object[size()];
        if (e != NOTHING)
            ret[0] = e;

        return ret;
    }

    public <T> T[] toArray(T[] a) {
        if (delegate != null)
            return delegate.toArray(a);

        Object[] ret = a;
        if (a.length < size())
            ret = (Object[]) Array.newInstance(a.getClass().getComponentType(), size());

        if (e != NOTHING)
            ret[0] = e;
        if (ret.length > size())
            ret[size()] = null;

        return (T[]) ret;
    }

    public boolean add(E o) {
        if (delegate == null)
            neededReplace(1);

        if (delegate != null)
            return delegate.add(o);

        if (e == NOTHING) {
            e = o;
            return true;
        }

        throw new IllegalStateException("Can not add more that one to this List impl");
    }

    public boolean remove(Object o) {
        if (delegate != null)
            return delegate.remove(o);

        if (e == NOTHING) return false;
        if (e == o || (e != null && e.equals(o))) {
            e = NOTHING;
            return true;
        }

        return false;
    }

    public boolean containsAll(Collection<?> c) {
        if (delegate != null)
            return delegate.containsAll(c);

        for (Object e1 : c) {
            if (!contains(e1)) return false;
        }
        return true;
    }

    public boolean addAll(Collection<? extends E> c) {
        if (delegate == null)
            neededReplace(c);

        if (delegate != null)
            return delegate.addAll(c);

        boolean ret = false;
        for (E e1 : c) {
            ret = add(e1) || ret;
        }
        return ret;
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        if (c.isEmpty())
            return false;

        if (delegate == null)
            neededReplace(size() + c.size());

        if (delegate != null)
            return delegate.addAll(index, c);

        if (index < 0 || index > size())
            throw new IndexOutOfBoundsException();

        if (index < 0 || index > size()) throw new IndexOutOfBoundsException();

//        if (index > 0) throw new IllegalArgumentException("Index exceeds implementation size");
        if (size() + c.size() > 1)
            throw new IllegalArgumentException("New size exceeds implementation size");


        boolean ret = false;
        for (E e1 : c) {
            ret = add(e1) || ret;
        }
        return ret;
    }

    public boolean removeAll(Collection<?> c) {
        if (delegate != null)
            return delegate.removeAll(c);

        boolean ret = false;
        for (Object e1 : c) {
            ret = remove(e1) || ret;
        }
        return ret;
    }

    public boolean retainAll(Collection<?> c) {
        if (delegate != null)
            return delegate.retainAll(c);

        if (isEmpty())
            return false;

        if (c.isEmpty()) {
            clear();
            return true;
        }

        if (!c.contains(e)) {
            clear();
            return true;
        }

        return false;
    }

    public void clear() {
        if (delegate != null)
            delegate.clear();

        e = NOTHING;
    }

    @Override
    public boolean equals(Object o) {
        if (delegate != null)
            return delegate.equals(o);

        if (this == o) return true;
        if (!(o instanceof List)) return false;

        List that = (List) o;

        if (that.size() != size()) return false;
        if (e != NOTHING && !that.contains(e)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        if (delegate != null)
            return delegate.hashCode();

        if (e == NOTHING)
            return 1;

        // Why 31 see javadoc java.util.List.hashCode()
        return 31 + (e != null ? e.hashCode() : 0);
    }

    public E get(int index) {
        if (delegate != null)
            return delegate.get(index);

        if (index < 0 || index >= size()) throw new IndexOutOfBoundsException();

        return (E) e;
    }

    public E set(int index, E element) {
        if (delegate != null)
            return delegate.set(index, element);

        if (index < 0 || index >= size()) throw new IndexOutOfBoundsException();

        E old = (E) e;
        e = element;
        return old;
    }

    public void add(int index, E element) {
        if (delegate == null)
            neededReplace(size() + 1);

        if (delegate != null) {
            delegate.add(index, element);
            return;
        }

        if (index < 0 || index > size())
            throw new IndexOutOfBoundsException();

        if (size() + 1 > 1)
            throw new IllegalStateException("Can not store more than one value in this impl.");

        e = element;
    }

    public E remove(int index) {
        if (delegate != null)
            return delegate.remove(index);

        if (index < 0 || index >= size()) throw new IndexOutOfBoundsException();
        E old = (E) e;
        e = NOTHING;

        return old;
    }

    public int indexOf(Object o) {
        if (delegate != null)
            return delegate.indexOf(o);

        if (e == NOTHING)
            return -1;

        if (e == o)
            return 0;

        // Handle case where e == null or O == null
        if (e != o && (e == null || o == null))
            return -1;

        if (e.equals(0))
            return 0;

        return -1;
    }

    public int lastIndexOf(Object o) {
        if (delegate != null)
            return delegate.lastIndexOf(o);

        return indexOf(o);
    }

    public ListIterator<E> listIterator() {
        if (delegate != null)
            return delegate.listIterator();

        return createListIterator();
    }

    private ListIterator<E> createListIterator() {
        return new ListIterator<E>() {
            Object local = e;
            ListIterator<E> del = null;

            public boolean hasNext() {
                if (del != null)
                    return del.hasNext();

                return local != NOTHING;
            }

            public E next() {
                if (del != null)
                    return del.next();

                if (local == NOTHING) throw new NoSuchElementException();
                E ret = (E) local;
                local = NOTHING;
                return ret;
            }

            public void remove() {
                if (del != null) {
                    del.remove();
                    return;
                }

                ParetoArrayList.this.e = NOTHING;
            }

            public boolean hasPrevious() {
                if (del != null)
                    return del.hasPrevious();

                return (local == NOTHING && e != NOTHING);
            }

            public E previous() {
                if (del != null)
                    return del.previous();

                if (local == NOTHING)
                    if (e != NOTHING) {
                        local = e;
                        return (E) local;
                    } else {
                        throw new NoSuchElementException();
                    }

                throw new UnsupportedOperationException();
            }

            public int nextIndex() {
                if (del != null)
                    return del.nextIndex();

                if (local == NOTHING) throw new NoSuchElementException();

                throw new UnsupportedOperationException();
            }

            public int previousIndex() {
                if (del != null)
                    return del.previousIndex();

                if (local == NOTHING) throw new NoSuchElementException();

                throw new UnsupportedOperationException();
            }

            public void set(E o) {
                if (del != null) {
                    del.set(o);
                    return;
                }

                if (size() > 0)
                    e = o;
                else
                    throw new IllegalStateException();
            }

            public void add(E o) {
                if (del != null) {
                    del.add(o);
                    return;
                }

                if (size() == 0)
                    e = o;
                else {
                    ParetoArrayList.this.add(o);
                    del = ParetoArrayList.this.listIterator();
                }
            }
        };
    }

    public ListIterator<E> listIterator(int index) {
        if (delegate != null)
            return delegate.listIterator(index);

        if (index > size() || index < 0)
            throw new IndexOutOfBoundsException();

        return createListIterator();
    }

    public List<E> subList(int fromIndex, int toIndex) {
        if (delegate != null)
            return delegate.subList(fromIndex, toIndex);

        if (fromIndex < 0)
            throw new IndexOutOfBoundsException();

        if (toIndex > size())
            throw new IndexOutOfBoundsException();

        if (toIndex < fromIndex)
            throw new IndexOutOfBoundsException();

        if (isEmpty() || (fromIndex == 0 && toIndex == 1))
            return this;

        if (fromIndex == 0 && toIndex == 0) {
            // return "view"
            return new EmptySubList<E>();
        }

        return this;
    }

    class EmptySubList<E> implements List<E> {

        public int size() {
            if (delegate != null)
                return delegate.size();

            return 0;
        }

        public boolean isEmpty() {
            if (delegate != null)
                return delegate.isEmpty();

            return true;
        }

        public boolean contains(Object o) {
            if (delegate != null)
                return delegate.contains(o);

            return false;
        }

        public Iterator<E> iterator() {
            if (delegate != null)
                return (Iterator<E>) delegate.iterator();

            return null;
        }

        public Object[] toArray() {
            if (delegate != null)
                return delegate.toArray();

            return new Object[0];
        }

        public <T> T[] toArray(T[] a) {
            if (delegate != null)
                return delegate.toArray(a);

            if (a != null && a.length >= 1)
                a[0] = null;

            return a;
        }

        public boolean add(E e) {
            throw new UnsupportedOperationException();
        }

        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean containsAll(Collection<?> c) {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public boolean addAll(Collection<? extends E> c) {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public boolean addAll(int index, Collection<? extends E> c) {
            throw new UnsupportedOperationException();
        }

        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }

        public E get(int index) {
            throw new UnsupportedOperationException();
        }

        public E set(int index, E element) {
            throw new UnsupportedOperationException();
        }

        public void add(int index, E element) {
            throw new UnsupportedOperationException();
        }

        public E remove(int index) {
            throw new UnsupportedOperationException();
        }

        public int indexOf(Object o) {
            throw new UnsupportedOperationException();
        }

        public int lastIndexOf(Object o) {
            throw new UnsupportedOperationException();
        }

        public ListIterator<E> listIterator() {
            throw new UnsupportedOperationException();
        }

        public ListIterator<E> listIterator(int index) {
            throw new UnsupportedOperationException();
        }

        public List<E> subList(int fromIndex, int toIndex) {
            return this;
        }
    }

    void neededReplace(Collection c) {
        if (c != null) {
            neededReplace(c.size());
        }
    }

    void neededReplace(int add) {
        if (delegate != null)
            return;

        replace(add);
    }

    void replace(int add) {
        int newSize = size() + add;

        if (newSize == 1) {
            paretoStatistics.paretoListEmpty.decrementAndGet();
            paretoStatistics.paretoListOne.incrementAndGet();
        } else if (newSize > 1) {
            paretoStatistics.paretoListOne.decrementAndGet();
            paretoStatistics.paretoListExpands.incrementAndGet();

            paretoStatistics.paretoListGeneral.incrementAndGet();
            List<E> newList = makeList(newSize);
            newList.addAll(this);
            delegate = newList;
        }
    }

    protected List<E> makeList(int newSize) {
        return new ArrayList<E>(newSize);
    }

    /**
     * Returns the underlying delegate
     *
     * @return
     */
    public List<E> getDelegate() {
        return delegate;
    }

    /**
     * Always encode at least a byte. The special REFERENCE value short cuts the logic.
     *
     * @param out
     * @throws java.io.IOException
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        if (delegate != null) {
            out.writeByte(GENERAL);
            out.writeObject(delegate);
        } else {
            if (e != NOTHING) {
                out.writeByte(ONE);
                out.writeObject(e);
            } else {
                out.writeByte(NONE);
            }
        }
    }

    /**
     * The first byte determines which scenario it is. In case of a 'NONE' value nothing more needs to be done.
     * Type 'ONE' means the one (set) of values is represented. 'GENERAL' encodes the use a the delegate.
     *
     * @param in
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        byte type = in.readByte();
        if (type == NONE)
            return;

        if (type == GENERAL) {
            delegate = (List<E>) in.readObject();
        } else {
            if (type == ONE) {
                e = in.readObject();
            }
        }
    }

    @Override
    public String toString() {
        return ParetoCollectionsUtil.toString(this, delegate);
    }
}
