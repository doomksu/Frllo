package fbdpReader;

import ru.gov.pfr.fbdpReader.FRLLOChangesChecker;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.gov.pfr.personEntities.fbdpSources.PersonFileSourceData;
import ru.gov.pfr.service.ConnectionService;
import ru.gov.pfr.service.LoggingService;

/**
 *
 * @author kneretin
 */
public class FRLLOChangesCheckerTest {

    public FRLLOChangesCheckerTest() {
    }

    /**
     * Test of isPersonChangedOrNew method, of class FRLLOChangesChecker.
     */
    @Test
    public void testIsPersonChangedOrNew() throws Exception {
        ConnectionService.getInstance();
        String path = "D:\\temp\\testChangesChaecker.csv";
        LoggingService.writeLog(">>read: " + path, "test");
        boolean snils = false;
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "cp1251"));
        String string;
        int count = 0;

        while ((string = reader.readLine()) != null) {
            if (count > 0) {
                LoggingService.writeLog("string: " + string, "test");
                PersonFileSourceData pfsd = new PersonFileSourceData(string);

                if (pfsd.isValid()) {
                    FRLLOChangesChecker checker = new FRLLOChangesChecker(pfsd,
                            ConnectionService.getInstance().selectConvertedPersonMap(pfsd.getNPERS()));
                    checker.isPersonChangedOrNew();
                    LoggingService.writeLog(">>valid: " + pfsd.getNPERS(), "test");
                    LoggingService.writeLog(pfsd.makeXMLStructure().makeLgotaXML(), "test");
                } else {
                    LoggingService.writeLog(">>person is not valid", "test");
                }
                if (count >= 10) {
                    break;
                }
            }
            count++;
        }
        if (!snils) {
            LoggingService.writeLog(">> no npers found", "test");
        }
        reader.close();

    }

}
