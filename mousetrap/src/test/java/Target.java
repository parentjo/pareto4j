import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by gebruiker on 26/12/13.
 */
public class Target {
    public FileInputStream read(String s) throws FileNotFoundException {
        File file = new File(s);
        FileInputStream fileInputStream = new FileInputStream(file);

        return fileInputStream;
    }

    public FileInputStream readRethrow(String s) throws FileNotFoundException {
        try {
            File file = new File(s);
            FileInputStream fileInputStream = new FileInputStream(file);

            return fileInputStream;
        } catch (FileNotFoundException e) {
            throw e;
        }
    }
}
