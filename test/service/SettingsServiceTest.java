package service;

import ru.gov.pfr.service.LoggingService;
import ru.gov.pfr.service.SettingsService;
import org.junit.Test;

public class SettingsServiceTest {

    /**
     * Test of checkDummyFile method, of class SettingsService.
     */
    @Test
    public void testCheckDummyFile() {
        boolean hasDummy = SettingsService.getInstance().checkDummyFile("LOG_CHANGES_CHECKER_RESULT");
        LoggingService.writeLog("has dummy: " + hasDummy, "debug");
        hasDummy = SettingsService.getInstance().checkDummyFile("LOG_CHANGES_CHECKER_RESULT");
    }

}
