package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import org.junit.Test;
import ru.gov.pfr.service.LoggingService;

public class ChunkFileReaderTest {

    @Test
    public void readNSUTest() throws Exception {
        File file = new File("D:\\temp\\frllo_validate_files_\\open-2021-06-15-5b68c4d8-c0df-4e15-b1d5-810a91e5fc6a.xml");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
        if (!file.isFile()) {
            System.out.println("not file: " + file.getAbsolutePath());
            return;
        }
        LoggingService.getInstance();
        System.out.println("file exists");
        LoggingService.writeLog("sample", "debug");
        String string = "";
        int lineNumber = 0;
        boolean writeLog = false;
        while ((string = reader.readLine()) != null) {
            if (writeLog) {
//                LoggingService.writeLog("line: " + lineNumber + " : " + string, "debug");
                LoggingService.writeLog(string, "debug");
            }
            if (lineNumber == 2487907) {  //15593209
                writeLog = true;
            }
            if (lineNumber == 2487997) {
                writeLog = false;
                break;
            }
            lineNumber++;
        }
        reader.close();
    }
}
