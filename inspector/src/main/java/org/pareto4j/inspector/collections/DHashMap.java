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

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: jparent
 * Date: 19/08/13
 * Time: 14:04
 * To change this template use File | Settings | File Templates.
 */
public class DHashMap extends HashMap implements Wrapper {
    public static final DelegateTypes TYPES = DelegateTypes.HASHMAP;

    HashMap hashMap;
    public final Location creation = new Location(new Where());

    public DHashMap() {
        super(1);
        hashMap= new HashMap();
        CollectionsData.register(this);
    }

    public DHashMap(int initialCapacity, float loadFactor) {
        super(1);
        this.hashMap = new HashMap(initialCapacity, loadFactor);
        CollectionsData.register(this);
    }

    public DHashMap(int initialCapacity) {
        super(1);
        this.hashMap = new HashMap(initialCapacity);
        CollectionsData.register(this);
    }

    public DHashMap(Map m) {
        super(1);
        hashMap= new HashMap();
        this.hashMap.putAll(m);
        CollectionsData.register(this);
    }

    public Object getOriginal() {
        return hashMap;
    }

    @Override
    public int size() {
        return hashMap.size();
    }

    @Override
    public boolean isEmpty() {
        return hashMap.isEmpty();
    }

    @Override
    public Object get(Object key) {
        return hashMap.get(key);
    }

    @Override
    public boolean containsKey(Object key) {
        return hashMap.containsKey(key);
    }

    @Override
    public Object put(Object key, Object value) {
        return hashMap.put(key, value);
    }

    @Override
    public void putAll(Map m) {
        hashMap.putAll(m);
    }

    @Override
    public Object remove(Object key) {
        return hashMap.remove(key);
    }

    @Override
    public void clear() {
        hashMap.clear();
    }

    @Override
    public boolean containsValue(Object value) {
        return hashMap.containsValue(value);
    }

    @Override
    public Object clone() {
        DHashMap r = new DHashMap();
        r.hashMap = (HashMap) hashMap.clone();
        return r;
    }

    @Override
    public Set keySet() {
        return hashMap.keySet();
    }

    @Override
    public Collection values() {
        return hashMap.values();
    }

    @Override
    public Set<Map.Entry> entrySet() {
        return hashMap.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        return hashMap.equals(o);
    }

    @Override
    public int hashCode() {
        return hashMap.hashCode();
    }

    @Override
    public String toString() {
        return hashMap.toString();
    }

    public Location getCreation() {
        return creation;
    }

    public DelegateTypes getType() {
        return TYPES;
    }

    public int getCapacity() {
        return getCapacity(this);
    }

    static Class cHM = HashMap.class;

    public static int getCapacity(HashMap m) {
        try {
            Field f = cHM.getDeclaredField("table");
            f.setAccessible(true);
            Object[] elementData = (Object[]) f.get(m);
            return elementData.length;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return -1;
    }}
