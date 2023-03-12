package service;

import ru.gov.pfr.service.LoggingService;
import ru.gov.pfr.service.XSDValidator;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import org.junit.Test;

public class ConvertedSearchAndRewriterTest {

    private File convertedFolder = new File("D:\\temp\\frllo_validate_files_");
    private String[] patterns;
    private int matches;
    private final File rewriteFolder = new File("D:\\temp\\frllo_validate_files");

    @Test
    public void testSearch() throws Exception {
        LoggingService.writeLog("testSearch", "test");
        LoggingService.writeLog("ConvertedSearchAndRewriterTest()", "test");

        patterns = new String[]{
            "<snils>07081801042</snils>",};
        for (File convertedFile : convertedFolder.listFiles()) {
            if (convertedFile.isFile()) {
                LoggingService.writeLog("read file: " + convertedFile.getName(), "debug");
                readAndSearchPatern(convertedFile);
            }
        }
        LoggingService.writeLog("search ", "debug");
    }

    private void readAndSearchPatern(File convertedFile) throws Exception {
        String string;
        ArrayList<String> buffer = new ArrayList<String>();
        String fileName = convertedFile.getName();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(convertedFile), "utf8"));
        File rewritedFile = new File(rewriteFolder.getAbsolutePath() + "\\" + fileName);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(rewritedFile), "utf8"));
        boolean found = false;
        boolean header = true;
        XSDValidator validator = new XSDValidator();
        while ((string = reader.readLine()) != null) {
            if (isMatch(string)) {
                found = true;
            }
            if (header) {
                writer.write(string + "\r\n");
                writer.flush();
            }
            if (!header) {
                buffer.add(string);
            }
            if (string.contains("<document>")) {
                header = false;
                if (!found) {
                    writeBuffer(buffer, writer);
                } else {
                    LoggingService.writeLog("found pattern in : " + convertedFile.getName(), "debug");
                }
                buffer.clear();
                found = false;
                matches = 0;
            }
        }
        LoggingService.writeLog("done read file: " + convertedFile.getName(), "debug");
        reader.close();
        if (!found) {
            writeBuffer(buffer, writer);
        }
        writer.flush();
        writer.close();
        if (validator.checkFile(rewritedFile)) {
            LoggingService.writeLog("valid file: " + rewritedFile.getAbsolutePath(), "debug");
        } else {
            LoggingService.writeLog("invalid file: " + rewritedFile.getAbsolutePath(), "error");
        }
    }

    private void writeBuffer(ArrayList<String> buffer, BufferedWriter writer) throws IOException {
        for (String string : buffer) {
            writer.write(string + "\r\n");
            writer.flush();
        }
    }

    private boolean isMatch(String str) {
        for (String pattern1 : patterns) {
            if (str.trim().contains(pattern1)) {
                matches++;
                LoggingService.writeLog("found: " + pattern1, "debug");
                return true;
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
