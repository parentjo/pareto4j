package org.pareto4j.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created with IntelliJ IDEA.
 * User: jparent
 * Date: 13/03/13
 * Time: 14:39
 * To change this template use File | Settings | File Templates.
 */
public class ParetoIterator<E> implements Iterator<E>, ParetoCollectionsConstants {
    Object local;
    int left;

    public ParetoIterator(Object local) {
        this.local = (E) local;
        left = (local != NOTHING) ? 1 : 0;

    }

    public boolean hasNext() {
        return left != 0;
    }


    public E next() {
        if (left == 0) throw new NoSuchElementException();
        left = 0;

        return (E) local;
    }

/*
    public E next() {
        if (local == NOTHING) throw new NoSuchElementException();
        E ret = (E) local;
        local = NOTHING;
        return ret;
    }
*/

    public void remove() {
        if (left == 0 && local == NOTHING) throw new IllegalStateException();
        left = 0;
        local = NOTHING;
    }
}
