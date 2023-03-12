package personEntities;

import ru.gov.pfr.personEntities.fbdpSources.PersonFileSourceData;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.gov.pfr.service.LoggingService;

public class PersonFileSourceDataTest {

    public PersonFileSourceDataTest() {
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
    public void testSomeMethod() throws Exception {
        File testIn = new File("D:\\temp\\testFRLLLOLines.csv");
//        File testIn = new File("D:\\temp\\frllo_14_01.csv");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(testIn), "Cp1251"));
        String string = "";
        int count = 0;
        boolean isFirst = true;
        while ((string = reader.readLine()) != null) {
            if (!isFirst) {
                PersonFileSourceData pfsd = new PersonFileSourceData(string);
                LoggingService.writeLog("str: " + string, "test");
                pfsd.print();
                if (pfsd.isValid()) {
                    LoggingService.writeLog("xml:\r\n"
                            + pfsd.makeXMLStructure().makeLgotaXML(), "test");
                    if (pfsd.isMonetization()) {
                        LoggingService.writeLog("xml monetization:\r\n"
                                + pfsd.makeXMLStructure().makeMonetizationXML(), "test");
                    }
                } else {
                    LoggingService.writeLog(" invalid record: " + pfsd.getValidationMeassage(), "test");
                }
                count++;
                if (count >= 3) {
                    break;
                }
            } else {
                LoggingService.writeLog("header String :" + string, "test");
            }
            isFirst = false;
        }
    }
}
