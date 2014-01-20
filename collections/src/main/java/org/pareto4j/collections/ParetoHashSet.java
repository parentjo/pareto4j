package org.pareto4j.collections;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: jparent
 * Date: 21/06/13
 * Time: 10:47
 * To change this template use File | Settings | File Templates.
 */
public class ParetoHashSet<E> implements Set<E>, Externalizable, ParetoCollectionsConstants {
    static final ParetoCollectionsStatistics paretoStatistics = ParetoCollectionsStatistics.instance;
    Object e = NOTHING;
    Set<E> delegate;

    public ParetoHashSet() {
        paretoStatistics.paretoSetEmpty.incrementAndGet();
        paretoStatistics.paretoSetTotal.incrementAndGet();
    }

    public ParetoHashSet(Collection c) {
        this(c.size());
        addAll(c);
    }

    public ParetoHashSet(int initialCapacity, float loadFactor) {
        this(initialCapacity);
    }

    public ParetoHashSet(int initialCapacity) {
        paretoStatistics.paretoSetTotal.incrementAndGet();
        if (initialCapacity <= 1) {
            paretoStatistics.paretoSetEmpty.incrementAndGet();
        } else {
            paretoStatistics.paretoSetGeneral.incrementAndGet();
            delegate = makeSet(initialCapacity);
        }
    }

    protected Set<E> makeSet(int initialCapacity) {
        return new HashSet<E>(initialCapacity);
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
        if (delegate != null)
            return delegate.contains(o);

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
                ParetoHashSet.this.clear();
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
        if (delegate == null && !ParetoCollectionsUtil.eq(e, o))
            neededReplace(1);

        if (delegate != null)
            return delegate.add(o);

        if (ParetoCollectionsUtil.eq(e, o))
            return false;

        if (e == NOTHING) {
            e = o;
            return true;
        }

        throw new IllegalStateException("Can not add more that one to this Set impl");
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

        boolean ret = true;
        for (Object e1 : c) {
            ret = contains(e1) && ret;
        }
        return ret;
    }

    public boolean addAll(Collection<? extends E> c) {
        neededReplace(c);
        if (delegate != null)
            return delegate.addAll(c);

        //
        boolean ret = false;
        for (E e1 : c) {
            ret = add(e1) || ret;
        }

        return ret;
    }

    public boolean retainAll(Collection<?> c) {
        if (delegate != null)
            return delegate.retainAll(c);

        if (isEmpty())
            return false;

        if (!c.contains(e)) {
            e = NOTHING;
            return true;
        }

        return false;
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
        if (!(o instanceof Set)) return false;

        Set that = (Set) o;

        if (that.size() != size()) return false;
        if (e != NOTHING && !that.contains(e)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        if (delegate != null)
            return delegate.hashCode();

        if (e == NOTHING)
            return 0;

        // Why 31 see javadoc java.util.List.hashCode()
        return (e != null ? e.hashCode() : 0);
    }

    void neededReplace(Collection c) {
        if (c != null) {
            neededReplace(c.size());
        }
    }

    void neededReplace(int add) {
        if (delegate != null)
            return;

        int newSize = size() + add;

        if (newSize == 1) {
            paretoStatistics.paretoSetEmpty.decrementAndGet();
            paretoStatistics.paretoSetOne.incrementAndGet();
        } else if (newSize > 1) {
            if (e == NOTHING)
                paretoStatistics.paretoSetEmpty.decrementAndGet();
            else
                paretoStatistics.paretoSetOne.decrementAndGet();

            paretoStatistics.paretoSetExpands.incrementAndGet();
            paretoStatistics.paretoSetGeneral.incrementAndGet();
            Set<E> newSet = makeSet(newSize);
            newSet.addAll(this);
            delegate = newSet;
        }
    }

    /**
     * Always encode at least a byte. The special REFERENCE value short cuts the logic.
     *
     * @param out
     * @throws IOException
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        if (isEmpty()) {
            out.writeByte(NONE);
            return;
        }

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
            delegate = (Set<E>) in.readObject();
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

    public Set<E> getDelegate() {
        return delegate;
    }
}
