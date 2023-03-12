package service;

import ru.gov.pfr.service.LoggingService;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MustBeRecordsTest {

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
    public void readFolderTest() throws Exception {
        HashMap<String, Integer> records = new HashMap<>();
        File folder = new File("D:\\temp\\frllo_count_documents");
        for (File file : folder.listFiles()) {
            if (file.isFile()) {
                String string = "";
                int count = 0;
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "cp1251"));
                while ((string = reader.readLine()) != null) {
                    if (string.contains("</document>")) {
                        count++;
                    }
                }
                records.put(file.getName(), count);
                reader.close();
            }
        }
        for (String key : records.keySet()) {
            LoggingService.writeLog(key + "; " + records.get(key), "debug");
        }
    }
}
