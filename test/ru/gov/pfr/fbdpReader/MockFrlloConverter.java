package ru.gov.pfr.fbdpReader;

import java.sql.SQLException;
import ru.gov.pfr.controller.MockMainController;
import ru.gov.pfr.personEntities.PersonDataSource;
import ru.gov.pfr.personEntities.convertedXMLSource.PersonConvertedXMLSourceData;
import ru.gov.pfr.service.LoggingService;

/**
 *
 * @author User
 */
public class MockFrlloConverter extends FrlloConverter {

    public MockFrlloConverter(MockMainController controller) throws Exception {
        super(new MockMainController());
        errorWriter = new MockErrorWriter();
        writer = new MockPersonWriter(this);
    }

    @Override
    protected FRLLOChangesChecker makeChangesChecker(PersonConvertedXMLSourceData pXML, PersonDataSource person) throws Exception {
        return new MockChangesChecker(pXML, person);
    }

    @Override
    protected void makeDBUpdatesOnWritePerson(PersonDataSource person) throws SQLException {
        LoggingService.writeLog("mock db updates on write person", "debug");
    }
    
    
    
}
