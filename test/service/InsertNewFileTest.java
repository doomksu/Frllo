package service;

import ru.gov.pfr.service.LoggingService;
import ru.gov.pfr.service.ConnectionService;
import org.junit.Test;

public class InsertNewFileTest {

    @Test
    public void testValidateFilesInFolders() {
        LoggingService.getInstance();
        try {
            String sql = "";
            ConnectionService.getInstance();
            ConnectionService.getInstance().executeInsertUpdateQuery(sql);
            
        }catch(Exception ex){
            LoggingService.writeLog(ex);
        }

    }
}
