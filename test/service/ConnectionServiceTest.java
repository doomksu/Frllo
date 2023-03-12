package service;

import ru.gov.pfr.service.LoggingService;
import ru.gov.pfr.service.FRLLOSQLFactory;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static ru.gov.pfr.service.ConnectionService.resultToMap;

public class ConnectionServiceTest {

    public ConnectionServiceTest() {
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
    public void testMakeTemplates() throws Exception {
        Connection connectionFBDP = FRLLOSQLFactory.createFBDPConnection();
        Statement st = connectionFBDP.createStatement();

//        String query = "select * from(\n"
//                + "\n"
//                + "(select man.id, man.changedate from changes.man man where man.npers = '021-287-428 26'\n"
//                + "and man.changedate = (select max(mann.changedate) from changes.man mann where mann.id = man.id)\n"
//                + ")man\n"
//                + "\n"
//                + "inner join \n"
//                + "\n"
//                + "(select * from changes.gsp gsp\n"
//                + "	\n"
//                + ")gsp \n"
//                + "\n"
//                + "on gsp.id = man.id\n"
//                + "and gsp.changedate = man.changedate\n"
//                + ")";
        String query = "select * from(\n"
                + "\n"
                + "(select man.id, man.changedate from changes.man man where man.npers = '021-287-428 26'\n"
                + "and man.changedate = (select max(mann.changedate) from changes.man mann where mann.id = man.id)\n"
                + ")man\n"
                + "\n"
                + "inner join \n"
                + "\n"
                + "(select * from changes.rasgsp rasgsp\n"
                + "	\n"
                + ")rasgsp \n"
                + "\n"
                + "on rasgsp.id = man.id\n"
                + "and rasgsp.changedate = man.changedate\n"
                + "\n"
                + ")";

        ResultSet result = st.executeQuery(query);
        while (result.next()) {
            LinkedHashMap<String, String> m = resultToMap(result);
            for (String key : m.keySet()) {
                LoggingService.writeLogNoTypeNoTime(key + " : " + m.get(key));
            }
        }
        st.close();
        connectionFBDP.close();
    }

}
