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
 * Date: 27/08/13
 * Time: 10:06
 * To change this template use File | Settings | File Templates.
 */
public class Info extends Statistics implements Comparable<Info> {

    final Location location;

    StackTraceElement owner = null;


    Info(Location location) {
        this.location = location;
    }

    public StackTraceElement getOwner() {
        if (owner == null)
            owner = location.getOwner();

        return owner;
    }

    protected Object getLocationInformation() {
        if (location != null)
            return location;

        return owner;
    }

    public int compareTo(Info o) {
        long d = count - o.count;
        return d == 0 ? 0 : (d >= 1 ? 1 : -1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Info info = (Info) o;

        if (totalSize != info.totalSize) return false;
        if (count != info.count) return false;
        if (getLocationInformation() != null ? !getLocationInformation().equals(info.getLocationInformation()) : info.getLocationInformation() != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getLocationInformation() != null ? getLocationInformation().hashCode() : 0;
        result = (int) (31 * result + count);
        result = (int) (31 * result + totalSize);
        return result;
    }

    @Override
    public String toString() {
        return "Info{" +
                "location=" + getLocationInformation() +
                ", count=" + count +
                ", totalSize=" + totalSize +
                '}';
    }
}