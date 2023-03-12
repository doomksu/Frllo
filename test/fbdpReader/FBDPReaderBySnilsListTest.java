package fbdpReader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import org.junit.Test;
import ru.gov.pfr.personEntities.fbdpSources.PersonFileSourceData;
import ru.gov.pfr.service.LoggingService;

public class FBDPReaderBySnilsListTest {

    @Test
    public void testRead_() throws Exception {
        StringBuilder comb = new StringBuilder();
        String path = "D:\\temp\\frllo_14_01.csv";
        LoggingService.writeLog(">>read: " + path, "test");
        boolean snils = false;
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "cp1251"));
        String string;
        while ((string = reader.readLine()) != null) {
            if (string.contains("130-392-412 19") || string.contains("104-533-143 07") || string.contains("104-654-923 42")) {
                LoggingService.writeLog("string: " + string, "test");
                PersonFileSourceData pfsd = new PersonFileSourceData(string);
                if (pfsd.isValid()) {
                    LoggingService.writeLog(">>valid: " + pfsd.getNPERS(), "test");
                    LoggingService.writeLog(pfsd.makeXMLStructure().makeLgotaXML(), "test");
                }
            }
        }
        if (!snils) {
            LoggingService.writeLog(">> no npers found", "test");
        }
        reader.close();
    }

//    @Test
//    public void testRead() throws Exception {
//        StringBuilder comb = new StringBuilder();
//        String path = "C:\\projects\\FRLLO\\ФРЛЛО_18.01.2021 результаты первичной выгрузки\\разбор\\fio_snils.txt";
//        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "cp1251"));
//        String string;
//        boolean first = true;
//        while ((string = reader.readLine()) != null) {
//            if (string.trim().matches("\\d{3}-\\d{3}-\\d{3} \\d{2}")) {
//                if (!first) {
//                    comb.append(",\"" + string.trim() + "\"");
//                } else {
//                    comb.append("\"" + string.trim() + "\"");
//                }
//            } else {
//                System.out.println("doesn't match: " + string);
//            }
//            first = false;
//        }
//        System.out.println("comb: " + comb);
//    }
}
