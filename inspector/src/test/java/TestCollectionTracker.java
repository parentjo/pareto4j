
import org.pareto4j.inspector.collections.CollectionsData;
import org.pareto4j.inspector.collections.DArrayList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: jparent
 * Date: 13/08/13
 * Time: 13:46
 * To change this template use File | Settings | File Templates.
 */
public class TestCollectionTracker {

    public static void main(String[] args) {
        final Collection all = new LinkedList();
        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    all.add(make());
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    all.clear();
                    System.gc();
                }
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    System.out.println(CollectionsData.COLLECTIONS_DATA);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
        }).start();

    }


    static Random random = new Random(123456L);

    private static ArrayList make() {
        int sz = random.nextInt(1024);
        ArrayList r = (ArrayList) new DArrayList();
        ArrayList other =  new DArrayList(new AL());
        AL al = new AL(r);
        //
        while (sz > 0) {
            r.add(new Object());
            other.isEmpty();
            al.isEmpty();
            sz--;
        }

        return r;
    }

    static class AL extends ArrayList {
        AL(int initialCapacity) {
            super(initialCapacity);
        }

        AL() {
        }

        AL(Collection c) {
            super(c);
        }
    }
}
