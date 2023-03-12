package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import org.junit.Test;
import ru.gov.pfr.service.LoggingService;

public class PersonsSnilsReaderTest {

    @Test
    public void testCollectSnils() throws Exception {
        HashSet<String> snilsSet = new HashSet<>();

        LoggingService.getInstance();
        File file = new File("C:\\projects\\FRLLO\\frllo_persons.csv");
        if (file.isFile() == false) {
            LoggingService.writeLog("file doesn't exists", "error");
            return;
        }
        LoggingService.writeLog("read file : " + file.getAbsolutePath(), "debug");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "cp1251"));
        String string = "";
        int count = 0;
        while ((string = reader.readLine()) != null) {
            String[] parts = string.split(",");
            if (parts.length >= 5) {
                if (Integer.parseInt(parts[5]) > 0) {
                    snilsSet.add(parts[1]);
                }
            } else {
                LoggingService.writeLog("len: " + parts.length + " " + string, "debug");
            }
            count++;
            if (count % 500000 == 0) {
                LoggingService.writeLog(" readed: " + count, "debug");
            }
        }
        reader.close();
        writeSnils(snilsSet);
    }

    private void writeSnils(HashSet<String> snilsSet) {
        try {
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream("for_monitizationSnilsList.csv"), "cp1251"));
            int i = 0;
            for (String snils : snilsSet) {
                writer.write("\"" + snils + "\"\r\n");
                writer.flush();
                i++;
            }
            writer.close();
        } catch (Exception ex) {
            LoggingService.writeLog(ex);
        }
    }

}
