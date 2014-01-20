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

import java.lang.instrument.Instrumentation;

/**
 * Created with IntelliJ IDEA.
 * User: jparent
 * Date: 13/08/13
 * Time: 16:21
 * To change this template use File | Settings | File Templates.
 */
public class InspectorAgent {
    public static void premain(String agentArguments, Instrumentation instrumentation) {
        System.out.println("Loading InspectorAgent... ");
        System.out.println("Built " + InspectorAgent.class.getPackage().getImplementationVersion());
        instrumentation.addTransformer(new InspectorCollectionTransformer());
        System.out.println("InspectorAgent configured");
    }
}
