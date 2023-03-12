package ru.gov.pfr.fbdpReader;

import ru.gov.pfr.personEntities.PersonDataSource;
import ru.gov.pfr.personEntities.convertedXMLSource.PersonConvertedXMLSourceData;
import ru.gov.pfr.service.LoggingService;

/**
 *
 * @author Kirill Neretin
 */
class MockChangesChecker extends FRLLOChangesChecker {

    public MockChangesChecker(PersonConvertedXMLSourceData pXML, PersonDataSource person) {
        super(person, null);
    }

    @Override
    public boolean isPersonChangedOrNew() {
        LoggingService.writeLog("mockChangesChecker always check persons as new: "+ newPerson.getNPERS(), "debug");
        return true;
    }
    
    

}
