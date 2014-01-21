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
 * Created with IntelliJ IDEA.
 * User: jparent
 * Date: 4/12/13
 * Time: 12:07
 * To change this template use File | Settings | File Templates.
 */
public class ParetoStatisticsMBean implements DynamicMBean {
    private enum Attrib {
        StateDistribution("Alive vs dead instance disitribution", State.DEAD),
        SizeDistributionDead("Small vs not small disitribution", State.DEAD),
        SizeDistributionAlive("Small vs not small disitribution", State.ALIVE),
        LargestInstance("Largest instance per type", State.DEAD);

        final String description;
        final State state;

        private Attrib(String description, State state) {
            this.description = description;
            this.state = state;
        }
    }

    private enum Operation {
        DumpByAllocationSiteAll("Alive vs dead instance disitribution", State.DEAD),
        DumpByAllocationSiteAlive("Small vs not small disitribution", State.ALIVE),
        DumpByStacktraceAll("Alive vs dead instance disitribution", State.DEAD),
        DumpByStacktraceAlive("Small vs not small disitribution", State.ALIVE),
        DumpSummary("Dumps all at once", State.DEAD, true);

        final String description;
        final State state;
        final boolean special;

        private Operation(String description, State state) {
            this.description = description;
            this.state = state;
            special = false;
        }

        private Operation(String description, State state, boolean special) {
            this.description = description;
            this.state = state;
            this.special = special;
        }
    }

    Operation[] siteOperations = new Operation[]{
            Operation.DumpByAllocationSiteAlive, Operation.DumpByAllocationSiteAll
    };

    /**
     * The use of the static final field CollectionsData.tracker will
     * still result in a NEW tracker for each classloader
     */
    public final CollectionTracker tracker = CollectionTracker.COLLECTION_TRACKER;

    public ParetoStatisticsMBean() {
        //
        if (System.getProperty("inspector.dumpOnExit") != null) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    System.err.println("Dump information");
                    dumpAllocationSummary(org.pareto4j.inspector.collections.State.DEAD);
                }
            });
        }

    }

    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        DelegateTypes type = null;
        //
        for (DelegateTypes delegateTypes : DelegateTypes.values()) {
            if (attribute.startsWith(delegateTypes.simpleName)) {
                type = delegateTypes;
                break;
            }
        }
        try {
            String attribLabel = attribute.substring(type.simpleName.length());
            Attrib attrib = Attrib.valueOf(attribLabel);
            switch (attrib) {
                case SizeDistributionAlive:
                case SizeDistributionDead:
                    return tracker.getSizeDistribution(type, attrib.state);

                case StateDistribution:
                    return tracker.getStateDistribution(type);

                case LargestInstance:
                    return tracker.getLargestSizeDistribution(type);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }


        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        //To change body of implemented methods use File | Settings | File Templates.
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
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
        DelegateTypes type = null;
        //
        for (DelegateTypes delegateTypes : DelegateTypes.values()) {
            if (actionName.startsWith(delegateTypes.simpleName)) {
                type = delegateTypes;
                break;
            }
        }
        String operationLabel = type != null ? actionName.substring(type.simpleName.length()) : actionName;
        Operation operation = Operation.valueOf(operationLabel);
        switch (operation) {
            case DumpByAllocationSiteAlive:
            case DumpByAllocationSiteAll:
                tracker.dumpAllocationInfoByOwner(type, operation.state);
                break;

            case DumpByStacktraceAlive:
            case DumpByStacktraceAll:
                tracker.dumpAllocationInfo(type, operation.state);
                break;

            case DumpSummary:
                dumpAllocationSummary(operation.state);

        }


        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private void dumpAllocationSummary(State state) {
        for (DelegateTypes delegateTypes : Profile.modify) {
            tracker.dumpAllocationInfoByOwner(delegateTypes, state);
        }
    }

    public MBeanInfo getMBeanInfo() {
        List<MBeanAttributeInfo> attributeInfoList = new LinkedList<MBeanAttributeInfo>();
        //
        for (DelegateTypes delegateTypes : Profile.modify) {
            createAttributes(attributeInfoList, delegateTypes);
        }
        List<MBeanOperationInfo> operationInfoList = new LinkedList<MBeanOperationInfo>();
        // Per type
        for (DelegateTypes delegateTypes : Profile.modify) {
            createOperations(operationInfoList, delegateTypes);
        }
        // Special (not per type)
        createOperations(operationInfoList);

        return new MBeanInfo(
                "MyMBEANINFO",
                "MyDesc",
                attributeInfoList.toArray(new MBeanAttributeInfo[attributeInfoList.size()]),
                null,
                operationInfoList.toArray(new MBeanOperationInfo[operationInfoList.size()]),
                null
        );
    }

    private void createOperations(List<MBeanOperationInfo> operationInfoList, DelegateTypes delegateTypes) {
        Operation[] operations = Profile.fullStacktrace ? Operation.values() : siteOperations;
        //
        for (Operation operation : operations) {
            if (!operation.special) {
                operationInfoList.add(
                        // String name String description, MBeanParameterInfo[] signature, String type,int impact
                        new MBeanOperationInfo(
                                delegateTypes.simpleName + operation.name(), operation.description,
                                new MBeanParameterInfo[0], "java/lang/Void", MBeanOperationInfo.INFO
                        )
                );
            }
        }
    }

    private void createOperations(List<MBeanOperationInfo> operationInfoList) {
        Operation[] operations = Profile.fullStacktrace ? Operation.values() : siteOperations;
        //
        for (Operation operation : operations) {
            if (operation.special) {
                operationInfoList.add(
                        // String name String description, MBeanParameterInfo[] signature, String type,int impact
                        new MBeanOperationInfo(
                                operation.name(), operation.description,
                                new MBeanParameterInfo[0], "java/lang/Void", MBeanOperationInfo.INFO
                        )
                );
            }
        }
    }

    private void createAttributes(List<MBeanAttributeInfo> attributeInfoList, DelegateTypes delegateTypes) {
        //
        for (Attrib attrib : Attrib.values()) {
            attributeInfoList.add(
                    //String name, String type, String description, boolean isReadable, boolean isWritable, boolean isIs
                    new MBeanAttributeInfo(delegateTypes.simpleName + attrib.name(), "java.lang.String", "description", true, false, false)
            );
        }
    }
}
