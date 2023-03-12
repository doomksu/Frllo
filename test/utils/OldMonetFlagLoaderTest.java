package utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.gov.pfr.service.ConnectionService;
import ru.gov.pfr.service.SettingsService;

public class OldMonetFlagLoaderTest {

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
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("monitizationSnilsList.csv"), "cp1251"));
        ArrayList<String> queryes = new ArrayList<>();
        String string = "";
        String query = "insert into NATA.OLDMONETIZATION (NPERS) values ('";
        int buffSize = Integer.parseInt(SettingsService.getInstance().getValue("BUFFER_SIZE"));
        int lines = 0;
        System.out.println("Start NSU ");
        while ((string = reader.readLine()) != null) {
            String request = query + string.replace("\"", "") + "')";
            queryes.add(request);
            if (queryes.size() >= buffSize) {
                System.out.println("Send oldmonet records: " + buffSize);
                    ConnectionService.getInstance().executeInsertUpdateQuery(queryes);
                    queryes.clear();
            }

            lines++;
            if (lines % 10000 == 0) {
                System.out.println("oldmonet readed: " + lines);
            }
        }

        if (queryes.isEmpty() == false) {
            ConnectionService.getInstance().executeInsertUpdateQuery(queryes);
        }
    }
}
