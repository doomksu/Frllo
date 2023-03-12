package ru.gov.pfr.fbdpReader;

import ru.gov.pfr.personEntities.PersonDataSource;
import ru.gov.pfr.service.LoggingService;

/**
 *
 * @author Kirill Neretin
 */
class MockErrorWriter extends ErrorWriter {

    public MockErrorWriter() {
    }

    @Override
    public void addPerson(PersonDataSource person) {
        LoggingService.writeLog("personWriteError", "error");
        LoggingService.writeLog(person.getValidationMeassage(), "error");
    }

}
