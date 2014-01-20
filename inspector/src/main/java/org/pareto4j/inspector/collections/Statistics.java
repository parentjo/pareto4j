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
 * Statistics about dead instances. Life instance statistics are computed some place else
 * <p/>
 * Created with IntelliJ IDEA.
 * User: gebruiker
 * Date: 22/11/13
 * Time: 20:01
 * To change this template use File | Settings | File Templates.
 */
public class Statistics {
    public long largestSize = 0;
    public long count = 0;
    public long totalCapacity = 0;
    public long totalSize = 0;
    public long smallInstances = 0;

    public void reset() {
        largestSize = 0;
        count = 0;
        totalCapacity = 0;
        totalSize = 0;
        smallInstances = 0;
    }

}
