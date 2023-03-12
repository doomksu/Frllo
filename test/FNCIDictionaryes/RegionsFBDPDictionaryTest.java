/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FNCIDictionaryes;

import ru.gov.pfr.FNCIDictionaryes.RegionsDictionary;
import ru.gov.pfr.FNCIDictionaryes.RegionsFBDPDictionary;
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
public class RegionsFBDPDictionaryTest {

    public RegionsFBDPDictionaryTest() {
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
     * Test of getInstance method, of class RegionsFBDPDictionary.
     */
    @Test
    public void testGetInstance() {
    }

    /**
     * Test of addValue method, of class RegionsFBDPDictionary.
     */
    @Test
    public void testAddValue() {
    }

    /**
     * Test of getValue method, of class RegionsFBDPDictionary.
     */
    @Test
    public void testGetValue() {
    }

    /**
     * Test of print method, of class RegionsFBDPDictionary.
     */
    @Test
    public void testPrint() {
        for (String oktmos : RegionsFBDPDictionary.getInstance().getMap().values()) {
            if (RegionsDictionary.getInstance().getMap().containsValue(oktmos)) {
                LoggingService.writeLog(oktmos, "debug");
            } else {
                LoggingService.writeLog(oktmos+";--"  , "error");
            }
        }
    }

    /**
     * Test of getMap method, of class RegionsFBDPDictionary.
     */
    @Test
    public void testGetMap() {
    }

}
