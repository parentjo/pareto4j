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

import java.util.EnumSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: gebruiker
 * Date: 3/12/13
 * Time: 20:36
 * To change this template use File | Settings | File Templates.
 */
public class Profile {
    static final Set<DelegateTypes> modify = EnumSet.noneOf(DelegateTypes.class);
    static final boolean fullStacktrace = System.getProperty("fullStacktrace") != null;
    static {
        for (DelegateTypes delegateTypes : DelegateTypes.values()) {
            if (System.getProperty("skip"+delegateTypes.name()) == null) {
                modify.add(delegateTypes);
                System.err.println("Will inspect " + delegateTypes);
            }
        }
    }

}