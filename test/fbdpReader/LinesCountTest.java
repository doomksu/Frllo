/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fbdpReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author kneretin
 */
public class LinesCountTest {

    public LinesCountTest() {

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
    public void testSomeMethod() throws FileNotFoundException, UnsupportedEncodingException, IOException {
        testCountText("1000");
        testCountText("1020");
        testCountText("21020");
        testCountText("121020");
        testCountText("12121020");

    }

    public static void testCountText(String val) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        String source = val;
        String countText = "";

        while (source.length() > 3) {
            countText = source.substring(source.length() - 3, source.length()) + " " + countText;
            source = source.substring(0, source.length() - 3);
        }
        if (source.length() > 0) {
            countText = source + " " + countText;
        }
        System.out.println("val: " + val + "  countText: " + countText);
    }

//    @Test
//    public void testSomeMethod() throws FileNotFoundException, UnsupportedEncodingException, IOException {
//        String string;
//        File file = new File("C:\\projects\\FRLLO\\frllo_actual.csv");
//        int lines =0;
//        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "Cp1251"));
//        while ((string = reader.readLine()) != null) {
//            lines++;
//        }
//        reader.close();
//        System.out.println("lines count: "+ lines);
//    }
}
