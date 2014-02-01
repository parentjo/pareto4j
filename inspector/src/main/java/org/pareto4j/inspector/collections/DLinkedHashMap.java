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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: jparent
 * Date: 10/09/13
 * Time: 9:47
 * To change this template use File | Settings | File Templates.
 */
public class DLinkedHashMap extends LinkedHashMap implements Wrapper {
    public static final DelegateTypes TYPES = DelegateTypes.LINKEDHASHMAP;

    MyLHM linkedHashMap;
    public final Location creation = Location.create(new Where());

    private static class MyLHM extends LinkedHashMap {
        private MyLHM(int initialCapacity, float loadFactor) {
            super(initialCapacity, loadFactor);
        }

        private MyLHM(int initialCapacity) {
            super(initialCapacity);
        }

        private MyLHM() {
        }

        private MyLHM(Map m) {
            super(m);
        }

        private MyLHM(int initialCapacity, float loadFactor, boolean accessOrder) {
            super(initialCapacity, loadFactor, accessOrder);
        }

        protected boolean removeEldestEntry(Map.Entry eldest) {
            return super.removeEldestEntry(eldest);
        }
    }

    public DLinkedHashMap() {
        linkedHashMap = new MyLHM();
        CollectionsData.register(this);
    }

    public DLinkedHashMap(int initialCapacity, float loadFactor) {
        super(0);
        this.linkedHashMap = new MyLHM();
        CollectionsData.register(this);
    }

    public DLinkedHashMap(int initialCapacity) {
        super(0);
        this.linkedHashMap = new MyLHM();
        CollectionsData.register(this);
    }

    public DLinkedHashMap(Map m) {
        linkedHashMap = new MyLHM();
        linkedHashMap.putAll(m);
        CollectionsData.register(this);
    }

    public DLinkedHashMap(int initialCapacity, float loadFactor, boolean accessOrder) {
        linkedHashMap = new MyLHM(initialCapacity, loadFactor, accessOrder);
        CollectionsData.register(this);
    }

    public Object getOriginal() {
        return linkedHashMap;
    }

    @Override
    public int size() {
        return linkedHashMap.size();
    }

    @Override
    public boolean isEmpty() {
        return linkedHashMap.isEmpty();
    }

    @Override
    public Object get(Object key) {
        return linkedHashMap.get(key);
    }

    @Override
    public boolean containsKey(Object key) {
        return linkedHashMap.containsKey(key);
    }

    @Override
    public Object put(Object key, Object value) {
        return linkedHashMap.put(key, value);
    }

    @Override
    public void putAll(Map m) {
        linkedHashMap.putAll(m);
    }

    @Override
    public Object remove(Object key) {
        return linkedHashMap.remove(key);
    }

    @Override
    public void clear() {
        linkedHashMap.clear();
    }

    @Override
    public boolean containsValue(Object value) {
        return linkedHashMap.containsValue(value);
    }

    @Override
    public Object clone() {
        DLinkedHashMap r = new DLinkedHashMap();
        r.linkedHashMap = (MyLHM) linkedHashMap.clone();
        return r;
    }

    @Override
    public Set keySet() {
        return linkedHashMap.keySet();
    }

    @Override
    public Collection values() {
        return linkedHashMap.values();
    }

    @Override
    public Set<Map.Entry> entrySet() {
        return linkedHashMap.entrySet();
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        return linkedHashMap.equals(o);
    }

    @Override
    public int hashCode() {
        return linkedHashMap.hashCode();
    }

    @Override
    public String toString() {
        return linkedHashMap.toString();
    }

    public Location getCreation() {
        return creation;
    }

    public DelegateTypes getType() {
        return TYPES;
    }

    public int getCapacity() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
