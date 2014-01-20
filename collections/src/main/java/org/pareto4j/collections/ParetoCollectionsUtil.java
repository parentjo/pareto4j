package org.pareto4j.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: jparent
 * Date: 23/12/13
 * Time: 10:01
 * To change this template use File | Settings | File Templates.
 */
public class ParetoCollectionsUtil {
    static public <E, D> String toString(Collection<E> c, Object delegate) {
        if (delegate != null)
            return delegate.toString();

        Iterator<E> it = c.iterator();
        if (!it.hasNext())
            return "[]";

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (; ; ) {
            E e = it.next();
            sb.append(e == c ? "(this Collection)" : e);
            if (!it.hasNext())
                return sb.append(']').toString();
            sb.append(',').append(' ');
        }
    }

    static public boolean equals(Set a, Set b) {
        if (a == b)
            return true;

        if (a.size() != b.size())
            return false;
        try {
            return a.containsAll(b);
        } catch (ClassCastException unused) {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }

    }

    static public <E> int hashCode(Set<E> s) {
        int h = 0;
        Iterator<E> i = s.iterator();
        while (i.hasNext()) {
            E obj = i.next();
            if (obj != null)
                h += obj.hashCode();
        }
        return h;
    }

    static public boolean eq(Object a, Object b) {
        if (a == b) return true;

        if (a == null) return false;

        return a.equals(b);
    }
}
