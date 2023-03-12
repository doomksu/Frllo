/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import ru.gov.pfr.utils.AutoThrowStack;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author kneretin
 */
public class AutoThrowStackTest {
    
    public AutoThrowStackTest() {
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
     * Test of insertString method, of class AutoThrowStack.
     */
    @Test
    public void testInsertString() {
        AutoThrowStack ats = new AutoThrowStack(3);
        ats.insertString("0");
        System.out.println("vals: "+ ats.getMessages());
        ats.insertString("1");
        System.out.println("vals: "+ ats.getMessages());
        ats.insertString("2");
        System.out.println("vals: "+ ats.getMessages());
        ats.insertString("3");
        ats.insertString("4");
        ats.insertString("5");
        ats.insertString("6");
        System.out.println("vals: "+ ats.getMessages());
    }


    
}
