package service;

import ru.gov.pfr.service.LoggingService;
import ru.gov.pfr.service.ConnectionService;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import org.junit.Test;
import ru.gov.pfr.utils.XMLPerson;

public class ConvertedSearchTest {

    private File convertedFolder;
    private String pattern;
    private String[] patterns;
    private int matches;

    @Test
    public void testSearch() throws Exception {
        new ConvertedSearchTest();
    }

    public ConvertedSearchTest() throws Exception {

        patterns = new String[]{
            "<snils>20534248632</snils>", //            "<snils>20937094472</snils>",
        //            "<snils>20745986087</snils>",
        //            "<snils>17758516017</snils>",
        //            "<snils>19751844823</snils>",
        };
        this.convertedFolder = new File("D:\\temp\\frllo_validate_files_");
        for (File convertedFile : convertedFolder.listFiles()) {
            LoggingService.writeLog("read file: " + convertedFile.getName(), "debug");
            readAndSearchPatern(convertedFile);
        }
        LoggingService.writeLog("search ", "debug");
    }

    private void readAndSearchPatern(File convertedFile) throws Exception {
        String string;
        XMLPerson xmlPerson = null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(convertedFile), "utf8"));
        String fileID = String.valueOf(ConnectionService.getInstance().getLoadedFileId(convertedFile.getName(), false));
        int count = 0;
        boolean found = false;

        while ((string = reader.readLine()) != null) {
            if (xmlPerson != null) {
                xmlPerson.parseString(string);
                if (isMatch(string)) {
                    found = true;
                }
            }
            if (string.contains("</document>")) {
                count++;
                if (xmlPerson != null && found) {
                    LoggingService.writeLog("pattern: " + stringPattern()
                            + " in file: " + convertedFile.getName()
                            + " person: " + xmlPerson.getSnils()
                            + " id: " + xmlPerson.getNvpId()
                            + " guid: " + xmlPerson.getConvertGUID(),
                            "debug");
                    found = false;
                    matches = 0;
                }
            }
            if (string.contains("<document>")) {
                found = false;
                xmlPerson = new XMLPerson();
                matches = 0;
            }
        }
        LoggingService.writeLog("done read file: " + convertedFile.getName(), "debug");
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
