package org.pareto4j.mousetrap;

import java.lang.instrument.Instrumentation;

/**
 * Created with IntelliJ IDEA.
 * User: jparent
 * Date: 13/08/13
 * Time: 16:21
 * To change this template use File | Settings | File Templates.
 */
public class TrapAgent {
    public static void premain(String agentArguments, Instrumentation instrumentation) {
        System.out.println("Loading TrapAgent... ");
        System.out.println("Built " + TrapAgent.class.getPackage().getImplementationVersion());
        instrumentation.addTransformer(new TrapTransformer());
        System.out.println("TrapAgent configured");
    }
}
