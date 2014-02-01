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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
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
    private Map<DelegateTypes, Holder> liveInstancesMap =
            new EnumMap<DelegateTypes, Holder>(DelegateTypes.class);

    final BlockingQueue<Wrapper> registerQueue = new LinkedBlockingQueue<Wrapper>();

    final NumberFormat nf = NumberFormat.getInstance();

    public CollectionTracker() {
        Handler[] handlers = logger.getHandlers();
        if (handlers != null && handlers.length == 0) {
            try {
                FileHandler fh = new FileHandler("pareto4j_log.%u.%g.txt", 50 * 1024 * 1024, 10, false);
                fh.setFormatter(new SimpleFormatter());
                logger.addHandler(fh);

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        setup();
    }

    private void setup() {
        //
        for (final DelegateTypes delegateTypes : DelegateTypes.values()) {
            queueMap.put(delegateTypes, new ReferenceQueue<Wrapper>());
            liveInstancesMap.put(delegateTypes, new Holder(delegateTypes.name()));
            allocationsMap.put(delegateTypes, new TreeMap<Location, Info>());
            deadStatistics.put(delegateTypes, new Statistics());
            start(new Runnable() {
                public void run() {
                    try {
                        collectDeadInstances(delegateTypes, queueMap.get(delegateTypes), liveInstancesMap.get(delegateTypes));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
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
    private void collectDeadInstances(DelegateTypes delegateTypes, ReferenceQueue q, Holder liveInstances) throws InterruptedException {
        while (true) {
            ComparableWeakReference death = (ComparableWeakReference) q.remove(); //
            if (death != null) {
                Object tracked;
                synchronized (liveInstances) {
                    tracked = liveInstances.remove(death);
                }
                updateDeadStatistics(delegateTypes, tracked, death.location);
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
        Holder refs = liveInstancesMap.get(w.getType());
        //
        synchronized (refs) {
            // Track the wrapped instance by using a WeakReference for the wrapper itself
            refs.add(new ComparableWeakReference(w, q));
            // Record some allocation related information
            updateAllocations(w);
        }
    }

    public long getAliveSmallCount(DelegateTypes delegateTypes) {
        Holder liveInstances = liveInstancesMap.get(delegateTypes);
        synchronized (liveInstances) {
            SizeHelper sizeHelper = getSizeHelper(delegateTypes);

            return getSmallCount(liveInstances.values(), sizeHelper);
        }
    }

    private SizeHelper getSizeHelper(DelegateTypes delegateTypes) {
        return isMap(delegateTypes) ? mapSizer : collectionSizer;
    }

    private boolean isMap(DelegateTypes delegateTypes) {
        return delegateTypes == DelegateTypes.HASHMAP || delegateTypes == DelegateTypes.LINKEDHASHMAP
                || delegateTypes == DelegateTypes.HASHTABLE
                || delegateTypes == DelegateTypes.CONCURRENTHASHMAP;
    }

    public long getSmallCount(Iterable<ComparableWeakReference> instances, SizeHelper sizeHelper) {
        long count = 0;
        //
        for (ComparableWeakReference r : instances) {
            if (sizeHelper.size(r.o) < threshold)
                count++;
        }
        //
        return count;
    }

    public double getAverageContentSize(DelegateTypes delegateTypes) {
        Holder liveInstances = liveInstancesMap.get(delegateTypes);
        synchronized (liveInstances) {
            SizeHelper sizeHelper = getSizeHelper(delegateTypes);

            return getAverageContentSize(liveInstances.values(), sizeHelper);
        }
    }

    public double getAverageContentSize(Iterable<ComparableWeakReference> instances, SizeHelper sizeHelper) {
        long sum = 0;
        long count = 0;
        //
        for (ComparableWeakReference r : instances) {
            count++;
            sum += sizeHelper.size(r.o);
        }
        //
        return sum * 1.0 / count;
    }

    public double getStdDevContentSize(DelegateTypes delegateTypes) {
        return getStdDevContentSize(delegateTypes, getAverageContentSize(delegateTypes));
    }

    public double getStdDevContentSize(DelegateTypes delegateTypes, double avg) {
        Holder liveInstances = liveInstancesMap.get(delegateTypes);
        synchronized (liveInstances) {
            SizeHelper sizeHelper = getSizeHelper(delegateTypes);

            return getStdDevContentSize(liveInstances.values(), avg, sizeHelper);
        }
    }

    public double getStdDevContentSize(Iterable<ComparableWeakReference> instances, double avg, SizeHelper sizeHelper) {
        double sum = 0;
        long count = 0;
        //
        for (ComparableWeakReference r : instances) {
            count++;
            int sz = sizeHelper.size(r.o);
            double d = avg - sz;
            sum += d * d;
        }
        //
        return Math.sqrt(sum / count);
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
        dumpInfo(merge(groupLive(liveInstancesMap.get(delegateTypes)), allocationsMap.get(delegateTypes)));
    }


    public Map<Location, Info> merge(Map<Location, Info> a, Map<Location, Info> b) {
        // First a
        for (Map.Entry<Location, Info> entry : a.entrySet()) {
            Info aInfo = entry.getValue();
            Info bInfo = b.get(entry.getKey());
            //
            merge(aInfo, bInfo);
        }

        // Look for b entries not in a
        for (Map.Entry<Location, Info> entry : b.entrySet()) {
            Info aInfo = b.get(entry.getKey());
            if (aInfo == null) {
                a.put(entry.getKey(), entry.getValue());
            }
        }

        return a;
    }

    static void merge(Info res, Info a) {
        res.merge(a);
    }

    public Map<Location, Info> groupLive(Holder instances) {
        Map<Location, Info> grouped = new TreeMap<Location, Info>();
        // Collect
        for (ComparableWeakReference referenceCollectionEntry : instances) {
            Wrapper wrapper = referenceCollectionEntry.get();
            if (wrapper != null) { // can have died in the meanwhile
                Info info = getOrCreateInfo(wrapper, grouped);
                //
                info.add(wrapper.size());
            }
        }

        //
        return grouped;
    }

    public void dumpLiveAllocationInfo(Holder instances) {
        dumpInfo(groupLive(instances));
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
                log(String.format("%.1f%% of instances ", (100.0 * info.count) / sum) + " count:" + nf.format(info.count) + " avg. sz:"
                        + " " + nf.format(info.totalSize * 1.0 / info.count) + " created at :\n"
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
        Info info = getOrCreateInfo(wrapper, allAllocations);
        //
        info.count++;
    }

    private Info getOrCreateInfo(Wrapper wrapper, Map<Location, Info> all) {
        Location creation = wrapper.getCreation();
        Info info = all.get(creation);
        if (info == null) {
            info = new Info(creation);
            all.put(creation, info);
        }
        return info;
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
                return (int) (o2.getValue().count - o1.getValue().count);
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
            double avg = info.totalSize * 1.0 / info.count;
            log(String.format("%d %.1f%% of instances ", count, percentage) + e.getKey() + " count:" + nf.format(info.count)
                    + " avg. sz:" + nf.format(avg) + (avg < 2.0 ? " (-!-)" : "")
                    + " created at :\n"
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
            info.totalSize += tmp.totalSize;
        }
        //
        return byOwner;
    }


    /**
     * Update dead instance statistics
     *
     * @param delegateTypes
     * @param tracked
     * @param location
     */
    public void updateDeadStatistics(DelegateTypes delegateTypes, Object tracked, Location location) {
        Statistics statistics = deadStatistics.get(delegateTypes);
        Map<Location, Info> a = allocationsMap.get(delegateTypes);
        //
        SizeHelper sizeHelper = getSizeHelper(delegateTypes);

        long capacity = -1;
        long size = sizeHelper.size(tracked);

/*
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
*/
        //
        update(capacity, size, statistics);
        a.get(location).add(size);
    }

    public void update(long capacity, long sz, Statistics s) {
        //
        if (sz < threshold)
            s.smallInstances++;

        s.add(sz);
        s.totalCapacity += capacity;
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
        Holder liveInstances = liveInstancesMap.get(delegateTypes);
        synchronized (liveInstances) {
            SizeHelper sizeHelper = getSizeHelper(delegateTypes);
            return getLargestSize(liveInstances.values(), sizeHelper);
        }
    }

    public long getLargestSize(Iterable<ComparableWeakReference> instances, SizeHelper sizeHelper) {
        long max = 0;
        //
        for (ComparableWeakReference o : instances) {
            max = Math.max(max, sizeHelper.size(o.o));
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

        return getAliveSmallCount(delegateTypes);
    }

    private double getDeadAverage(DelegateTypes delegateTypes) {
        return getTotalSize(delegateTypes) * 1.0 / getCount(delegateTypes);
    }

    private double getDeadAverageContent(DelegateTypes delegateTypes) {
        return getTotalContentSize(delegateTypes) * 1.0 / getCount(delegateTypes);
    }


    public double getAverageSize(DelegateTypes delegateTypes, State state) {
        if (state == State.DEAD) {
            Statistics statistics = deadStatistics.get(delegateTypes);
            return statistics.averageSize;
        }

        return getAverageContentSize(delegateTypes);
    }

    public double getAverageCapacity(DelegateTypes delegateTypes, State state) {
        if (state == State.DEAD) {
            Statistics statistics = deadStatistics.get(delegateTypes);
            return (statistics.totalCapacity * 1.0) / statistics.count;
        }

        return getAliveAvergeCapacity(delegateTypes);
    }

    public long getSmallCollectionContentSize(DelegateTypes delegateTypes, State state) {
        return getSmallCount(delegateTypes, state);
    }

    public String getSizeDistribution(DelegateTypes type, State state) {
        long small = getSmallCount(type, state);
        long count = getCount(type, state);
        return format(small) + "/" + format(count - small) + percentage(small, count) + " (SMALL/NOT SMALL)";
    }

    public String getStateDistribution(DelegateTypes type) {
        long alive = getCount(type, ALIVE);
        long dead = getCount(type, DEAD);
        return format(alive) + "/" + format(dead) + percentageParts(alive, dead) + " (ALIVE/DEAD)";
    }

    public String getAverageDistribution(DelegateTypes type) {
        double alive = getAverageSize(type, ALIVE);
        double dead = getAverageSize(type, DEAD);
        return String.format("%.2f+-%.2f/%.2f+-%.2f (ALIVE/DEAD)", alive, getStdDevContentSize(type), dead, getDeadStdDevContentSize(type));
    }

    private double getDeadStdDevContentSize(DelegateTypes type) {
        return Math.sqrt(deadStatistics.get(type).varianceSize);
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
        return format(alive) + "/" + format(dead) + " (ALIVE/DEAD)";
    }

    private String format(double dead) {
        return nf.format(dead);
    }

    private String format(long dead) {
        return nf.format(dead);
    }

    /**
     * Stores the Location and the tracked object reference for faster access
     * later on.
     * <p/>
     * Use 2 refs to impl. a double linked list
     */
    static class ComparableWeakReference extends WeakReference<Wrapper> {
        ComparableWeakReference left;  // prev and next are used by Reference
        ComparableWeakReference right; // so other names
        final Location location;
        //
        Object o;

        ComparableWeakReference(Wrapper referent) {
            super(referent);
            location = referent.getCreation();
        }

        ComparableWeakReference(Wrapper referent, ReferenceQueue<? super Wrapper> q) {
            super(referent, q);
            location = referent.getCreation();
            this.o = referent.getOriginal();
        }

        void remove() {
            ComparableWeakReference l;
            l = left;
            if (l != null) {
                left.right = right;
            }
            //
            l = right;
            if (l != null) {
                right.left = left;
            }

            // Null-ing refs is supposedly not helping GC
            // but just in case
            left = null;
            right = null;
        }

        @Override
        public String toString() {
            return "ComparableWeakReference{" +
                    "left=" + left +
                    ", right=" + right +
                    ", location=" + location +
                    ", o=" + o +
                    '}';
        }
    }

    /**
     * Basic structure to hold the first link of the double linked list. Provides a few
     * functionalities add() and remove()
     */
    static class Holder implements Iterable<ComparableWeakReference> {
        ComparableWeakReference head;
        final String name;
        int count = 0;

        public Holder(String name) {
            this.name = name;
        }

        public void add(ComparableWeakReference c) {
            if (head != null) {
                // Link in front of head
                head.left = c;
                c.right = head;
            }

            head = c;
            count++;
        }

        public Object remove(ComparableWeakReference c) {
            ComparableWeakReference newHead = null;
            if (head == c) {
                newHead = c.right;
            }

            c.remove();

            if (newHead != null)
                head = newHead;

            count--;

            return c.o;
        }

        public Iterator<ComparableWeakReference> iterator() {
            final ComparableWeakReference tmp = head;

            return new Iterator<ComparableWeakReference>() {
                ComparableWeakReference l = tmp;

                public boolean hasNext() {
                    return l != null && l.right != null;
                }

                public ComparableWeakReference next() {
                    ComparableWeakReference e = l;
                    l = l.right;
                    return e;
                }

                public void remove() {
                }
            };
        }

        Iterable values() {
            return this;
        }

        public int size() {
            return count;
        }

        public void clear() {
            count = 0;
            head = null;
        }

        @Override
        public String toString() {
            return "Holder{" +
                    "head=" + head +
                    ", name='" + name + '\'' +
                    ", count=" + count +
                    '}';
        }
    }

    static interface SizeHelper {
        int size(Object o);

    }

    static final SizeHelper mapSizer = new SizeHelper() {
        public int size(Object o) {
            return ((Map) o).size();
        }
    };

    static final SizeHelper collectionSizer = new SizeHelper() {
        public int size(Object o) {
            return ((Collection) o).size();
        }
    };

}
