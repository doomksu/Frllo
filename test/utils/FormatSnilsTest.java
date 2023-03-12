package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import org.junit.Test;
import ru.gov.pfr.service.LoggingService;

public class FormatSnilsTest {

    @Test
    public void testReplace() throws Exception {
        String[] patterns = new String[]{"20942835368",};
        File snilsFile = new File("D:\\temp\\snils_to_format.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(snilsFile), "utf8"));
        String string = "";
        while ((string = reader.readLine()) != null) {
            LoggingService.writeLog(formatSnils(string), "debug");
        }
//        for (String pattern : patterns) {
//            LoggingService.writeLog(formatSnils(pattern), "debug");
//        }
    }

    private String formatSnils(String value) {
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
