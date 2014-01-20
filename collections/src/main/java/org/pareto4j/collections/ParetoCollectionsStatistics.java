package org.pareto4j.collections;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created with IntelliJ IDEA.
 * User: jparent
 * Date: 2/05/13
 * Time: 13:37
 * To change this template use File | Settings | File Templates.
 */
public class ParetoCollectionsStatistics implements ParetoCollectionsStatisticsMBean {
    final AtomicLong paretoListTotal = new AtomicLong(0);
    final AtomicLong paretoListExpands = new AtomicLong(0);
    final AtomicLong paretoListEmpty = new AtomicLong(0);
    final AtomicLong paretoListOne = new AtomicLong(0);
    final AtomicLong paretoListGeneral = new AtomicLong(0);

    final AtomicLong paretoMapTotal = new AtomicLong(0);
    final AtomicLong paretoMapExpands = new AtomicLong(0);
    final AtomicLong paretoMapEmpty = new AtomicLong(0);
    final AtomicLong paretoMapOne = new AtomicLong(0);
    final AtomicLong paretoMapGeneral = new AtomicLong(0);

    final AtomicLong paretoSetTotal = new AtomicLong(0);
    final AtomicLong paretoSetExpands = new AtomicLong(0);
    final AtomicLong paretoSetEmpty = new AtomicLong(0);
    final AtomicLong paretoSetOne = new AtomicLong(0);
    final AtomicLong paretoSetGeneral = new AtomicLong(0);

    public static final ParetoCollectionsStatistics instance = new ParetoCollectionsStatistics();
    private static int id = 0;

    private ParetoCollectionsStatistics() {
        try {
            registerMBean(this, "Pareto:Module=Collections,name=Statistics", 10);
        } catch (MBeanRegistrationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (NotCompliantMBeanException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InstanceAlreadyExistsException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void registerMBean(Object mbean, String name, long attempts) throws MBeanRegistrationException, NotCompliantMBeanException, MalformedObjectNameException, InstanceAlreadyExistsException {
        try {
            registerMBeanRaw(mbean, name);
        } catch (InstanceAlreadyExistsException e) {
            while (attempts-- > 0) {
                InstanceAlreadyExistsException lastException;
                try {
                    registerMBeanRaw(mbean, name + "-" + (id++));
                    return;
                } catch (InstanceAlreadyExistsException e1) {
                    e1.printStackTrace();
                    lastException = e1;
                }

                throw lastException;
            }
        }
    }


    private void registerMBeanRaw(Object mbean, String name) throws MBeanRegistrationException, NotCompliantMBeanException, MalformedObjectNameException, InstanceAlreadyExistsException {
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        mbs.registerMBean(mbean, new ObjectName(name));
    }

    public long getListTotal() {
        return paretoListTotal.get();
    }

    public long getListExpands() {
        return paretoListExpands.get();
    }

    public long getMapTotal() {
        return paretoMapTotal.get();
    }

    public long getMapExpands() {
        return paretoMapExpands.get();
    }

    public long getListEmpty() {
        return paretoListEmpty.get();
    }

    public long getListOne() {
        return paretoListOne.get();
    }

    public long getListGeneral() {
        return paretoListGeneral.get();
    }

    public long getMapEmpty() {
        return paretoMapEmpty.get();
    }

    public long getMapOne() {
        return paretoMapOne.get();
    }

    public long getMapGeneral() {
        return paretoMapGeneral.get();
    }

    public long getSetTotal() {
        return paretoSetTotal.get();
    }

    public long getSetEmpty() {
        return paretoSetEmpty.get();
    }

    public long getSetOne() {
        return paretoSetOne.get();
    }

    public long getSetGeneral() {
        return paretoSetGeneral.get();
    }

    public String getListPercentage() {
        long z = paretoListEmpty.get();
        long o = paretoListOne.get();
        long g = paretoListGeneral.get();
        long t = paretoListTotal.get();
        return percentages(z, o, g, t);
    }

    public String getMapPercentage() {
        long z = paretoMapEmpty.get();
        long o = paretoMapOne.get();
        long g = paretoMapGeneral.get();
        long t = paretoMapTotal.get();
        return percentages(z, o, g, t);
    }

    public String getSetPercentage() {
        long z = paretoSetEmpty.get();
        long o = paretoSetOne.get();
        long g = paretoSetGeneral.get();
        long t = paretoSetTotal.get();
        return percentages(z, o, g, t);
    }

    private String percentages(long z, long o, long g, long t) {
        return p(z, t) + '/' + p(o, t) + '/' + p(g, t) + " (Nothing/One/General)";
    }

    String p(long value, long ref) {
        return percentage(value, ref);
    }

    String percentage(long value, long ref) {
        return percentage((ref == 0 ? 0.0 : ((100.0 * value) / ref)));
    }

    private String percentage(double v) {
        return String.format("%.1f%%", v);
    }
}
