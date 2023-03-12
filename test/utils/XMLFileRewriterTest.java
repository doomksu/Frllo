package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.gov.pfr.service.LoggingService;

public class XMLFileRewriterTest {

    public XMLFileRewriterTest() {
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

    @Test
    public void rewriteFile() throws Exception {
        File file = new File("C:\\projects\\FRLLO\\out\\monitization-9c25c63c-902d-46ea-8c80-7f679be22cc3.xml");
        File rewriteFolder = new File("D:\\temp\\rewriteInvalidMonetization");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));

        String string = "";
        int lineNumber = 0;

        while ((string = reader.readLine()) != null) {
            lineNumber++;
            LoggingService.writeLog(string, "debug");
            if (lineNumber > 520) {
                break;
            }
        }
        reader.close();
    }
}
