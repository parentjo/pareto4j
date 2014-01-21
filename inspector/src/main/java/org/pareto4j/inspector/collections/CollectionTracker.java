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

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.*;

import static org.pareto4j.inspector.collections.State.ALIVE;
import static org.pareto4j.inspector.collections.State.DEAD;

/**
 * Created with IntelliJ IDEA.
 * User: jparent
 * Date: 13/08/13
 * Time: 12:43
 * To change this template use File | Settings | File Templates.
 */
public class CollectionTracker {
    private static final Logger logger = Logger.getLogger("Collections");
    public static final CollectionTracker COLLECTION_TRACKER = new CollectionTracker();

    static public long threshold = Integer.valueOf(System.getProperty("sizeThreshold", "2")).intValue();
    static public boolean computeCapacity = System.getProperty("capacity") != null;

    /**
     * Statistics for all the type we track
     */
    Map<DelegateTypes, Statistics> deadStatistics = new EnumMap<DelegateTypes, Statistics>(DelegateTypes.class);

    /**
     * Reference queues (weak refs) used to detect whether an instance has been GC-ed
     */
    final Map<DelegateTypes, ReferenceQueue<Wrapper>> queueMap =
            new EnumMap<DelegateTypes, ReferenceQueue<Wrapper>>(DelegateTypes.class);

    /**
     * Track the instantiations by location, this overall= live+dead instances
     */
    private Map<DelegateTypes, Map<Location, Info>> allocationsMap
            = new EnumMap<DelegateTypes, Map<Location, Info>>(DelegateTypes.class);

    /**
     * Live instances. Maps from a Reference to the actual backing instance
     */
    private Map<DelegateTypes, Map<Reference, Object>> liveInstancesMap =
            new EnumMap<DelegateTypes, Map<Reference, Object>>(DelegateTypes.class);

    final BlockingQueue<Wrapper> registerQueue = new LinkedBlockingQueue<Wrapper>();

    final Object lock = new Object();

    public CollectionTracker() {
        Handler[] handlers = logger.getHandlers();
        if (handlers != null && handlers.length == 0) {
            try {
                FileHandler fh = new FileHandler("pareto4j_log.%u.%g.txt", 50 * 1024 * 1024, 10, false);
                fh.setFormatter(new SimpleFormatter());
                logger.addHandler(fh);

            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        setup();
    }

    private void setup() {
        //
        for (final DelegateTypes delegateTypes : DelegateTypes.values()) {
            queueMap.put(delegateTypes, new ReferenceQueue<Wrapper>());
            liveInstancesMap.put(delegateTypes, new TreeMap<Reference, Object>());
            allocationsMap.put(delegateTypes, new TreeMap<Location, Info>());
            deadStatistics.put(delegateTypes, new Statistics());
            start(new Runnable() {
                public void run() {
                    collect(delegateTypes, queueMap.get(delegateTypes), liveInstancesMap.get(delegateTypes));
                }
            }, delegateTypes.name() + "Tracker");
        }
        //
        start(new Runnable() {
            public void run() {
                processRegisterQueue();
            }
        }, "RegisterQueueProcessor");
    }

    private void start(Runnable runnable, String name) {
        Thread t = new Thread(runnable, name + "-" + System.identityHashCode(this));
        t.setDaemon(true);
        t.start();
    }

    /**
     * Look at the queue for the tracked type. If instance got GC-ed they'll get put in the queue. Now
     * access the actual instance and:
     * <ul>
     * <li>remove it so it the backing instance can be GC-ed too</li>
     * <li>updateDeadStatistics the statistics for the tracked type</li>
     * </ul>
     *
     * @param delegateTypes
     * @param q
     * @param liveInstances
     */
    private void collect(DelegateTypes delegateTypes, ReferenceQueue<Wrapper> q, Map<Reference, Object> liveInstances) {

        while (true) {
            Reference<? extends Wrapper> death = null;
            try {
                death = q.remove(); //
                if (death != null) {
                    Object tracked;
                    synchronized (liveInstances) {
                        tracked = liveInstances.remove(death);
                    }
                    if (tracked != null) {
                        updateDeadStatistics(delegateTypes, tracked);
                    } else
                        log("Missed an already death " + delegateTypes.name());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private void log(String msg) {
        //
        // HACK ALERT
        //
        // java.util.Logger uses a shutdowwnhook, so when we call
        // log (indirectly) from within 'our' shutdownhook then we
        // see nothing :(
        //
        if (logger.getHandlers().length == 0)
            System.err.println(msg);
        else
            logger.log(Level.INFO, msg);
    }


    /**
     * Register the instance with the tracker
     * <p/>
     *
     * @param w
     */
    public void register(Wrapper<?> w) {
        registerQueue.add(w);
    }

    protected void processRegisterQueue() {
        while (true) {
            try {
                process(registerQueue.take());
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    /**
     * Register the instance with the tracker
     * <p/>
     *
     * @param w
     */
    public void process(Wrapper<?> w) {

        // Queue for this type
        ReferenceQueue<Wrapper> q = queueMap.get(w.getType());
        Map<Reference, Object> refs = liveInstancesMap.get(w.getType());
        //
        synchronized (refs) {
            Object o = w.getOriginal();
            // Track the wrapped instance by using a WeakReference for the wrapper itself
            refs.put(new ComparableWeakReference<Wrapper>(w, q), o);
            // Record some allocation related information
            updateAllocations(w);
        }
    }


    public double getAvergeContentSize(Map<Reference, Collection> instances) {
        return getAverageCollectionContentSize(instances.values());
    }

    public long getSmallCollectionCount(DelegateTypes delegateTypes) {
        Map liveInstances = liveInstancesMap.get(delegateTypes);
        synchronized (liveInstances) {
            if (delegateTypes == DelegateTypes.HASHMAP || delegateTypes == DelegateTypes.LINKEDHASHMAP
                    || delegateTypes == DelegateTypes.HASHTABLE
                    || delegateTypes == DelegateTypes.CONCURRENTHASHMAP
                    )
                return getSmallMapCount(liveInstances.values());

            return getSmallCollectionCount(liveInstances.values());
        }
    }

    public long getSmallCollectionCount(Collection<?> instances) {
        long count = 0;
        //
        for (Object o : instances) {
            Collection collection = (Collection) o;
            if (collection.size() < threshold)
                count++;
        }
        //
        return count;
    }

    public long getSmallMapCount(Collection<?> instances) {
        long count = 0;
        //
        for (Object o : instances) {
            Map map = (Map) o;
            if (map.size() < threshold)
                count++;
        }
        //
        return count;
    }


    public double getAverageCollectionContentSize(Collection<?> instances) {
        long sum = 0;
        long count = 0;
        //
        for (Object o : instances) {
            Collection collection = (Collection) o;
            count++;
            sum += collection.size();
        }
        //
        return sum * 1.0 / count;
    }

    public double getAverageMapContentSize(Collection<?> instances) {
        long sum = 0;
        long count = 0;
        //
        for (Object o : instances) {
            Map map = (Map) o;
            count++;
            sum += map.size();
        }
        //
        return sum * 1.0 / count;
    }

    public double getAliveAvergeCapacity(DelegateTypes delegateTypes) {
        long sum = 0;
        long count = 0;
        //
        for (Object o : liveInstancesMap.get(delegateTypes).values()) {
            count++;
            sum += capacity(delegateTypes, o);
        }
        //
        return sum * 1.0 / count;
    }


    public long getAliveCount(DelegateTypes delegateTypes) {
        return liveInstancesMap.get(delegateTypes).size();
    }

    public void dumpAliveAllocationInfo(DelegateTypes delegateTypes) {
        dumpLiveAllocationInfo(liveInstancesMap.get(delegateTypes));
    }

    protected void dumpAllAllocationInfo(DelegateTypes delegateTypes) {
        dumpInfo(allocationsMap.get(delegateTypes));
    }


    public Map<Location, Info> groupLive(Map<Reference, ?> instances) {
        List<Wrapper> wrappers = new LinkedList<Wrapper>();
        // Collect
        for (Map.Entry<Reference, ?> referenceCollectionEntry : instances.entrySet()) {
            Wrapper wrapper = (Wrapper) referenceCollectionEntry.getKey().get();
            if (wrapper != null) { // can have died in the meanwhile
                wrappers.add(wrapper);
            }
        }

        //
        Map<Location, Info> grouped = new TreeMap<Location, Info>();
        for (Wrapper wrapper : wrappers) {
            Location creation = wrapper.getCreation();
            Info info = grouped.get(creation);
            if (info == null) {
                info = new Info(creation);
                grouped.put(creation, info);
            }
            //
            info.count++;
            info.total += wrapper.size();
        }

        //
        return grouped;
    }

    public void dumpLiveAllocationInfo(Map<Reference, ?> instances) {
        dumpInfo(groupLive(instances));
    }

    private void dumpInfo(DelegateTypes delegateTypes) {
        dumpInfo(allocationsMap.get(delegateTypes));
    }

    private void dumpInfo(Map<Location, Info> grouped) {
        List<Info> sorted = new LinkedList<Info>(grouped.values());

        Collections.sort(sorted);
        //
        int sum = 0;
        for (Info info : sorted) {
            sum += info.count;
        }
        for (Info info : sorted) {
            if (info.count > 0) {
                log(String.format("%.1f%% of instances ", (100.0 * info.count) / sum) + "count:" + info.count + " avg. sz:" + info.total * 1.0 / info.count + " created at :\n"
                        + info.location);
            }
        }
        log("Number of entries: " + grouped.size());
    }

    /**
     * Record the allocations per Location count.
     *
     * @param wrapper
     */
    void updateAllocations(Wrapper wrapper) {
        Map<Location, Info> allAllocations = allocationsMap.get(wrapper.getType());
        synchronized (allAllocations) {
            Location creation = wrapper.getCreation();
            Info info = allAllocations.get(creation);
            if (info == null) {
                info = new Info(creation);
                allAllocations.put(creation, info);
            }
            //
            info.count++;
        }
    }


    private int size(Wrapper wrapper) {
        return size(wrapper.getType(), wrapper.getOriginal());
    }

    private int size(DelegateTypes delegateTypes, Object original) {
        switch (delegateTypes) {
            case VECTOR:
            case LINKEDLIST:
            case ARRAYLIST:
                return ((List) original).size();

            case HASHSET:
            case LINKEDHASHSET:
                return ((Set) original).size();

            default:
                return ((Map) original).size();
        }
    }

    private long capacity(DelegateTypes delegateTypes, Object original) {
        switch (delegateTypes) {
            case HASHTABLE:
                return DHashtable.getCapacity((Hashtable) original);

            case VECTOR:
                return DVector.getCapacity((Vector) original);

            case LINKEDLIST:
                return ((LinkedList) original).size();

            case ARRAYLIST:
                return DArrayList.getCapacity((ArrayList) original);

            case HASHSET:
            case LINKEDHASHSET:
                return DHashSet.getCapacity((HashSet) original);

            case CONCURRENTHASHMAP:
                return -1;

            default:
                return DHashMap.getCapacity((HashMap) original);
        }
    }

    public void reset() {
        for (DelegateTypes delegateTypes : DelegateTypes.values()) {
            liveInstancesMap.get(delegateTypes).clear();
            allocationsMap.get(delegateTypes).clear();
        }
    }

    public void dumpAllocationInfoByOwner(DelegateTypes delegateTypes, State state) {
        if (state == State.DEAD) {
            dumpAllAllocationInfoByOwner(delegateTypes);
            return;
        }

        dumpAliveAllocationInfoByOwner(delegateTypes);
    }

    public void dumpAllocationInfo(DelegateTypes delegateTypes, State state) {
        if (state == State.DEAD) {
            dumpAllAllocationInfo(delegateTypes);
            return;
        }

        dumpAliveAllocationInfo(delegateTypes);
    }

    protected void dumpAliveAllocationInfoByOwner(DelegateTypes delegateTypes) {
        dumpByOwner(groupByOwner(groupLive(liveInstancesMap.get(delegateTypes))), delegateTypes);
    }

    protected void dumpAllAllocationInfoByOwner(DelegateTypes delegateTypes) {
        dumpByOwner(groupByOwner(allocationsMap.get(delegateTypes)), delegateTypes);
    }

    private void dumpByOwner(Map<String, Info> byOwner, DelegateTypes delegateTypes) {
        List<Map.Entry<String, Info>> all = new LinkedList<Map.Entry<String, Info>>(byOwner.entrySet());
        Collections.sort(all, new Comparator<Map.Entry<String, Info>>() {
            public int compare(Map.Entry<String, Info> o1, Map.Entry<String, Info> o2) {
                return o2.getValue().count - o1.getValue().count;
            }
        });
        //
        int sum = 0;
        for (Map.Entry<String, Info> e : all) {
            sum += e.getValue().count;
        }

        log("-------- By allocation site -- " + delegateTypes.simpleName + " ----------");
        float cumulate = 0.0f;
        int count = 0;
        for (Map.Entry<String, Info> e : all) {
            Info info = e.getValue();
            count++;
            float percentage = (100.0f * info.count) / sum;
            log(String.format("%d %.1f%% of instances ", count, percentage) + e.getKey() + "count:" + info.count + " avg. sz:" + info.total * 1.0 / info.count + " created at :\n"
                    + e.getKey());
            //
            cumulate += percentage;
            if (cumulate > 80.f) {
                log("Covering more than " + String.format("%.1f%%", cumulate) + " of all instances with + " +
                        String.format("%.1f%%", (100.f * count / all.size())) + " of the sites , rest (" + (all.size() - count) + " of " + all.size() + " not shown!)");
                break;
            }
        }
        log("Number of entries: " + all.size());
    }

//    protected Map<String, Info> groupByOwner(DelegateTypes delegateTypes) {
//        return groupByOwner(allocationsMap.get(delegateTypes));
//    }

    protected Map<String, Info> groupByOwner(Map<Location, Info> allocs) {
        Map<String, Info> byOwner = new TreeMap<String, Info>();
        for (Map.Entry<Location, Info> entry : allocs.entrySet()) {
            Location l = entry.getKey();
            String owner = l.getOwner().toString();
            Info info = byOwner.get(owner);
            if (info == null) {
                info = new Info(l);
                byOwner.put(owner, info);
            }
            Info tmp = entry.getValue();
            info.count += tmp.count;
            info.total += tmp.total;
        }
        //
        return byOwner;
    }


    /**
     * Update dead instance statistics
     *
     * @param delegateTypes
     * @param tracked
     */
    public void updateDeadStatistics(DelegateTypes delegateTypes, Object tracked) {
        Statistics statistics = deadStatistics.get(delegateTypes);
        //
        long capacity = -1;
        long size = -1;
        switch (delegateTypes) {
            case HASHTABLE:
                Hashtable ht = (Hashtable) tracked;
                size = ht.size();
                capacity = DHashtable.getCapacity(ht);
                break;

            case LINKEDLIST:
                LinkedList ll = (LinkedList) tracked;
                size = ll.size();
                capacity = size;
                break;

            case ARRAYLIST:
                ArrayList al = (ArrayList) tracked;
                if (computeCapacity)
                    capacity = DArrayList.getCapacity(al);
                size = al.size();
                break;

            case VECTOR:
                Vector v = (Vector) tracked;
                if (computeCapacity)
                    capacity = DVector.getCapacity(v);
                size = v.size();
                break;

            case HASHMAP:
            case LINKEDHASHMAP:
                HashMap m = (HashMap) tracked;
                if (computeCapacity)
                    capacity = DHashMap.getCapacity(m);
                size = m.size();
                break;

            case CONCURRENTHASHMAP:
                ConcurrentHashMap chm = (ConcurrentHashMap) tracked;
                if (computeCapacity)
                    capacity = -1;
                size = chm.size();
                break;

            case HASHSET:
            case LINKEDHASHSET:
                HashSet s = (HashSet) tracked;
                if (computeCapacity)
                    capacity = DHashSet.getCapacity(s);
                size = s.size();
                break;

            default:
                throw new IllegalStateException(delegateTypes.name());
        }
        //
        update(capacity, size, statistics);
    }

    public void update(long capacity, long sz, Statistics s) {
//            AtomicLong allocations, AtomicLong totalSize, AtomicLong totalContentSize, AtomicLong largestSize, AtomicLong smallInstanceCount) {
        //
        if (sz < threshold)
            s.smallInstances++;

        s.count++;
        if (capacity == 0)
            return;

        s.totalSize += capacity;
        s.totalCapacity += sz;
        //
        s.largestSize = Math.max(sz, s.largestSize);
    }

    private void max(long sz, AtomicLong largestSize) {
        long old;
        long largest;
        do {
            old = largestSize.get();
            if (sz < old)
                return;
            largest = Math.max(old, sz);
        } while (!largestSize.compareAndSet(old, largest));
    }


    public long getLargestSize(DelegateTypes delegateTypes, State state) {
        if (state == State.DEAD)
            return deadStatistics.get(delegateTypes).largestSize;

        return getLargestSizeAlive(delegateTypes);
    }

    public long getLargestSizeAlive(DelegateTypes delegateTypes) {
        Map liveInstances = liveInstancesMap.get(delegateTypes);
        synchronized (liveInstances) {
            if (delegateTypes == DelegateTypes.HASHMAP || delegateTypes == DelegateTypes.LINKEDHASHMAP
                    || delegateTypes == DelegateTypes.HASHTABLE
                    || delegateTypes == DelegateTypes.CONCURRENTHASHMAP
                    )
                return getLargestMapSize(liveInstances.values());

            return getLargestSize(liveInstances.values());
        }
    }

    public long getLargestSize(Collection<?> instances) {
        long max = 0;
        //
        for (Object o : instances) {
            Collection collection = (Collection) o;
            max = Math.max(max, collection.size());
        }
        //
        return max;
    }

    public long getLargestMapSize(Collection<?> instances) {
        long max = 0;
        //
        for (Object o : instances) {
            Map map = (Map) o;
            max = Math.max(max, map.size());
        }
        //
        return max;
    }

    public long getCount(DelegateTypes delegateTypes) {
        return deadStatistics.get(delegateTypes).count;
    }

    public long getTotalContentSize(DelegateTypes delegateTypes) {
        return deadStatistics.get(delegateTypes).totalSize;
    }

    public long getTotalSize(DelegateTypes delegateTypes) {
        return deadStatistics.get(delegateTypes).totalCapacity;
    }

    public long getCount(DelegateTypes delegateTypes, State state) {
        if (state == State.DEAD)
            return deadStatistics.get(delegateTypes).count;

        return getAliveCount(delegateTypes);
    }

    public long getSmallCount(DelegateTypes delegateTypes, State state) {
        if (state == State.DEAD)
            return deadStatistics.get(delegateTypes).smallInstances;

        return getSmallCollectionCount(delegateTypes);
    }

    public double getAverageSize(DelegateTypes delegateTypes, State state) {
        if (state == State.DEAD) {
            Statistics statistics = deadStatistics.get(delegateTypes);
            return (statistics.totalSize * 1.0) / statistics.count;
        }

        return getAliveAvergeCapacity(delegateTypes);
    }

    public double getAverageCapacity(DelegateTypes delegateTypes, State state) {
        if (state == State.DEAD) {
            Statistics statistics = deadStatistics.get(delegateTypes);
            return (statistics.totalCapacity * 1.0) / statistics.count;
        }

        return getAliveAvergeCapacity(delegateTypes);
    }

    public long getSmallCollectionContentSize(DelegateTypes delegateTypes, State state) {
        if (state == State.DEAD) {
            return deadStatistics.get(delegateTypes).smallInstances;
        }

        return getSmallCollectionCount(delegateTypes);
    }

    public String getSizeDistribution(DelegateTypes type, State state) {
        long small = getSmallCount(type, state);
        long count = getCount(type, state);
        return small + "/" + (count - small) + percentage(small, count) + " (SMALL/NOT SMALL)";
    }

    public String getStateDistribution(DelegateTypes type) {
        long alive = getCount(type, ALIVE);
        long dead = getCount(type, DEAD);
        return alive + "/" + dead + percentageParts(alive, dead) + " (ALIVE/DEAD)";
    }


    public String getRatio(DelegateTypes delegateTypes, State state) {
        return percentage(getSmallCount(delegateTypes, state), getCount(delegateTypes, state));
    }

    private String percentageParts(long a, long b) {
        long sum = a + b;
        return String.format(" - %.1f%%/%.1f%% ", (a * 100.0) / sum, (b * 100.0) / sum);
    }

    private String percentage(long smallCount, long count) {
        double perc = (smallCount * 100.0) / count;
        return String.format(" - %.1f%%/%.1f%% ", perc, 100.0 - perc);
    }

    @Override
    public String toString() {
        return "CollectionTracker{" +
                "deadStatistics=" + deadStatistics +
                ", queueMap=" + queueMap +
                ", allocationsMap=" + allocationsMap +
                ", liveInstancesMap=" + liveInstancesMap +
                '}';
    }

    public String getLargestSizeDistribution(DelegateTypes type) {
        long alive = getLargestSize(type, ALIVE);
        long dead = getLargestSize(type, DEAD);
        return alive + "/" + dead + " (ALIVE/DEAD)";
    }

    static class ComparableWeakReference<T> extends WeakReference<T> implements Comparable<ComparableWeakReference<T>> {
        final int id = System.identityHashCode(this);

        ComparableWeakReference(T referent) {
            super(referent);
        }

        ComparableWeakReference(T referent, ReferenceQueue<? super T> q) {
            super(referent, q);
        }

        public int compareTo(ComparableWeakReference<T> o) {
            int d = o.id - id;
            return d > 0 ? 1 : d < 0 ? -1 : 0;
        }
    }
}
