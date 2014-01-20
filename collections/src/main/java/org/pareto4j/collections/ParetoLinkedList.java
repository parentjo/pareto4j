package org.pareto4j.collections;

import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jparent
 * Date: 23/12/13
 * Time: 12:50
 * To change this template use File | Settings | File Templates.
 */
public class ParetoLinkedList<E> extends ParetoArrayList<E> {
    @Override
    protected List<E> makeList(int newSize) {
        return new LinkedList<E>();
    }
}
