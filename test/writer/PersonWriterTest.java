package writer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import org.junit.Assert;
import org.junit.Test;
import ru.gov.pfr.service.LoggingService;
import ru.gov.pfr.service.XSDValidator;

public class PersonWriterTest {

    public PersonWriterTest() {
    }

    @Test
    public void testRepackFileToMaxSize() throws Exception {
        LoggingService.getInstance();

        File originFile = new File("D:\\temp\\open-2021-09-20-1bfac2ef-eded-4831-bb37-1ec611ceb9d1.xml");
        File fileTail = new File("D:\\temp\\rewrited\\open-2021-09-20-secac2ef-eded-4831-bb37-1ec611ceb9d1.xml");

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(originFile), "UTF8"));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("D:\\temp\\rewrited\\" + originFile.getName()), "UTF8"));
        int count = 0;
        String string;
        XSDValidator validator = new XSDValidator();
        while ((string = reader.readLine()) != null) {
            writer.write(string);
            writer.flush();
            writer.newLine();
            if (string.contains("</document>")) {
                count++;
            }

            if (count == 100000) {
                writer.flush();
                writer.write("</documents>\r\n");
                writer.write("</root>\r\n");
                writer.flush();
                writer.close();

                Assert.assertEquals(true, validator.checkFile(originFile));
                LoggingService.writeLog("file valid: " + originFile.getAbsolutePath(), "debug");
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileTail), "UTF8"));
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
                writer.write("<root>\r\n");
                writer.write("<info_sys_code>1.001</info_sys_code>\r\n");
                writer.write("<documents>\r\n");
                System.out.println("file count: " + originFile.getName() + "  " + count);
                count = 0;
            }
        }
        writer.flush();
        writer.close();
        System.out.println("file count: " + fileTail.getName() + "  " + count);
        Assert.assertEquals(true, validator.checkFile(fileTail));
        LoggingService.writeLog("file valid: " + fileTail.getAbsolutePath(), "debug");

    }

//    @Test
//    public void testWrite() throws Exception {
//        System.out.println(" validate file test");
//        File toCheck = new File("D:\\temp\\Загружен с ошибкой (структура файла не соответствует xsd-схеме)\\9299e994-6b71-4ac0-8beb-c42d1938edb6.xml");
//        File xsdFile = new File("C:\\projects\\FRLLO\\xsd\\xsd.xsd");
//        checkOutFileXMLValidation(toCheck, xsdFile);
//    }
//
//    private void checkOutFileXMLValidation(File toCheck, File xsdFile) {
//        if (toCheck != null && toCheck.exists()) {
//            Source xmlFile = new StreamSource(toCheck);
//            SchemaFactory schemaFactory = SchemaFactory
//                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
//            try {
//                Schema schema = schemaFactory.newSchema(xsdFile);
//                Validator validator = schema.newValidator();
//                validator.validate(xmlFile);
//                System.out.println("file valid");
////                checkedFiles.put(toCheck, true);
//            } catch (SAXException e) {
//                System.out.println("file invalid");
//                e.printStackTrace();
//            } catch (IOException e) {
//                System.out.println("file invalid");
//                e.printStackTrace();
//            }
//        }
//    }
}
