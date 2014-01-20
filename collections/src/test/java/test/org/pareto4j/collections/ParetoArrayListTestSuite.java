package test.org.pareto4j.collections;

import com.google.common.collect.testing.ListTestSuiteBuilder;
import com.google.common.collect.testing.TestStringListGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.ListFeature;
import junit.framework.TestSuite;
import org.pareto4j.collections.ParetoArrayList;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jparent
 * Date: 13/12/13
 * Time: 10:24
 * To change this template use File | Settings | File Templates.
 */
public class ParetoArrayListTestSuite {
    public static TestSuite suite() throws Exception {
        return ListTestSuiteBuilder.using(new TestStringListGenerator() {
            public List<String> create(String[] objects) {
                ParetoArrayList l = new ParetoArrayList();
                for (Object object : objects) {
                    l.add(object);
                }
                return l;
            }
        })
                .named("LateListTest").withFeatures(CollectionSize.ANY, ListFeature.GENERAL_PURPOSE,
                        ListFeature.REMOVE_OPERATIONS, CollectionFeature.ALLOWS_NULL_VALUES,
                        CollectionFeature.KNOWN_ORDER, CollectionFeature.ALLOWS_NULL_QUERIES
                )
                .createTestSuite();

    }
}
