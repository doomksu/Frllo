package service;

import ru.gov.pfr.service.QueryRequester;
import ru.gov.pfr.service.PrintQueryRequest;
import ru.gov.pfr.service.ConnectionService;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class PrintRequest2Test {

    public PrintRequest2Test() {
    }

    @Test
    public void testReciveData() throws Exception {
        File folder = new File("C:\\projects\\FRLLO\\sql\\индексация срочной и накопительной 2021");
//        LoggingService.getInstance();
        ConnectionService.getInstance();
        ArrayList<CallabalQuery> queryes = new ArrayList<>();
        Future[] futures = new Future[folder.listFiles().length];

        ExecutorService es = Executors.newFixedThreadPool(folder.listFiles().length);
        int index = 0;
        for (File file : folder.listFiles()) {
            CallabalQuery cq = new CallabalQuery(file);
            queryes.add(cq);
            futures[index] = es.submit(cq);
            index++;
        }
        es.shutdown();
        while (es.isTerminated() == false) {
            es.awaitTermination(15, TimeUnit.SECONDS);
        }
        boolean allDone = false;
        while (!allDone) {
            boolean maybe = true;
            for (Future<Boolean> reader : futures) {
                if (!reader.isDone() && !reader.isCancelled()) {
                    if (reader.isCancelled()) {
//                        LoggingService.writeLog(">> reader" + reader.hashCode() + " is cancelled", "debug");
                    }
                    Thread.sleep(100);
                    if (!reader.isDone() && !reader.isCancelled()) {
                        maybe = false;
                    }
                }
            }
            allDone = maybe;
        }
        File outFile = new File("C:\\projects\\FRLLO\\sql\\индексация срочной и накопительной 2021\\результат raspen");
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outFile), "cp1251"));
        for (CallabalQuery query : queryes) {
            writer.write(query.toString());
//            LoggingService.writeLog(query.toString(), "debug");
            writer.flush();
        }
        writer.close();

    }

    public class CallabalQuery implements Callable<Boolean>, QueryRequester {

        private File file;
        private String partnumber;
        private String count;
        private String avg;

        public CallabalQuery(File file) {
            this.file = file;
        }

        @Override
        public Boolean call() throws Exception {
            ParsableQueryRequest pqr = new ParsableQueryRequest(this);
            String query = ConnectionService.getInstance().queryFromFile(file);
//            LoggingService.writeLog(file.getAbsolutePath(), "debug");
            System.out.println(">>run query: " + file.getAbsolutePath());
//            LoggingService.writeLog(">>run query: " + file.getAbsolutePath(), "debug");
            pqr.makeRequest(query);
            return true;
        }

        @Override
        public void parseResultMap(HashMap<String, String> result) {
//            LoggingService.writeLog(">> requester parse result for file: " + file.getAbsolutePath(), "debug");
            String res = "";
            for (Map.Entry<String, String> entry : result.entrySet()) {
                res += entry.getValue() + ";";
                if (entry.getKey().equals("1")) {
                    partnumber = entry.getValue();
                }
                if (entry.getKey().equals("2")) {
                    count = entry.getValue();
                }
                if (entry.getKey().equals("3")) {
                    avg = entry.getValue();
                }
            }

            File outFile = new File("C:\\projects\\FRLLO\\sql\\индексация срочной и накопительной 2021\\" + file.getName() + ".csv");
            try {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "cp1251"));
                writer.write(toString());
//                LoggingService.writeLog(toString(), "debug");
                writer.flush();
                writer.close();
            } catch (Exception ex) {
                ex.printStackTrace();
//                LoggingService.writeLog(ex);
            }
//            LoggingService.writeLog(">> requester asString result for file: " + res, "debug");
        }

        public File getFile() {
            return file;
        }

        public String getPartnumber() {
            return partnumber;
        }

        public String getCount() {
            return count;
        }

        public String getAvg() {
            return avg;
        }

        @Override
        public String toString() {
            return "CallabalQuery{" + "file=" + file + ", partnumber=" + partnumber + ", count=" + count + ", avg=" + avg + '}';
        }

    }

    public class ParsableQueryRequest extends PrintQueryRequest {

        private final QueryRequester requester;

        public ParsableQueryRequest(QueryRequester requester) {
            this.requester = requester;
        }

        @Override
        public void reciveData(LinkedHashMap<String, String> resultMap, String[] fieldsMap) throws Exception {
            requester.parseResultMap(resultMap);
        }

    }

}
