package service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DumpSplitTest {

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
    public void readDumpTest() throws Exception {
        File file = new File("D:\\temp\\persons14.05.csv");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "cp1251"));
        int lines = 0;
        int partFileIndex = 1;
        String string = "";
        BufferedWriter writer = null;
        while ((string = reader.readLine()) != null) {
            if (writer == null) {
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("D:\\temp\\base\\persons" + partFileIndex), "Cp1251"));
            }
            writer.write(string);
            writer.newLine();
            writer.flush();
            lines++;
            if (lines == 100000) {
                writer.flush();
                writer.close();
                lines = 0;
                partFileIndex++;
                writer = null;
            }
        }
        writer.flush();
        writer.close();
    }

}
