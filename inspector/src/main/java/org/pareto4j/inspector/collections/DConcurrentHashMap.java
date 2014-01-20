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
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: gebruiker
 * Date: 20/12/13
 * Time: 20:39
 * To change this template use File | Settings | File Templates.
 */
public class DConcurrentHashMap extends ConcurrentHashMap implements Wrapper {
    public static final DelegateTypes TYPES = DelegateTypes.CONCURRENTHASHMAP;
    public final Location creation = new Location(new Where());

    ConcurrentHashMap d;

    public DConcurrentHashMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
        d = new ConcurrentHashMap(initialCapacity, loadFactor, concurrencyLevel);
        CollectionsData.register(this);
    }

    public DConcurrentHashMap(int initialCapacity, float loadFactor) {
        d = new ConcurrentHashMap(initialCapacity, loadFactor);
        CollectionsData.register(this);
    }

    public DConcurrentHashMap(int initialCapacity) {
        d = new ConcurrentHashMap(initialCapacity);
        CollectionsData.register(this);
    }

    public DConcurrentHashMap() {
        d = new ConcurrentHashMap();
        CollectionsData.register(this);
    }

    public DConcurrentHashMap(Map m) {
        d = new ConcurrentHashMap();
        d.putAll(m);
        CollectionsData.register(this);
    }

    @Override
    public boolean equals(Object o) {
        return d.equals(o);
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
    public Object clone() throws CloneNotSupportedException {
        DConcurrentHashMap r = new DConcurrentHashMap();
        // todo
//        r.d =  d.clone();

        return r;
    }

    @Override
    public boolean isEmpty() {
        return d.isEmpty();
    }

    public Object getOriginal() {
        return d;
    }

    public Location getCreation() {
        return creation;
    }

    public DelegateTypes getType() {
        return TYPES;
    }

    @Override
    public int size() {
        return d.size();
    }

    public int getCapacity() {
        return -1;
    }

    @Override
    public Object get(Object key) {
        return d.get(key);
    }

    @Override
    public boolean containsKey(Object key) {
        return d.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return d.containsValue(value);
    }

    @Override
    public boolean contains(Object value) {
        return d.contains(value);
    }

    public Object put(Object key, Object value) {
        return d.put(key, value);
    }

    public Object putIfAbsent(Object key, Object value) {
        return d.putIfAbsent(key, value);
    }

    public void putAll(Map m) {
        d.putAll(m);
    }

    @Override
    public Object remove(Object key) {
        return d.remove(key);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return d.remove(key, value);
    }

    public boolean replace(Object key, Object oldValue, Object newValue) {
        return d.replace(key, oldValue, newValue);
    }

    public Object replace(Object key, Object value) {
        return d.replace(key, value);
    }

    @Override
    public void clear() {
        d.clear();
    }

    @Override
    public Set keySet() {
        return d.keySet();
    }

    @Override
    public Collection values() {
        return d.values();
    }

    @Override
    public Set<Entry> entrySet() {
        return d.entrySet();
    }

    @Override
    public Enumeration keys() {
        return d.keys();
    }

    @Override
    public Enumeration elements() {
        return d.elements();
    }
}
