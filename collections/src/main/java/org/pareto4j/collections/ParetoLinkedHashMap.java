package org.pareto4j.collections;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jparent
 * Date: 23/12/13
 * Time: 12:51
 * To change this template use File | Settings | File Templates.
 */
public class ParetoLinkedHashMap<K, V> extends ParetoHashMap<K, V> {
    @Override
    protected Map<K, V> makeMap(int size) {
        return new LinkedHashMap<K, V>(sizer(size));
    }
}