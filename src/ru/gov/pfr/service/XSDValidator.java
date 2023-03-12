package ru.gov.pfr.service;

import java.io.File;
import java.io.IOException;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.xml.sax.SAXException;

public class XSDValidator {

    private final File commonXSD;
    private final File monitizationXSD;

    public XSDValidator() {
        commonXSD = new File("xsd\\xsd.xsd");
        monitizationXSD = new File("xsd\\monetization_xsd.xsd");
    }

    public boolean checkFile(File file) {
        if (file.getName().contains("monet") || file.getName().contains("monit")) {
            return checkOutFileXMLValidation(file, monitizationXSD);
        }
        return checkOutFileXMLValidation(file, commonXSD);
    }

    private boolean checkOutFileXMLValidation(File toCheck, File xsdFile) {
        if (toCheck != null && toCheck.exists()) {
            Source xmlFile = new StreamSource(toCheck);
            SchemaFactory schemaFactory = SchemaFactory
                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            try {
                Schema schema = schemaFactory.newSchema(xsdFile);
                Validator validator = schema.newValidator();
                validator.validate(xmlFile);
                return true;
            } catch (SAXException e) {
                LoggingService.writeLog(e);
                return false;
            } catch (IOException e) {
                LoggingService.writeLog(e);
                return false;
            }
        }
        return false;
    }
}
