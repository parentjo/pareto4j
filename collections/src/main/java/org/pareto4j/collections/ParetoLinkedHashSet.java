package org.pareto4j.collections;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: jparent
 * Date: 23/12/13
 * Time: 12:51
 * To change this template use File | Settings | File Templates.
 */
public class ParetoLinkedHashSet<E> extends ParetoHashSet<E> {
    public ParetoLinkedHashSet() {
    }

    public ParetoLinkedHashSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public ParetoLinkedHashSet(int initialCapacity) {
        super(initialCapacity);
    }

    public ParetoLinkedHashSet(Collection<? extends E> c) {
        super(c);
    }

    @Override
    protected Set<E> makeSet(int initialCapacity) {
        return new LinkedHashSet<E>(initialCapacity);
    }
}
