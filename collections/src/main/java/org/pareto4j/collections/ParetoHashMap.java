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
 * Date: 14/03/13
 * Time: 11:16
 * To change this template use File | Settings | File Templates.
 */
public class ParetoHashMap<K, V> implements Map<K, V>, Externalizable, ParetoCollectionsConstants {
    static final ParetoCollectionsStatistics paretoStatistics = ParetoCollectionsStatistics.instance;

    Object key = NOTHING;
    Object value = NOTHING;

    Map<K, V> delegate;

    public ParetoHashMap(int size) {
        paretoStatistics.paretoMapTotal.incrementAndGet();
        if (size <= 1) {
            paretoStatistics.paretoMapEmpty.incrementAndGet();
        } else {
            paretoStatistics.paretoMapGeneral.incrementAndGet();
            delegate = makeMap(size);
        }
    }

    protected Map<K, V> makeMap(int size) {
        return new HashMap<K, V>(sizer(size));
    }

    public ParetoHashMap(Map<? extends K, ? extends V> m) {
        this(m.size());
        putAll(m);
    }

    public ParetoHashMap() {
        paretoStatistics.paretoMapTotal.incrementAndGet();
        paretoStatistics.paretoMapEmpty.incrementAndGet();
    }

    public int size() {
        if (delegate != null)
            return delegate.size();

        return key == NOTHING ? 0 : 1;
    }

    public boolean isEmpty() {
        if (delegate != null)
            return delegate.isEmpty();

        return key == NOTHING;
    }

    public boolean containsKey(Object key) {
        if (delegate != null)
            return delegate.containsKey(key);

        return ParetoCollectionsUtil.eq(this.key, key);
    }

    public boolean containsValue(Object value) {
        if (delegate != null)
            return delegate.containsValue(value);

        return ParetoCollectionsUtil.eq(this.value, value);
    }

    public V get(Object key) {
        if (delegate != null)
            return delegate.get(key);

        K k = (K) this.key;
        if (k == NOTHING) return null;
        if ((k == key) || ((k != null) && k.equals(key))) return (V) value;

        return null;
    }

    public V put(K key, V value) {
        if (this.key != key && delegate == null)
            neededReplace(1);

        if (delegate != null) {
            return delegate.put(key, value);
        }

        if (eq(this.key, key)) {
            Object old = this.value;
            this.value = value;
            return (V) old;
        }
        // Not same key and already got a key-value pair :(
        if (this.key != NOTHING && this.key != key)
            throw new IllegalStateException("This impl can only store 1 pair of values");

        //
        this.key = key;
        this.value = value;
        return null;  // no previous value
    }

    public V remove(Object key) {
        if (delegate != null)
            return delegate.remove(key);


        if (eq(this.key, key)) {
            Object old = this.value;
            this.key = NOTHING;
            this.value = NOTHING;
            return (V) old;
        }

        return null;
    }

    public void putAll(Map<? extends K, ? extends V> t) {
        for (Entry<? extends K, ? extends V> entry : t.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public void clear() {
        if (delegate != null)
            delegate.clear();

        this.key = NOTHING;
        this.value = NOTHING;
    }

    public Set<K> keySet() {
        if (delegate != null)
            return delegate.keySet();

        return new Set<K>() {
            public int size() {
                return ParetoHashMap.this.size();
            }

            public boolean isEmpty() {
                return ParetoHashMap.this.isEmpty();
            }

            public boolean contains(Object o) {
                return containsKey(o);
            }

            public boolean containsAll(Collection<?> c) {
                for (Object o : c) {
                    if (!contains(o)) {
                        return false;
                    }
                }
                return true;
            }

            public Iterator<K> iterator() {
                return new ParetoIterator<K>((K) key);
            }

            public Object[] toArray() {
                Object[] ret;
                if (key == NOTHING)
                    ret = new Object[0];
                else
                    ret = new Object[]{key};

                return ret;
            }

            public <T> T[] toArray(T[] a) {
                return (T[]) copyToArray(a, size(), key);
            }

            public boolean add(K o) {
                throw new UnsupportedOperationException();
            }

            public boolean remove(Object o) {
                return ParetoHashMap.this.remove(o) != null;
            }

            public boolean addAll(Collection<? extends K> c) {
                throw new UnsupportedOperationException();
            }

            public boolean retainAll(Collection<?> c) {
                if (ParetoHashMap.this.isEmpty())
                    return false;

                if (c.isEmpty()) {
                    ParetoHashMap.this.clear();
                    return true;
                }

                if (!c.contains(key)) {
                    ParetoHashMap.this.clear();
                    return true;
                }

                return false;
            }

            public boolean removeAll(Collection<?> c) {
                boolean ret = false;
                for (Object o : c) {
                    ret = remove(o) || ret;
                }

                return ret;
            }

            public void clear() {
                ParetoHashMap.this.clear();
            }

            public String toString() {
                return ParetoCollectionsUtil.toString(this, null);
            }

            public boolean equals(Object o) {
                if (o == this)
                    return true;

                if (!(o instanceof Set))
                    return false;

                return ParetoCollectionsUtil.equals(this, (Set) o);
            }

            @Override
            public int hashCode() {
                return ParetoCollectionsUtil.hashCode(this);
            }
        };
    }

    public Collection<V> values() {
        if (delegate != null)
            return delegate.values();

        return new Set<V>() {
            public int size() {
                return ParetoHashMap.this.size();
            }

            public boolean isEmpty() {
                return ParetoHashMap.this.isEmpty();
            }

            public boolean contains(Object o) {
                return containsValue(o);
            }

            public boolean containsAll(Collection<?> c) {
                for (Object o : c) {
                    if (!contains(o)) {
                        return false;
                    }
                }
                return true;
            }

            public Iterator<V> iterator() {
                return new ParetoIterator<V>((V) value);
            }

            public Object[] toArray() {
                Object[] ret;
                if (value == NOTHING)
                    ret = new Object[0];
                else
                    ret = new Object[]{value};

                return ret;
            }

            public <T> T[] toArray(T[] a) {
                return (T[]) copyToArray(a, size(), value);
            }

            public boolean add(V o) {
                throw new UnsupportedOperationException();
            }

            public boolean remove(Object o) {
                if (eq(o, value)) {
                    ParetoHashMap.this.clear();
                    return true;
                }

                return false;
            }

            public boolean addAll(Collection<? extends V> c) {
                throw new UnsupportedOperationException();
            }

            public boolean retainAll(Collection<?> c) {
                if (ParetoHashMap.this.isEmpty())
                    return false;

                if (c.isEmpty()) {
                    ParetoHashMap.this.clear();
                    return true;
                }

                if (!c.contains(value)) {
                    ParetoHashMap.this.clear();
                    return true;
                }

                return false;
            }

            public boolean removeAll(Collection<?> c) {
                boolean ret = false;
                for (Object o : c) {
                    ret = remove(o) || ret;
                }

                return ret;
            }

            public void clear() {
                ParetoHashMap.this.clear();
            }

            public String toString() {
                return ParetoCollectionsUtil.toString(this, null);
            }

            public boolean equals(Object o) {
                if (o == this)
                    return true;

                if (!(o instanceof Set))
                    return false;

                return ParetoCollectionsUtil.equals(this, (Set) o);
            }

            @Override
            public int hashCode() {
                return ParetoCollectionsUtil.hashCode(this);
            }
        };
    }

    public Set<Entry<K, V>> entrySet() {
        if (delegate != null)
            return delegate.entrySet();

        return new Set<Entry<K, V>>() {
            public int size() {
                return ParetoHashMap.this.size();
            }

            public boolean isEmpty() {
                return ParetoHashMap.this.isEmpty();
            }

            public boolean contains(Object o) {
                return !isEmpty() && iterator().next().equals(o);
            }

            public boolean containsAll(Collection<?> c) {
                for (Object o : c) {
                    if (!contains(o)) {
                        return false;
                    }
                }
                return true;
            }

            public Iterator<Entry<K, V>> iterator() {
                return new ParetoIterator<Entry<K, V>>(ParetoHashMap.this.isEmpty() ? NOTHING : new OneEntry()) {
                    @Override
                    public void remove() {
                        super.remove();
                        ParetoHashMap.this.clear();
                    }
                };
            }

            public Object[] toArray() {
                if (size() == 0)
                    return new Object[0];
                else
                    return new Object[]{new OneEntry()};
            }

            public <T> T[] toArray(T[] a) {
                return (T[]) copyToArray(a, size(), new OneEntry());
            }

            public boolean add(Entry<K, V> o) {
                throw new UnsupportedOperationException();
            }

            public boolean remove(Object o) {
                if (new OneEntry().equals(o)) {
                    ParetoHashMap.this.clear();
                    return true;
                }
                return false;
            }

            public boolean addAll(Collection<? extends Entry<K, V>> c) {
                throw new UnsupportedOperationException();
            }

            public boolean retainAll(Collection<?> c) {
                if (ParetoHashMap.this.isEmpty())
                    return false;

                if (c.isEmpty()) {
                    ParetoHashMap.this.clear();
                    return true;
                }

                if (!c.contains(new OneEntry())) {
                    ParetoHashMap.this.clear();
                    return true;
                }

                return false;
            }

            public boolean removeAll(Collection<?> c) {
                if (c.contains(new OneEntry())) {
                    ParetoHashMap.this.clear();
                    return true;
                }
                return false;
            }

            public void clear() {
                ParetoHashMap.this.clear();
            }

            public String toString() {
                return ParetoCollectionsUtil.toString(this, null);
            }

            public boolean equals(Object o) {
                if (o == this)
                    return true;

                if (!(o instanceof Set))
                    return false;

                return ParetoCollectionsUtil.equals(this, (Set) o);
            }

            @Override
            public int hashCode() {
                return ParetoCollectionsUtil.hashCode(this);
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (delegate != null)
            return delegate.equals(o);

        if (this == o) return true;
        if (!(o instanceof Map)) return false;

        Map m = (Map) o;

        if (size() != m.size())
            return false;
        for (Object obj : m.entrySet()) {
            Map.Entry e = (Entry) obj;
            Object key = e.getKey();
            if (!eq(key, this.key)) return false;
            if (!eq(e.getValue(), value)) return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        if (delegate != null)
            return delegate.hashCode();

        if (isEmpty())
            return 0;

        return entryHashCode(key, value);
    }

    void neededReplace(Map c) {
        if (c != null) {
            neededReplace(c.size());
        }
    }

    void neededReplace(int add) {
        if (delegate != null)
            return;

        int newSize = size() + add;
        if (newSize == 1) {
            paretoStatistics.paretoMapEmpty.decrementAndGet();
            paretoStatistics.paretoMapOne.incrementAndGet();
        } else if (newSize > 1) {
            paretoStatistics.paretoMapOne.decrementAndGet();
            paretoStatistics.paretoMapGeneral.incrementAndGet();

            paretoStatistics.paretoMapExpands.incrementAndGet();
            Map<K, V> newMap = makeMap(newSize);
            newMap.putAll(this);
            delegate = newMap;
        }
    }

    static int sizer(int target) {
        return sizer(target, 0.75);
    }

    /**
     * Determines the size of the hash Map or Set to allocate in order to avoid a resize
     * operation (doubling size and copying) upon insertion
     *
     * @param target
     * @param loadFactor
     * @return
     */
    static int sizer(int target, double loadFactor) {
        int exp = 2;
        while ((exp * loadFactor) <= target) {
            exp <<= 1;
        }

        return exp;
    }


    public Map<K, V> getDelegate() {
        return delegate;
    }

    static boolean eq(Object a, Object b) {
        if (a == b) return true;

        if (a == null) return false;

        return a.equals(b);
    }

    static public <T> Object[] copyToArray(Object[] a, int size, Object value) {
        Object[] ret = a;
        if (a.length < size)
            ret = (Object[]) Array.newInstance(a.getClass().getComponentType(), size);

        if (size > 0 && ret.length > 0)
            ret[0] = value;
        if (ret.length > size)
            ret[size] = null;

        return (T[]) ret;
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
            out.writeByte(ONE);
            out.writeObject(key);
            out.writeObject(value);
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
            delegate = (Map<K, V>) in.readObject();
        } else {
            key = in.readObject();
            value = in.readObject();
        }
    }

    public class OneEntry implements Map.Entry {

        public Object getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        public Object setValue(Object value) {
            Object old = value;
            ParetoHashMap.this.value = value;
            return old;
        }

        public String toString() {
            return getKey() + "=" + getValue();
        }

        /**
         * Compares the specified object with this entry for equality.
         * Returns <tt>true</tt> if the given object is also a map entry and
         * the two entries represent the same mapping.  More formally, two
         * entries <tt>e1</tt> and <tt>e2</tt> represent the same mapping
         * if<pre>
         *     (e1.getKey()==null ?
         *      e2.getKey()==null : e1.getKey().equals(e2.getKey()))  &&
         *     (e1.getValue()==null ?
         *      e2.getValue()==null : e1.getValue().equals(e2.getValue()))
         * </pre>
         * This ensures that the <tt>equals</tt> method works properly across
         * different implementations of the <tt>Map.Entry</tt> interface.
         *
         * @param o object to be compared for equality with this map entry.
         * @return <tt>true</tt> if the specified object is equal to this map
         * entry.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry e = (Map.Entry) o;
            Object k1 = getKey();
            Object k2 = e.getKey();
            if (k1 == k2 || (k1 != null && k1.equals(k2))) {
                Object v1 = getValue();
                Object v2 = e.getValue();
                if (v1 == v2 || (v1 != null && v1.equals(v2)))
                    return true;
            }
            return false;
        }

        /**
         * Returns the hash code value for this map entry.  The hash code
         * of a map entry <tt>e</tt> is defined to be: <pre>
         *     (e.getKey()==null   ? 0 : e.getKey().hashCode()) ^
         *     (e.getValue()==null ? 0 : e.getValue().hashCode())
         * </pre>
         * This ensures that <tt>e1.equals(e2)</tt> implies that
         * <tt>e1.hashCode()==e2.hashCode()</tt> for any two Entries
         * <tt>e1</tt> and <tt>e2</tt>, as required by the general
         * contract of <tt>Object.hashCode</tt>.
         *
         * @return the hash code value for this map entry.
         * @see Object#hashCode()
         * @see Object#equals(Object)
         * @see #equals(Object)
         */
        @Override
        public int hashCode() {
            return entryHashCode(getKey(), getValue());
        }

    }

    public static <K, V> int entryHashCode(K k, V v) {
        int result = k != null ? (k == null ? 0 : k.hashCode()) : 0;
        return result ^ (v != null ? v.hashCode() : 0);
    }

    public String toString() {
        if (delegate != null)
            return delegate.toString();

        if (isEmpty()) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        K key = (K) this.key;
        V value = (V) this.value;
        sb.append(key == this ? "(this Map)" : key);
        sb.append('=');
        sb.append(value == this ? "(this Map)" : value);
        return sb.append('}').toString();
    }

}
