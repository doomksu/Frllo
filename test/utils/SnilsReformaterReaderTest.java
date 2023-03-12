package utils;

import ru.gov.pfr.utils.XMLValues;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import org.junit.Test;
import ru.gov.pfr.service.LoggingService;

public class SnilsReformaterReaderTest {

    @Test
    public void readSNILSTest() throws Exception {
        File folder = new File("D:\\temp\\check_monetization_npers");
        for (File file : folder.listFiles()) {
//            LoggingService.writeLog("file: " + file.getAbsolutePath(), "debug");
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
            String string = "";
            int lineNumber = 0;
            boolean startLog = false;
            int number = 0;
            while ((string = reader.readLine()) != null) {
                if (string.contains("snils")) {
                    String xmlVal = XMLValues.getValue(string);
                    String formSNILS = formatSnils(xmlVal);
                    LoggingService.writeLogNoTypeNoTime(formSNILS);
                    number++;
                }
//                if (number == 50) {
//                    break;
//                }
            }
            reader.close();
        }
//        File file = new File("D:\\temp\\frllo_validate_files_\\7e7438bb-d26e-4238-b4f0-45fcc9f9f09f.xml");
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
