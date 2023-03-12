package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Test;
import ru.gov.pfr.service.ConnectionService;
import static ru.gov.pfr.service.ConnectionService.resultToMap;
import ru.gov.pfr.service.LoggingService;

public class SplitAndForamtTest {
    
    @Test
    public void testRead() throws Exception {
        LoggingService.getInstance();
        File file = new File("D:\\scan\\scan.csv");
        if (file.isFile() == false) {
            Assert.assertEquals(true, false);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
        String string = "";
        while ((string = reader.readLine()) != null) {
            Person person = new Person();
            String[] parts = string.split(";");
            if (parts.length > 1) {
                placeFIO(parts[0], person);
                if (parts.length == 3) {
                    if (parts[2].trim().matches("\\d{3}-\\d{3}-\\d{3} \\d{2}")) {
                        person.setNpers(parts[2]);
                    }
                } else {
                    if (parts.length == 2) {
                        
                    }
                }
                if (!parts[1].isEmpty()) {
                    Pattern p = Pattern.compile("(\\d{2}).(\\d{2}).(\\d{4})");
                    Matcher m = p.matcher(parts[1]);
                    String rdatRev = m.replaceAll("$3-$2-$1");
                    person.setRdat(rdatRev);
                }
//                if (person.getNpers() != null && !person.getNpers().isEmpty()) {
                String querySQL = getQuery(person);
//                LoggingService.writeLogNoTypeNoTime(querySQL);
//                System.out.println("querySQL: " + querySQL);
//                    PrintQueryRequest pqr = new PrintQueryRequest();
                Connection connectionFBDP = ConnectionService.getInstance().getConnectionFBDP();
                Statement st = connectionFBDP.createStatement();
                ResultSet result = st.executeQuery(querySQL);
                while (result.next()) {
                    LinkedHashMap<String, String> m = resultToMap(result);
                    person.parseMap(m);
//                    for (String key : m.keySet()) {
//                        LoggingService.writeLogNoTypeNoTime(key + " : " + m.get(key));
//                    }
                }
                st.close();
                if (person.getID() != null) {
                    querySQL = getPEQuery(person);
//                    LoggingService.writeLogNoTypeNoTime(querySQL);
//                    System.out.println("querySQL: " + querySQL);
                    st = connectionFBDP.createStatement();
                    result = st.executeQuery(querySQL);
                    while (result.next()) {
                        LinkedHashMap<String, String> m = resultToMap(result);
                        person.parseMap(m);
//                        for (String key : m.keySet()) {
//                            LoggingService.writeLogNoTypeNoTime(key + " : " + m.get(key));
//                        }
                    }
                    st.close();
                }
                person.printData();
            }
        }
    }
    
    private void placeFIO(String fio, Person person) {
        String[] fioHeaders = {"man.fa", "man.im", "man.ot"};
        String[] fioParts = fio.split(" ");
        person.setFa(fioParts[0]);
        if (fioParts.length > 1) {
            person.setIm(fioParts[1]);
        }
        if (fioParts.length > 2) {
            person.setOt(fioParts[2]);
        }
    }
    
    private String getQuery(Person person) throws Exception {
        String homePath = Paths.get("").toAbsolutePath().toString();
        String pathsql = homePath + "\\sql\\checkIsPersonHasPension.sql";
        File queryFile = new File(pathsql);
        String queryTemplate = queryFromFile(queryFile);
        if (queryFile.exists()) {
            queryTemplate = queryTemplate.replace("<MAN_QUERY>", person.makeQuery());
        }
        return queryTemplate;
    }
    
    private String getPEQuery(Person person) throws Exception {
        String homePath = Paths.get("").toAbsolutePath().toString();
        String pathsql = homePath + "\\sql\\selectPEbyMan.sql";
        File queryFile = new File(pathsql);
        String queryTemplate = queryFromFile(queryFile);
        if (queryFile.exists()) {
            queryTemplate = queryTemplate.replace("<ID>", person.getID());
            queryTemplate = queryTemplate.replace("<CHANGEDATE>", person.getChangedate());
        }
        return queryTemplate;
    }
    
    public String queryFromFile(File file) throws Exception {
        String query = "";
        String string;
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
        while ((string = reader.readLine()) != null) {
            query += string + "\r\n";
        }
        return query;
    }
    
    class Person {
        
        String fa;
        String im;
        String ot;
        String rdat;
        String npers;
        private String id;
        private String changedate;
        private String dat;
        private String sroks;
        private String pgp;
        private String pw;
        private String srokpo;
        private String cpen;
        
        public void setFa(String fa) {
            this.fa = fa;
        }
        
        public void setIm(String im) {
            this.im = im;
        }
        
        public void setOt(String ot) {
            this.ot = ot;
        }
        
        public void setRdat(String rdat) {
            this.rdat = rdat;
        }
        
        public void setNpers(String npers) {
            this.npers = npers;
        }
        
        public String getFa() {
            return fa;
        }
        
        public String getIm() {
            return im;
        }
        
        public String getOt() {
            return ot;
        }
        
        public String getRdat() {
            return rdat;
        }
        
        public String getNpers() {
            return npers;
        }
        
        public String makeQuery() {
            String query = "where "
                    + ((fa != null && !fa.isEmpty()) ? ("man.fa='" + fa.toUpperCase() + "'\n") : "")
                    + ((im != null && !im.isEmpty()) ? ("and man.im='" + im.toUpperCase() + "'\n") : "")
                    + ((ot != null && !ot.isEmpty()) ? ("and man.ot='" + ot.toUpperCase() + "'\n") : "")
                    + ((rdat != null && !rdat.isEmpty()) ? ("and man.rdat='" + rdat + "'\n") : "")
                    + ((npers != null && !npers.isEmpty()) ? ("and man.npers='" + npers + "'\n") : "");
            return query;
        }
        
        private void printData() {
            String pdata = fa + ";" + im + ";" + ot + ";" + rdat;
            boolean noPensionData = true;
            if (npers != null && !npers.isEmpty() && !npers.equals("null")) {
                pdata += ";" + npers;
            }
            if (sroks != null && !sroks.isEmpty()) {
                noPensionData = false;
                pdata += ";" + sroks + ";" + srokpo + ";" + cpen;
            }
            if (noPensionData) {
                LoggingService.writeLogNoTypeNoTime(pdata + ";нет данных о пенсии");
            } else {
                LoggingService.writeLogNoTypeNoTime(pdata);
            }
        }
        
        private void parseMap(LinkedHashMap<String, String> m) {
            if (m.containsKey("ID")) {
                id = m.get("ID");
            }
            if (m.containsKey("CHANGEDATE")) {
                changedate = m.get("CHANGEDATE");
            }
            if (m.containsKey("DAT")) {
                dat = m.get("DAT");
            }
            if (m.containsKey("RDAT")) {
                rdat = m.get("RDAT");
            }
            if (m.containsKey("SROKS")) {
                sroks = m.get("SROKS");
            }
            if (m.containsKey("SROKPO")) {
                srokpo = m.get("SROKPO");
            }
            if (m.containsKey("PGP")) {
                pgp = m.get("PGP");
            }
            if (m.containsKey("PW")) {
                pw = m.get("PW");
            }
            if (m.containsKey("CPEN")) {
                cpen = m.get("CPEN");
            }

//PE_ID : 039030042179856576762177569001
//DAT : 2019-11-20
//PW : 0
//DPW : 
//PGP : 1
//CHANGEDATE : 2021-09-08 15:59:27.264
//RASPEN_ID : 039030042179856576762177569001
//CPEN : 12959.56
//SROKS : 2021-08-01
//SROKPO : 
//OPER : ВЗН
        }
        
        private CharSequence getID() {
            return id;
        }
        
        private CharSequence getChangedate() {
            return changedate;
        }
        
    }
}
