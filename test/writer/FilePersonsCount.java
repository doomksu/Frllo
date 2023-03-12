package writer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import org.junit.Test;
import ru.gov.pfr.service.XSDValidator;

public class FilePersonsCount {

    @Test
    public void testCountPersons() throws Exception {
        File folder = new File("D:\\temp\\rewrited");
        for (File file : folder.listFiles()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
            int count = 0;
            String string;
            XSDValidator validator = new XSDValidator();
            while ((string = reader.readLine()) != null) {
                if (string.contains("</document>")) {
                    count++;
                }
            }
            System.out.println("file count: " + file.getName() + "  " + count);
        }
    }
    
}
