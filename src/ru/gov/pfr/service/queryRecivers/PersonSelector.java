package ru.gov.pfr.service.queryRecivers;

import java.util.LinkedHashMap;
import ru.gov.pfr.service.FRLLOSQLFactory;

public class PersonSelector implements QueryReciver {

    private String guid;
    private String FILE_ID_FILED = "FILE_ID";
    private String MONETIZATION_FILE_ID_FILED = "MFILE_ID";
    private int fileID = -1;
    private int monetizationFileID = -1;

    public PersonSelector(String guid) {
        this.guid = guid;
    }

    @Override
    public void reciveData(LinkedHashMap<String, String> resultMap, String[] fieldsMap) throws Exception {
        if (resultMap.containsKey(FILE_ID_FILED)) {
            fileID = Integer.parseInt(resultMap.get(FILE_ID_FILED));
        }
        if (resultMap.containsKey(MONETIZATION_FILE_ID_FILED)) {
            monetizationFileID = Integer.parseInt(resultMap.get(MONETIZATION_FILE_ID_FILED));
        }
    }

    @Override
    public void close() throws Exception {

    }

    public int getFileID(boolean isMonetization) {
        if (isMonetization) {
            return monetizationFileID;
        } else {
            return fileID;
        }
    }

    public String getQuery() {
        String selectPerson = FRLLOSQLFactory.selectPersonByGUIDstatment
                .replace(FRLLOSQLFactory.DB_SCHEMA_TEG, FRLLOSQLFactory.DB_SCHEMA_NAME)
                .replace(FRLLOSQLFactory.DB_GUID_TEG, guid);
        return selectPerson;
    }
}
