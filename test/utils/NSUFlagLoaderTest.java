package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.gov.pfr.service.ConnectionService;
import ru.gov.pfr.service.LoggingService;
import ru.gov.pfr.service.SettingsService;

public class NSUFlagLoaderTest {

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
    public void loadNSUFlagTest() throws Exception {
        ConnectionService.getInstance();

        File folder = new File("D:\\temp\\nsuLoad");
        long avgTime = 0;
        long startTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis();

        for (File file : folder.listFiles()) {
            LoggingService.writeLog("file strat: " + file.getName(), "debug");

            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "cp1251"));
            ArrayList<String> queryes = new ArrayList<>();
            String string = "";

            String query = "insert into NATA.FRLLO_NSU (NPERS,NSU) values(";
            int buffSize = Integer.parseInt(SettingsService.getInstance().getValue("BUFFER_SIZE"));
            int lines = 0;
            while ((string = reader.readLine()) != null) {
                String[] parts = string.split(";");
                if (parts.length == 2) {
                    try {
                        int nsuFlag = Integer.parseInt(parts[1]);
                        String request = query + "'" + parts[0] + "'," + nsuFlag + ")";
                        queryes.add(request);
                    } catch (Exception ex) {
                    }
                    if (queryes.size() >= buffSize) {
                        startTime = System.currentTimeMillis();
                        ConnectionService.getInstance().executeInsertUpdateQuery(queryes);
                        endTime = System.currentTimeMillis();
                        avgTime = (endTime - startTime) / queryes.size();
                        queryes.clear();
                    }
                }
                lines++;
                if (lines % 20000 == 0) {
                    LoggingService.writeLog("recods readed and sended: " + lines + " avg: " + avgTime + " ms per row", "debug");
                }
            }
            if (queryes.isEmpty() == false) {
                ConnectionService.getInstance().executeInsertUpdateQuery(queryes);
            }
            LoggingService.writeLog("file done: " + file.getName(), "debug");
            reader.close();
            file.delete();
        }
    }
}
