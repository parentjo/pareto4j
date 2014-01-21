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

import javax.management.*;
import java.lang.management.ManagementFactory;

import static org.pareto4j.inspector.collections.DelegateTypes.*;
import static org.pareto4j.inspector.collections.State.*;

/**
 * Created with IntelliJ IDEA.
 * User: jparent
 * Date: 13/08/13
 * Time: 10:37
 * To change this template use File | Settings | File Templates.
 */
public class CollectionsData implements CollectionsDataMBean {
    private static long id = 0;
    /**
     * The use of the static final field CollectionsData.tracker will
     * still result in a NEW tracker for each classloader
     */
    private final CollectionTracker tracker = CollectionTracker.COLLECTION_TRACKER;
    public static final CollectionsData COLLECTIONS_DATA = new CollectionsData();


    private CollectionsData() {
        try {
            registerMBean();
        } catch (MalformedObjectNameException e) {
            logError(e);
        } catch (NotCompliantMBeanException e) {
            logError(e);
        } catch (MBeanRegistrationException e) {
            logError(e);
        } catch (InstanceAlreadyExistsException e) {
            logError(e);
        }
    }

    public static void register(Wrapper<?> w) {
        COLLECTIONS_DATA.tracker.register(w);
    }

    private void registerMBean() throws MBeanRegistrationException, NotCompliantMBeanException, MalformedObjectNameException, InstanceAlreadyExistsException {
        registerMBean(this, "Pareto4j:Module=Inspector,name=Details", 5);
        registerMBean(new ParetoStatisticsMBean(), "Pareto4j:Module=Inspector,name=Overview", 5);
    }

    private void registerMBean(Object mbean, String name, long attempts) throws MBeanRegistrationException, NotCompliantMBeanException, MalformedObjectNameException, InstanceAlreadyExistsException {
        try {
            registerMBeanRaw(mbean, name);
        } catch (InstanceAlreadyExistsException e) {
            while (attempts-- > 0) {
                InstanceAlreadyExistsException lastException;
                try {
                    registerMBeanRaw( mbean, name + '-' + (id++));
                    return;
                } catch (InstanceAlreadyExistsException e1) {
                    e1.printStackTrace();
                    lastException = e1;
                }

                throw lastException;
            }
        }
    }


    private void registerMBeanRaw(Object mbean, String name) throws MBeanRegistrationException, NotCompliantMBeanException, MalformedObjectNameException, InstanceAlreadyExistsException {
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        mbs.registerMBean(mbean, new ObjectName(name));
    }

    void logError(final Exception exc) {
        exc.printStackTrace();
    }


    public void reset() {
        tracker.reset();
    }

    public long get1_SmallSizeThreshold() {
        return CollectionTracker.threshold;
    }

    public String get1_SmallSizeThresholdMeaning() {
        return "Any data structure with size() <" + CollectionTracker.threshold + " is considered \"small\".";
    }

    public String get1_SmallListRatio_ALIVE() {
        return tracker.getRatio(ARRAYLIST, ALIVE);
    }

    public String get1_SmallListRatio_DEAD() {
        return tracker.getRatio(ARRAYLIST, DEAD);
    }

    public String get1_SmallHashMapRatio_ALIVE() {
        return tracker.getRatio(HASHMAP, ALIVE);
    }

    public String get1_SmallHashMapRatio_DEAD() {
        return tracker.getRatio(HASHMAP, DEAD);
    }

    public String get1_SmallLinkedHashMapRatio_ALIVE() {
        return tracker.getRatio(LINKEDHASHMAP, ALIVE);
    }

    public String get1_SmallLinkedHashMapRatio_DEAD() {
        return tracker.getRatio(LINKEDHASHMAP, DEAD);
    }

    public String get1_SmallHashSetRatio_ALIVE() {
        return tracker.getRatio(HASHSET, ALIVE);
    }

    public String get1_SmallHashSetRatio_DEAD() {
        return tracker.getRatio(HASHSET, DEAD);
    }

    public String get1_SmallLinkedHashSetRatio_ALIVE() {
        return tracker.getRatio(LINKEDHASHSET, ALIVE);
    }

    public String get1_SmallLinkedHashSetRatio_DEAD() {
        return tracker.getRatio(LINKEDHASHSET, DEAD);
    }

    public String get1_ListStateDistribution() {
        return getStateDistribution(ARRAYLIST);
    }

    public String get1_ListSizeDistributionAlive() {
        return getSizeDistribution(ARRAYLIST, ALIVE);
    }

    public String get1_ListSizeDistributionDead() {
        return getSizeDistribution(ARRAYLIST, DEAD);
    }

    private String getSizeDistribution(DelegateTypes type, State state) {
        return tracker.getSmallCount(type, state) + '/' + tracker.getCount(type, state) + " (SMALL/NOT SMALL)";
    }

    public String getStateDistribution(DelegateTypes type) {
        return tracker.getCount(type, ALIVE) + '/' + tracker.getCount(type, DEAD) + " (ALIVE/DEAD)";
    }


    // List
    public long getListCount_Dead() {
        return tracker.getCount(ARRAYLIST, DEAD);
    }

    public double getListAverageSize_Dead() {
        return tracker.getAverageSize(ARRAYLIST, DEAD);
    }

    public double getListAverageCapacity_Dead() {
        return tracker.getAverageCapacity(ARRAYLIST, DEAD);
    }

    public long getListSmallCount_Dead() {
        return tracker.getSmallCollectionContentSize(ARRAYLIST, DEAD);
    }

    public long getListCount_Alive() {
        return tracker.getCount(ARRAYLIST, ALIVE);
    }

    public double getListAverageSize_Alive() {
        return tracker.getAverageCapacity(ARRAYLIST, ALIVE);
    }

    public double getListAverageCapacity_Alive() {
        return tracker.getAverageCapacity(ARRAYLIST, ALIVE);
    }

    public long getListSmallCount_Alive() {
        return tracker.getSmallCollectionContentSize(ARRAYLIST, ALIVE);
    }

    // HashSet
    public long getHashSetCount_Dead() {
        return tracker.getCount(HASHSET, DEAD);
    }

    public double getHashSetAverageSize_Dead() {
        return tracker.getAverageSize(HASHSET, DEAD);
    }

    public double getHashSetAverageCapacity_Dead() {
        return tracker.getAverageCapacity(HASHSET, DEAD);
    }

    public long getHashSetSmallCount_Dead() {
        return tracker.getSmallCollectionContentSize(HASHSET, DEAD);
    }

    public long getHashSetCount_Alive() {
        return tracker.getCount(HASHSET, ALIVE);
    }

    public double getHashSetAverageSize_Alive() {
        return tracker.getAverageCapacity(HASHSET, ALIVE);
    }

    public double getHashSetAverageCapacity_Alive() {
        return tracker.getAverageCapacity(HASHSET, ALIVE);
    }

    public long getHashSetSmallCount_Alive() {
        return tracker.getSmallCollectionContentSize(HASHSET, ALIVE);
    }

    // LinkedHashSet
    public long getLinkedHashSetCount_Dead() {
        return tracker.getCount(LINKEDHASHSET, DEAD);
    }

    public double getLinkedHashSetAverageSize_Dead() {
        return tracker.getAverageSize(LINKEDHASHSET, DEAD);
    }

    public double getLinkedHashSetAverageCapacity_Dead() {
        return tracker.getAverageCapacity(LINKEDHASHSET, DEAD);
    }

    public long getLinkedHashSetSmallCount_Dead() {
        return tracker.getSmallCollectionContentSize(LINKEDHASHSET, DEAD);
    }

    public long getLinkedHashSetCount_Alive() {
        return tracker.getCount(LINKEDHASHSET, ALIVE);
    }

    public double getLinkedHashSetAverageSize_Alive() {
        return tracker.getAverageCapacity(LINKEDHASHSET, ALIVE);
    }

    public double getLinkedHashSetAverageCapacity_Alive() {
        return tracker.getAverageCapacity(LINKEDHASHSET, ALIVE);
    }

    public long getLinkedHashSetSmallCount_Alive() {
        return tracker.getSmallCollectionContentSize(LINKEDHASHSET, ALIVE);
    }

    // HashMap
    public long getHashMapCount_Dead() {
        return tracker.getCount(HASHMAP, DEAD);
    }

    public double getHashMapAverageSize_Dead() {
        return tracker.getAverageSize(HASHMAP, DEAD);
    }

    public double getHashMapAverageCapacity_Dead() {
        return tracker.getAverageCapacity(HASHMAP, DEAD);
    }

    public long getHashMapSmallCount_Dead() {
        return tracker.getSmallCollectionContentSize(HASHMAP, DEAD);
    }

    public long getHashMapCount_Alive() {
        return tracker.getCount(HASHMAP, ALIVE);
    }

    public double getHashMapAverageSize_Alive() {
        return tracker.getAverageCapacity(HASHMAP, ALIVE);
    }

    public double getHashMapAverageCapacity_Alive() {
        return tracker.getAverageCapacity(HASHMAP, ALIVE);
    }

    public long getHashMapSmallCount_Alive() {
        return tracker.getSmallCollectionContentSize(HASHMAP, ALIVE);
    }

    // LinkedHashMap
    public long getLinkedHashMapCount_Dead() {
        return tracker.getCount(LINKEDHASHMAP, DEAD);
    }

    public double getLinkedHashMapAverageSize_Dead() {
        return tracker.getAverageSize(LINKEDHASHMAP, DEAD);
    }

    public double getLinkedHashMapAverageCapacity_Dead() {
        return tracker.getAverageCapacity(LINKEDHASHMAP, DEAD);
    }

    public long getLinkedHashMapSmallCount_Dead() {
        return tracker.getSmallCollectionContentSize(LINKEDHASHMAP, DEAD);
    }

    public long getLinkedHashMapCount_Alive() {
        return tracker.getCount(LINKEDHASHMAP, ALIVE);
    }

    public double getLinkedHashMapAverageSize_Alive() {
        return tracker.getAverageCapacity(LINKEDHASHMAP, ALIVE);
    }

    public double getLinkedHashMapAverageCapacity_Alive() {
        return tracker.getAverageCapacity(LINKEDHASHMAP, ALIVE);
    }

    public long getLinkedHashMapSmallCount_Alive() {
        return tracker.getSmallCollectionContentSize(LINKEDHASHMAP, ALIVE);
    }

    public void dumpListAllAllocationInfo() {
        tracker.dumpAllocationInfo(ARRAYLIST, DEAD);
    }

    public void dumpListAliveAllocationInfo() {
        tracker.dumpAllocationInfo(ARRAYLIST, ALIVE);
    }

    public void dumpListAllAllocationInfoByOwner() {
        tracker.dumpAllocationInfoByOwner(ARRAYLIST, DEAD);
    }

    public void dumpListAliveAllocationInfoByOwner() {
        tracker.dumpAllocationInfoByOwner(ARRAYLIST, ALIVE);
    }

    public void dumpHashSetAllAllocationInfo() {
        tracker.dumpAllocationInfo(HASHSET, DEAD);
    }

    public void dumpHashSetAliveAllocationInfo() {
        tracker.dumpAllocationInfo(HASHSET, ALIVE);
    }

    public void dumpHashSetAllAllocationInfoByOwner() {
        tracker.dumpAllocationInfoByOwner(HASHSET, DEAD);
    }

    public void dumpHashSetAliveAllocationInfoByOwner() {
        tracker.dumpAllocationInfoByOwner(HASHSET, ALIVE);
    }

    public void dumpLinkedHashSetAllAllocationInfo() {
        tracker.dumpAllocationInfo(LINKEDHASHSET, DEAD);
    }

    public void dumpLinkedHashSetAliveAllocationInfo() {
        tracker.dumpAllocationInfo(LINKEDHASHSET, ALIVE);
    }

    public void dumpLinkedHashSetAllAllocationInfoByOwner() {
        tracker.dumpAllocationInfoByOwner(LINKEDHASHSET, DEAD);
    }

    public void dumpLinkedHashSetAliveAllocationInfoByOwner() {
        tracker.dumpAllocationInfoByOwner(LINKEDHASHSET, ALIVE);
    }

    public void dumpHashMapAllAllocationInfo() {
        tracker.dumpAllocationInfo(HASHMAP, DEAD);
    }

    public void dumpHashMapAliveAllocationInfo() {
        tracker.dumpAllocationInfo(HASHMAP, ALIVE);
    }

    public void dumpHashMapAllAllocationInfoByOwner() {
        tracker.dumpAllocationInfoByOwner(HASHMAP, DEAD);
    }

    public void dumpHashMapAliveAllocationInfoByOwner() {
        tracker.dumpAllocationInfoByOwner(HASHMAP, ALIVE);
    }

    public void dumpLinkedHashMapAllAllocationInfo() {
        tracker.dumpAllocationInfo(LINKEDHASHMAP, DEAD);
    }

    public void dumpLinkedHashMapAliveAllocationInfo() {
        tracker.dumpAllocationInfo(LINKEDHASHMAP, ALIVE);
    }

    public void dumpLinkedHashMapAllAllocationInfoByOwner() {
        tracker.dumpAllocationInfoByOwner(LINKEDHASHMAP, DEAD);
    }

    public void dumpLinkedHashMapAliveAllocationInfoByOwner() {
        tracker.dumpAllocationInfoByOwner(LINKEDHASHMAP, ALIVE);
    }



    // - HELPERS ---------------------------

    private double getDeadAverage(DelegateTypes delegateTypes) {
        return tracker.getTotalSize(delegateTypes) * 1.0 / tracker.getCount(delegateTypes);
    }

    private double getDeadAverageContent(DelegateTypes delegateTypes) {
        return tracker.getTotalContentSize(delegateTypes) * 1.0 / tracker.getCount(delegateTypes);
    }
}
