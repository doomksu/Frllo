package utils;

import ru.gov.pfr.utils.LoadDatesTemplater;
import org.junit.Test;

public class LoadDatesTemplaterTest {

    @Test
    public void testFormatTemplate() {
        try {

            LoadDatesTemplater ldt = new LoadDatesTemplater("2021-01-14", "2021-06-02");
            String template = "";
            while ((template = ldt.getTemplate()) != null) {
                System.out.println("template: " + template);
            }
            
            ldt = new LoadDatesTemplater("2021-06-01", "2021-06-02");
            template = "";
            while ((template = ldt.getTemplate()) != null) {
                System.out.println("template: " + template);
            }
            
            ldt = new LoadDatesTemplater("2021-06-02", "2021-06-02");
            template = "";
            while ((template = ldt.getTemplate()) != null) {
                System.out.println("template: " + template);
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
