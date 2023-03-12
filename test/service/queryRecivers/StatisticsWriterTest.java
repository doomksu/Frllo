package service.queryRecivers;

import ru.gov.pfr.service.queryRecivers.StatisticsWriter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.gov.pfr.service.ServiceRunner;

public class StatisticsWriterTest {

    public StatisticsWriterTest() {
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
    public void testMakeStatistics() throws Exception {
        ServiceRunner sr = new ServiceRunner(null);
        StatisticsWriter sw = new StatisticsWriter(null);
        sw.fetchStatistics();
    }

}
