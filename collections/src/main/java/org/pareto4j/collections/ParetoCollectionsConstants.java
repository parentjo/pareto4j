package org.pareto4j.collections;

import java.io.Serializable;

/**
 * A few constants used by all the "Pareto" classes. Those are used for the enconding and decoding of instance during
 * serialization
 * <p/>
 * <p></p>
 * Created with IntelliJ IDEA.
 * User: jparent
 * Date: 20/09/13
 * Time: 10:26
 */
public interface ParetoCollectionsConstants {
    final Serializable NOTHING = Nothing.NOTHING;
    final byte NONE = 0;
    final byte ONE = 1;
    final byte GENERAL = 2;
}
