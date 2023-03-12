package fbdpReader;

import java.io.File;
import java.io.IOException;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.junit.Test;
import org.xml.sax.SAXException;
import ru.gov.pfr.service.LoggingService;

public class CheckValidationTest {

    @Test
    public void testValidateFilesInFolders() {
        String path = "D:\\temp\\frllo_validate_files_";
        File folder = new File(path);
        testValidateFilesInFolder(folder);
    }

    public void testValidateFilesInFolder(File folder) {
        LoggingService.writeLog(">>testValidateFilesInFolder", "test");
        File xsdM = new File("xsd\\monetization_xsd.xsd");
        File xsdL = new File("xsd\\xsd.xsd");
        if (folder.isDirectory()) {
            for (File file : folder.listFiles()) {
                if (file.isFile()) {
                    if (file.getName().contains("monitization")) {
                        validateFile(file, xsdM);
                    } else {
                        validateFile(file, xsdL);
                    }
                }
            }
        }
    }

    public boolean validateFile(File file, File xsd) {
        Source xmlFile = new StreamSource(file);
        try {
            SchemaFactory schemaFactory = SchemaFactory
                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(xsd);
            Validator validator = schema.newValidator();
            validator.validate(xmlFile);
            LoggingService.writeLog(xmlFile.getSystemId() + " is valid", "test");
            return true;
        } catch (SAXException e) {
            LoggingService.writeLog(xmlFile.getSystemId() + " is NOT valid reason: ", "error");
            LoggingService.writeLog(e);
        } catch (IOException e) {
            LoggingService.writeLog(xmlFile.getSystemId() + " is NOT valid reason: " + e.getMessage(), "error");
        }
        return false;
    }

}
