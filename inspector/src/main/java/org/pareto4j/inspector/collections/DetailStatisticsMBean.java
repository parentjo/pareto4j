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
import java.util.LinkedList;
import java.util.List;

/**
 * Created by gebruiker on 1/02/14.
 */
public class DetailStatisticsMBean implements DynamicMBean {
    /**
     * The use of the static final field CollectionsData.tracker will
     * still result in a NEW tracker for each classloader
     */
    public final CollectionTracker tracker = CollectionTracker.COLLECTION_TRACKER;

    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        String[] parts = attribute.split("\\.");
        DelegateTypes type = DelegateTypes.valueOf(parts[0].toUpperCase());
        Attrib attrib = Attrib.valueOf(parts[1]);
        State state = State.valueOf(parts[2]);

        switch (attrib) {
            case AVERAGE:
                return tracker.getAverageSize(type, state);

            case COUNT:
                return tracker.getCount(type, state);

            case LARGEST:
                return tracker.getLargestSize(type, state);

            case SMALL:
                return tracker.getSmallCount(type, state);
        }
        return null;
    }

    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {

    }

    public AttributeList getAttributes(String[] attributes) {
        AttributeList ret = new AttributeList();
        //
        for (String attribute : attributes) {
            try {
                ret.add(new Attribute(attribute, getAttribute(attribute)));
            } catch (AttributeNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (MBeanException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ReflectionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return ret;
    }

    public AttributeList setAttributes(AttributeList attributes) {
        return null;
    }

    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
        return null;
    }

    public MBeanInfo getMBeanInfo() {
        List<MBeanAttributeInfo> attributeInfoList = new LinkedList<MBeanAttributeInfo>();
        //
        for (DelegateTypes delegateTypes : Profile.modify) {
            createAttributes(attributeInfoList, delegateTypes);
        }

        List<MBeanOperationInfo> operationInfoList = new LinkedList<MBeanOperationInfo>();


        return new MBeanInfo(
                "MyMBEANINFO",
                "MyDesc",
                attributeInfoList.toArray(new MBeanAttributeInfo[attributeInfoList.size()]),
                null,
                operationInfoList.toArray(new MBeanOperationInfo[operationInfoList.size()]),
                null
        );
    }

    private void createAttributes(List<MBeanAttributeInfo> attributeInfoList, DelegateTypes delegateTypes) {
        //
        for (Attrib attrib : Attrib.values()) {
            for (State state : State.values()) {
                attributeInfoList.add(
                        new MBeanAttributeInfo(delegateTypes.simpleName + '.' + attrib.name() + '.' + state.name(),
                                attrib.type, "description", true, false, false)
                );
            }
        }
    }

    private static enum Attrib {
        AVERAGE("java.lang.double"), LARGEST("java.lang.long"), SMALL("java.lang.long"), COUNT("java.lang.long");

        public final String type;

        Attrib(String type) {
            this.type = type;
        }
    }
}
