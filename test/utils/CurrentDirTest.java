package utils;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author User
 */
public class CurrentDirTest {

    public CurrentDirTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void writeCurrentDir() {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));

        String s = Paths.get("").toAbsolutePath().toString();
        System.out.println("Current absolute path is: " + s);
    }
}
