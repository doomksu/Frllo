package service;

import ru.gov.pfr.service.LoggingService;
import ru.gov.pfr.service.XSDValidator;
import java.io.File;
import org.junit.Test;

public class ValidateFilesInFolderTest {

    @Test
    public void testLoopValidate() {
        LoggingService.getInstance();
        File folder = new File("D:\\temp\\validated_and_clear");
        XSDValidator validator = new XSDValidator();
        for (File file : folder.listFiles()) {
            System.out.println("check file: " + file.getAbsolutePath());
            if (file.getName().contains(".xml")) {
                validator.checkFile(file);

            }
        }
    }
}
