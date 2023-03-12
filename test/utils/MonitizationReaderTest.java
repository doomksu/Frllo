package utils;

import ru.gov.pfr.utils.XMLValues;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.gov.pfr.service.LoggingService;

public class MonitizationReaderTest {

    public MonitizationReaderTest() {
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
    public void testCollectMonitizationSnils() throws Exception {
        HashSet<String> snilsSet = new HashSet<>();
        LoggingService.getInstance();
        File folder = new File("D:\\temp\\monet");
        if (folder.isDirectory() == false) {
            LoggingService.writeLog("not a directory", "error");
            return;
        }
        for (File file : folder.listFiles()) {
            if (file.isFile()) {
                readFile(file, snilsSet);
            }
        }
        writeSnils(snilsSet);
        collectPersons(snilsSet);
    }

    private void readFile(File file, HashSet<String> snilsSet) {
        try {
            LoggingService.writeLog("read file : " + file.getAbsolutePath(), "debug");
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "cp1251"));
            String string;
            int c = 0;
            while ((string = reader.readLine()) != null) {
                if (string.contains("</snils>")) {
                    snilsSet.add(XMLValues.getValue(string));
                    c++;
                }
            }
            reader.close();
        } catch (Exception ex) {
            LoggingService.writeLog(ex);
        }
    }

    private void writeSnils(HashSet<String> snilsSet) {
        try {
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream("monitizationSnilsList.csv"), "cp1251"));
            int i = 0;
            int fileIndex = 0;
            for (String snils : snilsSet) {
                writer.write("\"" + formatSnils(snils) + "\"\r\n");
                writer.flush();
                i++;
            }
            writer.close();
        } catch (Exception ex) {
            LoggingService.writeLog(ex);
        }
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
            LoggingService.writeLog("wrongSnils: " + value, "debug");
        }
        return value;
    }

    private void collectPersons(HashSet<String> snilsSet) throws Exception {
        HashSet<String> controllSet = new HashSet<>();
        File file = new File("D:\\temp\\frllo_persons.csv");
        if (file.isFile() == false) {
            LoggingService.writeLog("file doesn't exists", "error");
            return;
        }
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream("for_monitizationSnilsList.csv"), "cp1251"));

        LoggingService.writeLog("read file : " + file.getAbsolutePath(), "debug");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "cp1251"));
        String string = "";
        int count = 0;
        int writed = 0;
        while ((string = reader.readLine()) != null) {
            String[] parts = string.split(",");
            if (parts.length >= 5) {
                if (Integer.parseInt(parts[5]) > 0) {

                    if (snilsSet.contains(parts[1]) == false && controllSet.contains(parts[1]) == false) {
                        controllSet.add(parts[1]);
                        writer.write(parts[1].replace("\"", ""));
                        writer.flush();
                        writer.newLine();
                        writed++;
                    }
                }
            } else {
                LoggingService.writeLog("len: " + parts.length + " " + string, "debug");
            }
            count++;
            if (count % 500000 == 0) {
                LoggingService.writeLog(" readed: " + count + "  writed: " + writed, "debug");
            }
        }
        LoggingService.writeLog(" readed: " + count + "  writed: " + writed, "debug");
        writer.close();
        reader.close();
    }
}
