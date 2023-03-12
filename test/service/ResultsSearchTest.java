package service;

import ru.gov.pfr.service.LoggingService;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import org.junit.Test;
import ru.gov.pfr.utils.XMLResult;

public class ResultsSearchTest {

    private File resultsFolder;
    private String[] patterns;
    private int matches;
    private FileWriter foundWriter;
    private int count;
    private int total;

    @Test
    public void testSearch() throws Exception {
        new ResultsSearchTest();
    }

    public ResultsSearchTest() throws Exception {
        total=0;
        patterns = new String[]{
            "<code>CitizenIdentifyError</code>",
            "<description>Переданы некорректные идентификаторы документов гражданина</description>"
        };
        foundWriter = new FileWriter("wrongdocs.csv");
        this.resultsFolder = new File("C:\\projects\\FRLLO\\frllo_results_real");
        for (File convertedFile : resultsFolder.listFiles()) {
            readAndSearchPatern(convertedFile);
            LoggingService.writeLog("read file: " + convertedFile.getName() + " found: " + count +" total: "+ total, "debug");
        }
        foundWriter.close();
        LoggingService.writeLog("search complete: "+ total, "debug");
    }

    private void readAndSearchPatern(File convertedFile) throws Exception {
        String string;
        XMLResult xmlResult = null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(convertedFile), "utf8"));
        count = 0;
        boolean found = false;
        while ((string = reader.readLine()) != null) {
            if (xmlResult != null) {
                xmlResult.parseString(string);
                if (isMatch(string)) {
                    found = true;
                    foundWriter.write("insert into NATA.WR_DOCS VALUES('" + xmlResult.getGuid() + "')\r\n");
                    foundWriter.flush();
                    found = false;
                    count++;
                    total++;
                    matches = 0;
                }
            }
            if (string.contains("</document_id>")) {

            }
            if (string.contains("<document_id>")) {
                found = false;
                xmlResult = new XMLResult();
                xmlResult.parseString(string);
                matches = 0;
            }
        }
        reader.close();
    }

    private boolean isMatch(String str) {
        for (String pattern1 : patterns) {
            if (str.trim().contains(pattern1)) {
                matches++;
            }
        }
        if (matches == patterns.length) {
            return true;
        }
        return false;
    }

    private String stringPattern() {

        String ret = "";
        for (String pattern1 : patterns) {
            ret += " " + pattern1 + " ";
        }
        return ret;
    }
}
