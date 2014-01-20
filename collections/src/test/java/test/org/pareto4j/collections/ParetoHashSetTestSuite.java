package test.org.pareto4j.collections;

import com.google.common.collect.testing.SetTestSuiteBuilder;
import com.google.common.collect.testing.TestStringSetGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.SetFeature;
import junit.framework.TestSuite;
import org.pareto4j.collections.ParetoHashSet;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: jparent
 * Date: 13/12/13
 * Time: 10:34
 * To change this template use File | Settings | File Templates.
 */
public class ParetoHashSetTestSuite {
    public static TestSuite suite() throws Exception {
        return SetTestSuiteBuilder.using(new TestStringSetGenerator() {

            @Override
            protected Set<String> create(String[] elements) {
                ParetoHashSet s = new ParetoHashSet();
                for (String element : elements) {
                    s.add(element);
                }
                return s;
            }
        })
                .named("ParetoHashSetTestSuite")
                .withFeatures(SetFeature.GENERAL_PURPOSE, CollectionSize.ANY, CollectionFeature.ALLOWS_NULL_VALUES,
                        CollectionFeature.ALLOWS_NULL_QUERIES, CollectionFeature.REMOVE_OPERATIONS,
                        CollectionFeature.SERIALIZABLE)
                .createTestSuite();
    }
}
