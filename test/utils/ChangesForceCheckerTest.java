package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import ru.gov.pfr.service.ConnectionService;
import static ru.gov.pfr.service.ConnectionService.resultToMap;

public class ChangesForceCheckerTest {

    @Test
    public void testQueryChecker() throws Exception {
        String querySQL = getQuery();
        Connection connectionFBDP = ConnectionService.getInstance().getConnectionFBDP();
        Statement st = connectionFBDP.createStatement();
        System.out.println(">> execute: " + querySQL);
        ResultSet result = st.executeQuery(querySQL);
        int limit = 100;
        while (result.next() && limit > 0) {
            LinkedHashMap<String, String> m = resultToMap(result);
            limit--;
            for (Map.Entry<String, String> entry : m.entrySet()) {
                System.out.println(entry.getKey() + " : " + entry.getValue());
            }
        }
        st.close();
    }

    private String getQuery() throws Exception {
        String homePath = Paths.get("").toAbsolutePath().toString();
        String pathsql = homePath + "\\sql\\uniqueIDLoads.sql";
        File queryFile = new File(pathsql);
        String queryTemplate = queryFromFile(queryFile);
        if (queryFile.exists()) {
            
        }
        return queryTemplate;
    }

    public String queryFromFile(File file) throws Exception {
        String query = "";
        String string;
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
        while ((string = reader.readLine()) != null) {
            query += string + "\r\n";
        }
        return query;
    }
}
