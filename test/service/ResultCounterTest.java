package service;

import ru.gov.pfr.service.LoggingService;
import ru.gov.pfr.service.SettingsService;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import org.junit.Test;

public class ResultCounterTest {

    private HashMap<String, Integer> personsCount;
    private HashMap<String, Integer> uniquePersonsCount;
    private HashMap<String, HashSet<String>> uniqueIdsMap;
    private HashSet<ResultFileCounter> counters;

    @Test
    public void testReadResultsAndOut() throws InterruptedException {
        LoggingService.getInstance();
        LoggingService.writeLog("Read and count result files", "debug");
        personsCount = new HashMap<>();
        uniqueIdsMap = new HashMap<>();
        uniquePersonsCount = new HashMap<>();
        counters = new HashSet<>();
        String path = SettingsService.getInstance().getValue("resultsPath");
        System.out.println("path: " + path);
        File folder = new File(path);
        combineUniqueNames(folder);
    }

    private void combineUniqueNames(File folder) throws InterruptedException {
        HashMap<String, HashSet<File>> filesMap = new HashMap<>();
        for (File resultFile : folder.listFiles()) {
            if (resultFile.getName().contains("result") && resultFile.getName().contains(".xml")) {
                String outFileName = resultFile.getName().replace("result_", "");   //check result file prefix

                int yearIndex = outFileName.indexOf("__2021");
                int xmlIndex = outFileName.indexOf(".xml");

                int index = Integer.min(yearIndex, xmlIndex);
                outFileName = outFileName.substring(0, index) + ".xml";
                outFileName = outFileName.replace("_", "-");

                personsCount.put(outFileName, 0);
                uniquePersonsCount.put(outFileName, 0);
                if (filesMap.containsKey(outFileName) == false) {
                    filesMap.put(outFileName, new HashSet<>());
                }
                filesMap.get(outFileName).add(resultFile);
            }
        }
        int num = 1;
//        ExecutorService es = Executors.newFixedThreadPool(filesMap.size());
        for (String fileName : filesMap.keySet()) {
            ResultFileCounter rfc = new ResultFileCounter(fileName, filesMap.get(fileName));
            counters.add(rfc);    
            rfc.call();
            num++;
        }
        
        
        LoggingService.writeLog("num of pack's: " + num, "debug");
        for (ResultFileCounter counter : counters) {
            LoggingService.writeLog(counter.getPackName()
                    + "\t total: " + counter.getTotalResults()
                    + "\t unique: " + counter.getUniqueCount(), "debug");
        }

    }
}
