package utils;

import ru.gov.pfr.utils.XMLValues;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author User
 */
public class XMLValuesTest {

    private String convertedFile;

    public XMLValuesTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testIsSingleTag() {
        String[] vals = {"<citizen>",
            "<register_id/>",
            "<ext_citizen_id>071061118457017538486584367001</ext_citizen_id>",
            "<name>ВАЛЕНТИНА</name>",
            "<surname>ОРЛОВА</surname>",
            "<patronymic>ВЛАДИМИРОВНА</patronymic>",
            "<birthdate>1952-05-03</birthdate>",
            "<sex>2</sex>"};

        for (String string : vals) {
            System.out.println("is single teg: " + string + "   " + XMLValues.isSingleTag(string));
        }

    }

    @Test
    public void testReadBackupHeader() throws Exception {
        String filePath = "C:\\projects\\FRLLO\\frllo_persons.csv";
        File f = new File(filePath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "cp1251"));
        String string;

        int c = 0;
        while ((string = reader.readLine()) != null) {
            System.out.println(string);
            c++;
            if (c == 10) {
                break;
            }
        }
        reader.close();
    }

    @Test
    public void testReadBackupHeader1() throws Exception {
        System.out.println("testReadBackupHeader1");
        String filePath = "C:\\projects\\FRLLO\\frllo_persons.csv";
        File f = new File(filePath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "cp1251"));
        String string;

        int c = 0;
        String newval = "";
        while ((string = reader.readLine()) != null) {
            String[] vals = string.split(",");
            newval = valsFromStringArray(vals);
            System.out.println(newval);
            c++;
            if (c == 10) {
                break;
            }
        }
        reader.close();
    }

    private String valsFromStringArray(String[] line) throws Exception {
        String vals = "";
        boolean notFirst = false;
        for (String val : line) {
            if (notFirst) {
                vals += ";";
            }
            vals += val;
            notFirst = true;
        }
        return vals;
    }

}
