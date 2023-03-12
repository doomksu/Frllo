package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.gov.pfr.service.ConnectionService;
import ru.gov.pfr.service.LoggingService;

public class ReadDoublesInSnilsListTest {

    public ReadDoublesInSnilsListTest() {
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
    public void testReadDoubles() throws Exception {
        File file = new File("D:\\temp\\frllo_14_01.csv");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf8"));
        String doubleFileName = "doubles_" + file.getName();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(doubleFileName), "Cp1251"));
        String string = "";
        String queryTemplate = "insert into NATA.UNIQUE_NPERS values ";

        ArrayList<String> queryes = new ArrayList<>();
        int count = 0;
        while ((string = reader.readLine()) != null) {
            String[] parts = string.split(";");
            if (parts[0].matches("\\d{3}-\\d{3}-\\d{3}\\s\\d{2}")) {
                parts[0] = parts[0].replace("\"", "");
                queryes.add(queryTemplate + "('" + parts[0] + "')");
                count++;
            }
            if (queryes.size() == 10000) {
                ConnectionService.getInstance().executeInsertUpdateQuery(queryes);
                queryes.clear();
            }
        }
        if (queryes.isEmpty() == false) {
            ConnectionService.getInstance().executeInsertUpdateQuery(queryes);
        }
        reader.close();
        writer.close();
    }
}
