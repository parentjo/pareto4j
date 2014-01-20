import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gebruiker
 * Date: 23/12/13
 * Time: 20:58
 * To change this template use File | Settings | File Templates.
 */
public class MyObject {
    List l = new ArrayList();
    ArrayList al = new ArrayList();

    public ArrayList copy() {
        return al;
    }

    public void use(boolean b) {
        if (b) {
            List aList = new ArrayList();
        } else {
            ArrayList bList = new ArrayList();
        }
    }

    public void usesArrayList() {
        ArrayList l = new ArrayList();
        l.addAll(al);
    }

    public void usesList() {
        List l = new ArrayList();
        l.addAll(al);
    }

    public List usesArrayListReturnsList() {
        return new ArrayList();
    }

    public ArrayList usesArrayListReturnsArrayList() {
        return new ArrayList();
    }
}
