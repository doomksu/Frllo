package ru.gov.pfr.fbdpReader;

import ru.gov.pfr.personEntities.PersonDataSource;
import ru.gov.pfr.service.LoggingService;
import ru.gov.pfr.writer.PersonWriter;

/**
 *
 * @author Kirill Neretin
 */
class MockPersonWriter extends PersonWriter {

    public MockPersonWriter(MockFrlloConverter converter) {
        super(converter);
    }

    protected void writeNPERSJournal(PersonDataSource person) {
        LoggingService.writeLog("mock mode - writeNPERSJournal ", "debug");
    }
}
