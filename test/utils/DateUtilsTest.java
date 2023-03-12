package utils;

import ru.gov.pfr.utils.AutoThrowStack;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.gov.pfr.utils.DateUtils;

/**
 *
 * @author kneretin
 */
public class DateUtilsTest {

    public DateUtilsTest() {
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
        String out = DateUtils.thresholdMinDateOrIncomingDate("2018-12-01", "2020-12-01");
        System.out.println("out: " + out);
        Assert.assertEquals("2020-12-01", DateUtils.thresholdMinDateOrIncomingDate("2020-12-01", "2018-12-01"));
        Assert.assertEquals("", DateUtils.thresholdMinDateOrIncomingDate("2020-12-01", ""));
    }

}
