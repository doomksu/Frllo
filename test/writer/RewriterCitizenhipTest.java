package writer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.junit.Test;
import org.xml.sax.SAXException;
import ru.gov.pfr.service.LoggingService;
import ru.gov.pfr.utils.XMLValues;

/**
 *
 * @author kneretin
 */
public class RewriterCitizenhipTest {

    @Test
    public void testWrite() throws Exception {
        File xsdM = new File("C:\\projects\\FRLLO\\xsd\\monetization_xsd.xsd");
        File xsdL = new File("C:\\projects\\FRLLO\\xsd\\xsd.xsd");

        File folder = new File("D:\\temp\\frllo_validate_files_");
        for (File toCheck : folder.listFiles()) {
            LoggingService.writeLog("will read file: " + toCheck.getAbsolutePath(), "debug");
            File choosenXSD = xsdL;
            if (toCheck.getName().contains("monit") || toCheck.getName().contains("monet")) {
                choosenXSD = xsdM;
            }
            checkOutFileXMLValidation(readWriteBlock(toCheck), choosenXSD);
        }
    }

    private File readWriteBlock(File file) {
        LoggingService.writeLog("readWriteBlock test for file : " + file.getAbsolutePath(), "debug");
        File rewrite = new File("D:\\temp\\frllo_rewrite_citizenship\\" + file.getName());
        try {
            String string = "";
            boolean hasCitizenship = false;
            boolean confirmBlock = false;
            boolean personHasConfirmBlock = false;
            boolean inPerson = false;
            String compCitizenshipBlock = "";
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
            BufferedWriter rewriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(rewrite), "UTF8"));
            while ((string = reader.readLine()) != null) {

                if (string.contains("<document>")) {
                    inPerson = true;
                }

                if (string.contains("</document>")) {
                    inPerson = false;
                    personHasConfirmBlock = false;
                }

                if (string.contains("<citizenship_confirm>")) {
                    personHasConfirmBlock = true;
                    confirmBlock = true;
                    if (hasCitizenship == false) {
//                        LoggingService.writeLog("found citizenship_confirm", "debug");
                    }
                    hasCitizenship = false;
                    compCitizenshipBlock = "";
                }
                if (confirmBlock) {
                    if (!string.contains("<identify_docs>") && !string.contains("</identify_docs>")) {
                        compCitizenshipBlock += string + "\r\n";
                    }
                }
                if (string.contains("<snils>")) {
                    if (personHasConfirmBlock) {
                        LoggingService.writeLog("snils: " + XMLValues.getValue(string), "debug");
                    }
                }

                if (string.contains("</citizenship>")) {
                    hasCitizenship = true;
                }

                if (confirmBlock == false) {
                    rewriter.write(string + "\r\n");
                    if (hasCitizenship) {
                        rewriter.write(compCitizenshipBlock);
                        hasCitizenship = false;
                    }
                    compCitizenshipBlock = "";
                    rewriter.flush();
                }
                if (string.contains("</citizenship_confirm>")) {
                    confirmBlock = false;
                    hasCitizenship = false;
                }
            }
            reader.close();
            rewriter.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return rewrite;
    }

    private void checkOutFileXMLValidation(File toCheck, File xsdFile) {
        if (toCheck != null && toCheck.exists()) {
            Source xmlFile = new StreamSource(toCheck);
            SchemaFactory schemaFactory = SchemaFactory
                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            try {
                Schema schema = schemaFactory.newSchema(xsdFile);
                Validator validator = schema.newValidator();
                validator.validate(xmlFile);
                System.out.println("file valid");
//                checkedFiles.put(toCheck, true);
            } catch (SAXException e) {
                System.out.println("file invalid");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("file invalid");
                e.printStackTrace();

            }
        }
    }

}
