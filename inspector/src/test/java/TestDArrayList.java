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

import com.google.common.collect.testing.ListTestSuiteBuilder;
import com.google.common.collect.testing.TestStringListGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.ListFeature;
import junit.framework.TestSuite;
import org.pareto4j.inspector.collections.DArrayList;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jparent
 * Date: 12/12/13
 * Time: 11:50
 * To change this template use File | Settings | File Templates.
 */

public class TestDArrayList {
    public static TestSuite suite() throws Exception {
        return ListTestSuiteBuilder.using(new TestStringListGenerator() {
            public List<String> create(String[] objects) {
                DArrayList l = new DArrayList();
                for (Object object : objects) {
                    l.add(object);
                }
                return l;
            }
        }).named("DArrayListTest").withFeatures(CollectionSize.ANY, ListFeature.GENERAL_PURPOSE,
                ListFeature.REMOVE_OPERATIONS,  CollectionFeature.ALLOWS_NULL_VALUES,
                CollectionFeature.KNOWN_ORDER, CollectionFeature.ALLOWS_NULL_QUERIES
        )
                .createTestSuite();
    }

//    public static void main(String[] args) {
//        testDArrayListTestSuite().r
//    }
}
