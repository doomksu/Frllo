/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.gov.pfr.fbdpReader;

import java.util.LinkedHashMap;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.gov.pfr.personEntities.fbdpSources.PersonDBFBDPSourceData;
import ru.gov.pfr.personEntities.fbdpSources.PersonFBDPSources;

/**
 *
 * @author User
 */
public class MockFBDPPersonMap extends PersonDBFBDPSourceData{
    
    public MockFBDPPersonMap(LinkedHashMap<String, String> outMap) {
        super(outMap);
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

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
}
