package service;

import ru.gov.pfr.service.SchedulerService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SchedulerServiceTest {

    public SchedulerServiceTest() {
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
    public void testStopLooping() {
        LocalDateTime now = LocalDateTime.now();
        now = now.plusHours(3);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM HH:mm:ss");
        System.out.println("new now: " + now.format(dtf));
        if (now.getHour() >= 21 || now.getHour() <= 6) {
            System.out.println("at night");
        } else {
            System.out.println("at day");
        }
    }

//    @Test
//    public void testCheckSleepIntervalsValidation() {
//        System.out.println("0 valid: " + SchedulerService.isSleepIntervalPatternValid("0"));
//        System.out.println("1 valid: " + SchedulerService.isSleepIntervalPatternValid("1"));
//        System.out.println("61 valid: " + SchedulerService.isSleepIntervalPatternValid("61"));
//        System.out.println("60 valid: " + SchedulerService.isSleepIntervalPatternValid("60"));
//        System.out.println("20 valid: " + SchedulerService.isSleepIntervalPatternValid("20"));
//        System.out.println("10 valid: " + SchedulerService.isSleepIntervalPatternValid("10"));
//        System.out.println("11 valid: " + SchedulerService.isSleepIntervalPatternValid("11"));
//        System.out.println("-1 valid: " + SchedulerService.isSleepIntervalPatternValid("-1"));
//        System.out.println("asd valid: " + SchedulerService.isSleepIntervalPatternValid("asd"));
//    }
    
    @Test
    public void testCheckStartTimeValidation() {
        System.out.println("0:12 valid: " + SchedulerService.isStartTimePatternValid("0:12"));
        System.out.println("0:0 valid: " + SchedulerService.isStartTimePatternValid("0:0"));
        System.out.println("00:00 valid: " + SchedulerService.isStartTimePatternValid("00:00"));
        System.out.println("23:59 valid: " + SchedulerService.isStartTimePatternValid("23:59"));
        System.out.println("24:60 valid: " + SchedulerService.isStartTimePatternValid("24:60"));
        System.out.println("24:59 valid: " + SchedulerService.isStartTimePatternValid("24:59"));
        System.out.println("25:59 valid: " + SchedulerService.isStartTimePatternValid("25:59"));
        System.out.println("221:59 valid: " + SchedulerService.isStartTimePatternValid("221:59"));
        System.out.println("12:159 valid: " + SchedulerService.isStartTimePatternValid("12:159"));
        System.out.println("12:61 valid: " + SchedulerService.isStartTimePatternValid("12:61"));
    }

}
