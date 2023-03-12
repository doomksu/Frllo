package service;

import ru.gov.pfr.service.LoggingService;
import ru.gov.pfr.service.ConnectionService;
import ru.gov.pfr.service.PrintQueryRequest;
import java.io.File;
import org.junit.Test;
import static ru.gov.pfr.service.FRLLOSQLFactory.DB_DATE_END_TEG;
import static ru.gov.pfr.service.FRLLOSQLFactory.DB_DATE_START_TEG;

public class PrintQueryRequestTest {

    public PrintQueryRequestTest() {
    }

    @Test
    public void testReciveData() throws Exception {
        PrintQueryRequest pqr = new PrintQueryRequest();
//        File file = new File("sql\\npers_closed.sql");
        File file = new File("sql\\select_person.sql");
        String query = ConnectionService.getInstance().queryFromFile(file);
        LoggingService.writeLog(">>select_person.sql", "debug");
//        query = pasteNpers(query);
//        LoggingService.writeLog(query, "debug");
        pqr.makeRequest(query);

//        PrintQueryRequest pqr = new PrintQueryRequest();
////        File file = new File("sql\\npers_closed.sql");
//        File file = new File("sql\\demo13.sql");
//        String query = ConnectionService.getInstance().queryFromFile(file);
//        LoggingService.writeLog(">>demo13", "debug");
////        query = pasteNpers(query);
////        LoggingService.writeLog(query, "debug");
//        pqr.makeRequest(query);
//        file = new File("sql\\npers_closed.sql");
//        query = ConnectionService.getInstance().queryFromFile(file);
//        LoggingService.writeLog(">> npers_closed", "debug");
//        query = pasteNpers(query);
////        LoggingService.writeLog(query, "debug");
//        pqr.makeRequest(query);
//        
//        
//        
//        file = new File("sql\\npers_open.sql");
//        query = ConnectionService.getInstance().queryFromFile(file);
//        LoggingService.writeLog(">> npers_open", "debug");
//        query = pasteNpers(query);
////        LoggingService.writeLog(query, "debug");
//        pqr.makeRequest(query);
    }

    public String pasteNpers(String query) {
        String npers = convertSnils();
        query = query.replace("<MAN_NPERS>", npers)
                .replace(DB_DATE_START_TEG, "2021-05-26")
                .replace(DB_DATE_END_TEG, "2021-05-26");
        return query;
    }

    public String convertSnils() {
        String[] npers = {"00100408443",
            "00100410632",};
//            "00101238348",
//            "00101275556",
//            "00101861064",
//            "00102798694",
//            "00102956383",
//            "00103390162",
//            "00103637675",
//            "00103696691",
//            "00103704563",
//            "00103828278",
//            "00103830669",
//            "00104947594",
//            "00106609988",
//            "00106762289",
//            "00106787002",
//            "00107742086",
//            "00108457395",
//            "00108701683",
//            "00109172283",
//            "00109507793",
//            "00109560191",
//            "00109892822",
//            "00110215539",
//            "00110257555",
//            "00110371048",
//            "00110492969",
//            "00110653159",
//            "00110726059",
//            "00110764673",
//            "00112025342",
//            "00112656377",
//            "00113055760",
//            "00113369480",
//            "00113451768",
//            "00113452366",
//            "00113628579",
//            "00113741676",
//            "00113746080",
//            "00113942484",
//            "00113951687",
//            "00113959300",
//            "00114202954",
//            "00114217664",
//            "00114308970",
//            "00114604469",
//            "00115175073",
//            "00115233768",
//            "00115372479",
//            "00115374382",
//            "00115656190",
//            "00115700571",
//            "00115702373",
//            "00115873400",
//            "00116184988"};
        String template = "";
        boolean first = true;
        for (String nper : npers) {
            if (!first) {
                template += ",";
            }
            template += "'" + formatSnils(nper) + "'";
            first = false;
        }
        return template;
    }

    public String formatSnils(String value) {
        if (value.length() == 11) {
            String res = value.substring(0, 3)
                    + "-"
                    + value.substring(3, 6)
                    + "-"
                    + value.substring(6, 9)
                    + " "
                    + value.substring(9);
            return res;
        } else {
//            LoggingService.writeLog("wrongSnils: " + value, "debug");
        }
        return value;
    }
}
