package writer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.gov.pfr.service.LoggingService;

public class ErrorLinesParserTest {

    public ErrorLinesParserTest() {
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
    public void testParse() throws Exception {
        LoggingService.getInstance().recreateLog();
        LoggingService.getInstance().switchTimer(false);
        LoggingService.getInstance().switchLogType(false);
        HashMap<String, BufferedWriter> writers = new HashMap<>();

        File file = new File("D:\\temp\\frllo load from 01.02.21\\errorLines\\01.02 14.34.37_error_Lines.csv");
        String fPart = file.getName().substring(0, file.getName().indexOf("_"));
        BufferedReader is = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String line;
        HashMap<String, HashSet<String>> map = new HashMap<>();
        while ((line = is.readLine()) != null) {
            String add = "";
            String parts[] = line.split(";");
            String key = parts[0];
            String[] keyParts = key.split(":");
            key = keyParts[0].trim();

            if (map.containsKey(key) == false) {
                map.put(key, new HashSet<>());
                String fileName = "разбор\\" + fPart + "\\" + key + ".csv";
                File f = new File(fileName);
                f.mkdirs();

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "Cp1251"));
                writers.put(key, writer);
            }

            String val = parts[1].isEmpty() ? parts[2] : parts[1];
            if (keyParts.length > 1) {
                for (int i = 1; i < keyParts.length; i++) {
                    String keyPart = keyParts[i];
                    add += ";" + keyPart.replace(val, "").replace(key, "");
                }
            }
            map.get(key).add(val + add);
        }

        for (String key : map.keySet()) {
            for (String string : map.get(key)) {
                writers.get(key).write(string);
                writers.get(key).newLine();
                writers.get(key).flush();
            }
        }
        for (String key : map.keySet()) {
            writers.get(key).close();
        }

    }
}
