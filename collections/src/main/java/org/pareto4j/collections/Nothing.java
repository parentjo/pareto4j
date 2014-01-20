package org.pareto4j.collections;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Singleton like class used to represent NOTHING, used by Pareto implementations as a dummy value
 * <p/>
 * Impl Externalizable iface to speed things up.
 * <br><br>
 * Created with IntelliJ IDEA.
 * User: jparent
 * Date: 20/06/13
 * Time: 12:09
 */
public final class Nothing implements Externalizable {
    public static final Nothing NOTHING = new Nothing();

    private Nothing() {
    }

    private Object readResolve() {
        return NOTHING;
    }

    /**
     * Nothing logic wanted here, we need to impl this method but readResolve() will
     * map any serialized instance to the singleton instance
     *
     * @param out
     * @throws IOException
     */
    public void writeExternal(ObjectOutput out) throws IOException {
    }

    /**
     * Nothing logic wanted here, we need to impl this method but readResolve() will
     * map any serialized instance to the singleton instance
     *
     * @param in
     * @throws IOException
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    }
}
