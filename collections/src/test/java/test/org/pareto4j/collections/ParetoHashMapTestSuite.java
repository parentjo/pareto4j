package test.org.pareto4j.collections;

import com.google.common.collect.testing.MapTestSuiteBuilder;
import com.google.common.collect.testing.TestStringMapGenerator;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.MapFeature;
import junit.framework.TestSuite;
import org.pareto4j.collections.ParetoHashMap;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jparent
 * Date: 13/12/13
 * Time: 10:34
 * To change this template use File | Settings | File Templates.
 */
public class ParetoHashMapTestSuite {
    public static TestSuite suite() throws Exception {
        return MapTestSuiteBuilder.using(new TestStringMapGenerator() {
            @Override
            protected Map<String, String> create(Map.Entry<String, String>[] entries) {
                ParetoHashMap m = new ParetoHashMap();
                //
                for (Map.Entry<String, String> entry : entries) {
                    if (entry == null)
                        m.put(null, null);
                    else
                        m.put(entry.getKey(), entry.getValue());
                }
                return m;
            }
        }
        ).named("ParetoHashMap"
        ).withFeatures(MapFeature.GENERAL_PURPOSE, CollectionSize.ANY, MapFeature.ALLOWS_NULL_VALUES, MapFeature.ALLOWS_NULL_KEYS
        ).createTestSuite();
    }
}
