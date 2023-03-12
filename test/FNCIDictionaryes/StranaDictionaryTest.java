package FNCIDictionaryes;

import ru.gov.pfr.FNCIDictionaryes.StranaFBDPDictionary;
import ru.gov.pfr.FNCIDictionaryes.StranaDictionary;
import java.util.HashMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.gov.pfr.service.LoggingService;

/**
 *
 * @author kneretin
 */
public class StranaDictionaryTest {

    public StranaDictionaryTest() {
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

    /**
     * Test of getInstance method, of class StranaDictionary.
     */
    @Test
    public void testGetInstance() {

//        StranaDictionary.print();
//        StranaFBDPDictionary.print();
        HashMap<String, String> commonMap = new HashMap<>();

        for (String val : StranaFBDPDictionary.getInstance().getMap().keySet()) {
            StranaFBDPDictionary.addValue(val, StranaDictionary.getInstance().getValue(val));
        }
        LoggingService.writeLog("commonStrana ", "debug");
//        StranaFBDPDictionary.print();

    }

}
