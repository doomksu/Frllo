package ru.gov.pfr.service.queryRecivers;

import java.util.LinkedHashMap;
import ru.gov.pfr.service.ConnectionService;
import ru.gov.pfr.service.FRLLOSQLFactory;
import static ru.gov.pfr.service.FRLLOSQLFactory.DB_FILENAME_TEG;
import static ru.gov.pfr.service.FRLLOSQLFactory.DB_SCHEMA_NAME;
import static ru.gov.pfr.service.FRLLOSQLFactory.DB_SCHEMA_TEG;
import ru.gov.pfr.service.LoggingService;

public class FileSelector implements QueryReciver {

    private String[] fileMap = {"ID", "FILENAME", "CREATION_DATE"};
    private boolean isMonetization = false;
    private int fileID = -1;
    private String fileName;

    public FileSelector(String name, boolean isMonetization) {
        this.isMonetization = isMonetization;
        fileName = name;
    }

    private void fetchFileId() throws Exception {
        String resultMessage = "";
        ConnectionService.getInstance().fetchData(selectIDByNameQuery(), this, null);
        if (fileID < 0) {
            resultMessage = "Нет ID файла-1: " + fileName;
            String originName = fileName;
            fileName = fileName.replace("closed-", "closed_").replace("open-", "open_");
            ConnectionService.getInstance().fetchData(selectIDByNameQuery(), this, null);
            if (fileID < 0) {
                resultMessage += "  Нет ID файла-2: " + fileName;
                LoggingService.writeLog(resultMessage, "debug");
                fileName = originName;
            }
        }
    }

    @Override
    public void reciveData(LinkedHashMap<String, String> resultMap, String[] fieldsMap) throws Exception {
        String strID = resultMap.get("ID");
        if (strID != null) {
            try {
                fileID = Integer.parseInt(strID);
            } catch (Exception ex) {
                fileID = -1;
            }
        }
    }

    public static synchronized String selectMaxIDQuery(boolean isMonetization) {
        String query = "";
        if (isMonetization) {
            query = FRLLOSQLFactory.selectMaxMonetizationFileIndex.replaceAll(DB_SCHEMA_TEG, DB_SCHEMA_NAME);
        } else {
            query = FRLLOSQLFactory.selectMaxLgotaFileIndex.replaceAll(DB_SCHEMA_TEG, DB_SCHEMA_NAME);
        }
        return query;
    }

    public String selectIDByNameQuery() {
        String query = "";
        if (isMonetization) {
            query = FRLLOSQLFactory.selectMonetizationFileIdByName.replaceAll(DB_SCHEMA_TEG, DB_SCHEMA_NAME).replace(DB_FILENAME_TEG, fileName);
        } else {
            query = FRLLOSQLFactory.selectLoadedFileIdByName.replaceAll(DB_SCHEMA_TEG, DB_SCHEMA_NAME).replace(DB_FILENAME_TEG, fileName);
        }
//        LoggingService.writeLog(">> selectIDByNameQuery isMonetization: " + isMonetization + "   " + query, "debug");
        return query;
    }

    public int getFileID() throws Exception {
        fetchFileId();
        return fileID;
    }

    public int getNextFileID() {
        return fileID + 1;
    }

    @Override
    public void close() throws Exception {

    }

}
