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

/**
 * Created with IntelliJ IDEA.
 * User: jparent
 * Date: 13/08/13
 * Time: 10:36
 * To change this template use File | Settings | File Templates.
 */
public interface CollectionsDataMBean {
    long get1_SmallSizeThreshold();
    String get1_SmallSizeThresholdMeaning();

    String get1_SmallListRatio_ALIVE();
    String get1_SmallListRatio_DEAD();

    String get1_SmallHashMapRatio_ALIVE();
    String get1_SmallHashMapRatio_DEAD();

    String get1_SmallLinkedHashMapRatio_ALIVE();
    String get1_SmallLinkedHashMapRatio_DEAD();

    String get1_SmallHashSetRatio_ALIVE();
    String get1_SmallHashSetRatio_DEAD();

    String get1_SmallLinkedHashSetRatio_ALIVE();
    String get1_SmallLinkedHashSetRatio_DEAD();

    String get1_ListStateDistribution();
    String get1_ListSizeDistributionDead();
    String get1_ListSizeDistributionAlive();


    //List
    // Dead
    long getListCount_Dead();
    double getListAverageSize_Dead();
    double getListAverageCapacity_Dead();
    long getListSmallCount_Dead();
    // Alive
    long getListCount_Alive();
    double getListAverageSize_Alive();
    double getListAverageCapacity_Alive();
    long getListSmallCount_Alive();

    //
    // Dead
    long getHashSetCount_Dead();
    double getHashSetAverageSize_Dead();
    double getHashSetAverageCapacity_Dead();
    long getHashSetSmallCount_Dead();
    // Alive
    long getHashSetCount_Alive();
    double getHashSetAverageSize_Alive();
    double getHashSetAverageCapacity_Alive();
    long getHashSetSmallCount_Alive();

    //
    // Dead
    long getLinkedHashSetCount_Dead();
    double getLinkedHashSetAverageSize_Dead();
    double getLinkedHashSetAverageCapacity_Dead();
    long getLinkedHashSetSmallCount_Dead();
    // Alive
    long getLinkedHashSetCount_Alive();
    double getLinkedHashSetAverageSize_Alive();
    double getLinkedHashSetAverageCapacity_Alive();
    long getLinkedHashSetSmallCount_Alive();

    //
    // Dead
    long getHashMapCount_Dead();
    double getHashMapAverageSize_Dead();
    double getHashMapAverageCapacity_Dead();
    long getHashMapSmallCount_Dead();
    // Alive
    long getHashMapCount_Alive();
    double getHashMapAverageSize_Alive();
    double getHashMapAverageCapacity_Alive();
    long getHashMapSmallCount_Alive();

    //
    // Dead
    long getLinkedHashMapCount_Dead();
    double getLinkedHashMapAverageSize_Dead();
    double getLinkedHashMapAverageCapacity_Dead();
    long getLinkedHashMapSmallCount_Dead();
    // Alive
    long getLinkedHashMapCount_Alive();
    double getLinkedHashMapAverageSize_Alive();
    double getLinkedHashMapAverageCapacity_Alive();
    long getLinkedHashMapSmallCount_Alive();


    // Operations
    void dumpListAllAllocationInfo();
    void dumpListAliveAllocationInfo();
    void dumpListAllAllocationInfoByOwner();
    void dumpListAliveAllocationInfoByOwner();

    void dumpHashSetAllAllocationInfo();
    void dumpHashSetAliveAllocationInfo();
    void dumpHashSetAllAllocationInfoByOwner();
    void dumpHashSetAliveAllocationInfoByOwner();

    void dumpLinkedHashSetAllAllocationInfo();
    void dumpLinkedHashSetAliveAllocationInfo();
    void dumpLinkedHashSetAllAllocationInfoByOwner();
    void dumpLinkedHashSetAliveAllocationInfoByOwner();

    void dumpHashMapAllAllocationInfo();
    void dumpHashMapAliveAllocationInfo();
    void dumpHashMapAllAllocationInfoByOwner();
    void dumpHashMapAliveAllocationInfoByOwner();

    void dumpLinkedHashMapAllAllocationInfo();
    void dumpLinkedHashMapAliveAllocationInfo();
    void dumpLinkedHashMapAllAllocationInfoByOwner();
    void dumpLinkedHashMapAliveAllocationInfoByOwner();

    void reset();
}
