package ru.gov.pfr.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ru.gov.pfr.controller.MainWindowController;
import ru.gov.pfr.fbdpReader.FrlloConverter;
import ru.gov.pfr.fbdpReader.exceptions.ShowMessageAndWaitException;
import ru.gov.pfr.personEntities.PersonDataSource;
import static ru.gov.pfr.service.FRLLOSQLFactory.DB_NPERS_TEG;
import static ru.gov.pfr.service.FRLLOSQLFactory.DB_SCHEMA_NAME;
import static ru.gov.pfr.service.FRLLOSQLFactory.DB_SCHEMA_TEG;
import static ru.gov.pfr.service.FRLLOSQLFactory.deleteFrlloLoadsFilesForDateStatment;
import static ru.gov.pfr.service.FRLLOSQLFactory.deleteFrlloLoadsForDateStatment;
import static ru.gov.pfr.service.FRLLOSQLFactory.deleteOldPersonControlRecord;
import static ru.gov.pfr.service.FRLLOSQLFactory.insertConvertedPersonStatment;
import static ru.gov.pfr.service.FRLLOSQLFactory.insertErrorCodeStatment;
import static ru.gov.pfr.service.FRLLOSQLFactory.insertFBDPErrorStatment;
import static ru.gov.pfr.service.FRLLOSQLFactory.insertFileStatment;
import static ru.gov.pfr.service.FRLLOSQLFactory.insertMonetizationFileStatment;
import static ru.gov.pfr.service.FRLLOSQLFactory.insertPersonsStatment;
import static ru.gov.pfr.service.FRLLOSQLFactory.insertResultErrorCode;
import static ru.gov.pfr.service.FRLLOSQLFactory.insertResultErrorStatment;
import static ru.gov.pfr.service.FRLLOSQLFactory.selectAllConvertedFiles;
import static ru.gov.pfr.service.FRLLOSQLFactory.selectErrorCodeByDescription;
import static ru.gov.pfr.service.FRLLOSQLFactory.selectPersonByNPERSstatment;
import static ru.gov.pfr.service.FRLLOSQLFactory.updateResultLoadsStatusStatment;
import ru.gov.pfr.service.queryRecivers.FileSelector;
import ru.gov.pfr.service.queryRecivers.QueryReciver;
import ru.gov.pfr.utils.DateUtils;
import ru.gov.pfr.utils.ResultBlock;
import ru.gov.pfr.utils.XMLPerson;

public class ConnectionService {

    private boolean isCancel = false;
    private Connection connectionFRLLO;
    private Connection connectionFBDP;
    private HashMap<String, String> convertedFiles;
    private HashMap<String, String> resultErrorCodes;
    private static ConnectionService instance;
    private String dateString;

    private static final String LOG_CHANGES_CHECKER_QUERY = "LOG_CHANGES_CHECKER_QUERY";
    private static final String PRINT_CREATE_TABLE_STAMNETS = "PRINT_CREATE_TABLE_STAMNETS";

    private ConnectionService() {
        try {
            connectionFRLLO = FRLLOSQLFactory.createFRLLOConnection();
            connectionFBDP = FRLLOSQLFactory.createFBDPConnection();
        } catch (SQLException ex) {
            LoggingService.writeLog(ex);
        }
        try {
            if (connectionFRLLO != null) {
                if (connectionFRLLO.isClosed()) {
                    LoggingService.writeLog("connection is closed", "error");
                } else {
                    createTables(FRLLOSQLFactory.createTablesSQLList());
                }
            } else {
                LoggingService.writeLog("connection is null", "error");
            }
            getConvertedFiles();
        } catch (Exception ex) {
            LoggingService.writeLog(ex);
        }
        Date now = new Date();
        SimpleDateFormat dtf = new SimpleDateFormat(DateUtils.datePattern);
        dateString = dtf.format(now);
        resultErrorCodes = new HashMap<>();
    }

    private void createTables(ArrayList<String> queries) throws Exception {
        if (connectionFRLLO.isClosed() == false) {
            LoggingService.writeLog("FRLLO create basic tables", "debug");
            for (String query : queries) {
                Statement st = connectionFRLLO.createStatement();
                try {
                    query = query.replaceAll(DB_SCHEMA_TEG, DB_SCHEMA_NAME);
                    LoggingService.writeLogIfDummy(PRINT_CREATE_TABLE_STAMNETS, query, "debug");
                    st.executeUpdate(query);
                } catch (SQLException ex) {
                    if (ex.getSQLState().equals("42710") == false
                            || ex.getMessage().contains("SQLSTATE=42710") == false) {    //skip error if exists
                        throw ex;
                    } else {
                        LoggingService.writeLogIfDummy(PRINT_CREATE_TABLE_STAMNETS, "table already exists", "debug");
                    }
                }
                st.close();
            }
        } else {
            LoggingService.writeLog("connection to FRLLO closed", "error");
        }
    }

    /**
     * Получить ID для следующего файла, который будет внесен в БД
     *
     * @param isMonetization - false - файл льготы true - файл монетизации
     * @return @throws SQLException
     */
    public synchronized int getNextOutFileIndex(boolean isMonetization) throws SQLException {
        String comm = "";
        if (isMonetization) {
            comm = FRLLOSQLFactory.selectMaxMonetizationFileIndex.replaceAll(DB_SCHEMA_TEG, DB_SCHEMA_NAME);
        } else {
            comm = FRLLOSQLFactory.selectMaxLgotaFileIndex.replaceAll(DB_SCHEMA_TEG, DB_SCHEMA_NAME);
        }
        ResultSet result = connectionFRLLO.createStatement().executeQuery(comm);
        ResultSetMetaData metaData = result.getMetaData();
        String colname = metaData.getColumnName(1);
        result.next();
        int nextID = result.getInt(colname) + 1;
        result.close();
        return nextID;
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

    public static LinkedHashMap resultToMap(ResultSet result) {
        LinkedHashMap<String, String> rowResult = new LinkedHashMap();
        try {
            ResultSetMetaData metaData = result.getMetaData();
            int columns = metaData.getColumnCount();
            for (int i = 1; i <= columns; ++i) {
                String val = "";
                if (result.getObject(i) != null) {
                    val = String.valueOf(result.getObject(i)).trim();
                    if (val == null) {
                        val = "";
                    }
                }
                rowResult.put(metaData.getColumnName(i), val);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return rowResult;
    }

    public void makeRequestPart(FrlloConverter converter, String query) throws Exception {
        try {
            Statement st = connectionFBDP.createStatement();
            ResultSet result = st.executeQuery(query);
            while (result.next()) {
                converter.parsePersonDataMap(resultToMap(result), true);
            }
            st.close();
        } catch (Exception ex) {
            throw ex;
        }
    }

    public String clearFRLLO(MainWindowController controller, LocalDate start, LocalDate end) throws SQLException {
        String resultMessage = "";
        String datePattern = "CREATION_DATE>='<DATE_START>' and CREATION_DATE<='<DATE_END>'";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DateUtils.datePattern);
        String startString = start.format(dtf);
        String endString = end.format(dtf);
        String datePereiodQuery = datePattern.replace("<DATE_START>", startString)
                .replace("<DATE_END>", endString);

        Statement ps = connectionFRLLO.createStatement();
        String queryById = deleteFrlloLoadsForDateStatment.replace("<DATE_PATTERN>", datePereiodQuery)
                .replace(DB_SCHEMA_TEG, DB_SCHEMA_NAME);
        LoggingService.writeLog(">>clear query: " + queryById, "debug");
        int result = ps.executeUpdate(queryById);
        LoggingService.writeLog(">> execute 1 - result = " + result, "debug");
        resultMessage += " удалено записей получателей: " + result;
        ps.close();

        ps = connectionFRLLO.createStatement();
        String queryByFiles = deleteFrlloLoadsFilesForDateStatment.replace("<DATE_PATTERN>", datePereiodQuery)
                .replace(DB_SCHEMA_TEG, DB_SCHEMA_NAME);
        LoggingService.writeLog(">>second clear query: " + queryByFiles, "debug");
        result = ps.executeUpdate(queryByFiles);
        LoggingService.writeLog(">> execute 2 - result = " + result, "debug");
        resultMessage += " удалено записей о файлах: " + result;
        ps.close();
        return resultMessage;
    }

    public int executeUpdate(String sqlCommand) throws SQLException {
        int result = 0;
        Statement st = connectionFRLLO.createStatement();
        try {
            result = st.executeUpdate(sqlCommand);
        } catch (Exception ex) {
            if (ex.getMessage().contains("SQLCODE=-1218, SQLSTATE=57011")) {
                LoggingService.writeLog(">>hande sql : " + sqlCommand + ";", "error");
            } else {
                if (!ex.getMessage().contains(" SQLCODE=-803, SQLSTATE=23505")) {
                    throw ex;
                }
            }
        }
        st.close();
        return result;
    }

    public String getUpdatePersonResultsStatment(String guid, int isLoaded, String fileID, boolean isMonetization) throws Exception {
        String sql = "";
        if (isMonetization) {
            sql = updateResultLoadsStatusStatment
                    .replace(DB_SCHEMA_TEG, DB_SCHEMA_NAME)
                    + " set ismloaded = " + isLoaded
                    + ", mfile_id = " + fileID
                    + " where guid = '" + guid + "'";
        } else {
            sql = updateResultLoadsStatusStatment
                    .replace(DB_SCHEMA_TEG, DB_SCHEMA_NAME)
                    + " set isloaded = " + isLoaded
                    + ", file_id = " + fileID
                    + " where guid = '" + guid + "'";
        }
        return sql;
    }

    public String getInsertErrorLineStatment(ResultBlock block, String convertedFileID, String resultName) throws SQLException {
        String sql = insertResultErrorStatment
                .replace(DB_SCHEMA_TEG, DB_SCHEMA_NAME)
                + " (GUID, ERROR_CASE, CONVERTED_FILE_ID, RESULT_FILE, LOAD_DATE) values ("
                + "'" + block.getId() + "', "
                + getErrorCodeID(block.getDescription()) + ", "
                + convertedFileID + ", "
                + "'" + resultName + "', "
                + "'" + dateString + "')";
        return sql;
    }

    public int insertErrorLine(ResultBlock block, String convertedFileID, String resultName) throws Exception {
        String sql = insertResultErrorStatment
                .replace(DB_SCHEMA_TEG, DB_SCHEMA_NAME)
                + " (GUID, ERROR_CASE, CONVERTED_FILE_ID, RESULT_FILE, LOAD_DATE) values ("
                + "'" + block.getId() + "', "
                + getErrorCodeID(block.getDescription()) + ", "
                + convertedFileID + ", "
                + "'" + resultName + "', "
                + "'" + dateString + "')";
        return executeInsertUpdateQuery(sql);
    }

    public int writeLoadedPerson(PersonDataSource person, String fileID) throws Exception {
        if (person.isDBWriteble()) {
            return executeInsertUpdateQuery(getInsertConvertedQuery(person, fileID));
        }
        return 0;
    }

    /**
     * Сформировать запрос на создание контрольной записи
     *
     * @param xmlPerson
     * @param fileID
     * @return
     */
    public List<String> getInsertConvertedQuery(XMLPerson xmlPerson, String fileID) {;
        ArrayList<String> queue = new ArrayList<>();
        queue.add(deleteOldPersonControlRecord
                .replace(DB_SCHEMA_TEG, DB_SCHEMA_NAME)
                .replace(DB_NPERS_TEG, xmlPerson.getSnils()));
        queue.add(insertConvertedPersonStatment.replace(DB_SCHEMA_TEG, DB_SCHEMA_NAME)
                + xmlPerson.getFullValues(fileID));
        return queue;
    }

    /**
     * Сформировать запрос на создание контрольной записи
     *
     * @param xmlPerson
     * @param fileID
     * @return
     */
    private List<String> getInsertConvertedQuery(PersonDataSource person, String fileID) {
        ArrayList<String> queue = new ArrayList<>();
        queue.add(deleteOldPersonControlRecord
                .replace(DB_SCHEMA_TEG, DB_SCHEMA_NAME)
                .replace(DB_NPERS_TEG, person.getNPERS()));
        queue.add(insertConvertedPersonStatment
                .replace(DB_SCHEMA_TEG, DB_SCHEMA_NAME)
                + person.sqlValuesLine(fileID));
        return queue;
    }

    public int executeInsertUpdateQuery(List<String> queryes) throws Exception {
        int result = queryes.size();
        if (connectionFRLLO != null) {
            Statement st = connectionFRLLO.createStatement();
            for (String sql : queryes) {
                st.addBatch(sql);
            }
            try {
                st.executeBatch();
                st.close();
            } catch (Exception ex) {
                if (ex.getMessage().contains("SQLCODE=-1218, SQLSTATE=57011")) {
                    String err = "";
                    for (String string : queryes) {
                        err += string + ";\r\n ";
                    }
                    LoggingService.writeLog(">>handle sql : " + err + ";", "error");
                }
            }
        }
        return result;
    }

    public int executeInsertUpdateQuery(String sql) throws Exception {
        int result = 0;
        if (connectionFRLLO != null) {
            Statement st = connectionFRLLO.createStatement();
            st.addBatch(sql);
            try {
                result = st.executeUpdate(sql);
                Thread.sleep(2);
                st.close();
            } catch (Exception ex) {
                if (ex.getMessage().contains("SQLCODE=-1218, SQLSTATE=57011")) {
                    LoggingService.writeLog(">>handle sql : " + sql + ";", "error");
                } else {
                    if (!ex.getMessage().contains(" SQLCODE=-803, SQLSTATE=23505")) {
                        LoggingService.writeLog("error on query: " + sql, "error");
                        throw ex;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Получить список файлов сконвертированных программой и загруженных в БД
     *
     * @throws SQLException
     */
    private void getConvertedFiles() throws SQLException {
        if (connectionFRLLO != null) {
            Statement st = connectionFRLLO.createStatement();
            convertedFiles = new HashMap<>();
            ResultSet result = st.executeQuery(selectAllConvertedFiles.replace(DB_SCHEMA_TEG, DB_SCHEMA_NAME));
            while (result.next()) {
                if (isCancel == true) {
                    break;
                }
                LinkedHashMap<String, String> map = resultToMap(result);
                if (map.containsKey("FILENAME") && map.containsKey("ID")) {
                    convertedFiles.put(map.get("FILENAME"), map.get("ID"));
                }
            }
        }
    }

    public void setCanceled(boolean b) {
        isCancel = true;
    }

    public static ConnectionService getInstance() {
        if (instance == null) {
            instance = new ConnectionService();
        }
        return instance;
    }

    public void reconnectFRLLO() throws SQLException {
        if (connectionFRLLO != null) {
            connectionFRLLO.close();
        }
        connectionFRLLO = FRLLOSQLFactory.createFRLLOConnection();
    }

    public void reconnectFBDP() throws SQLException {
        if (connectionFBDP != null) {
            connectionFBDP.close();
        }
        connectionFBDP = FRLLOSQLFactory.createFBDPConnection();
    }

    public Connection getConnectionFRLLO() {
        return connectionFRLLO;
    }

    public Connection getConnectionFBDP() {
        return connectionFBDP;
    }

    public void closeConnections() throws SQLException {
        LoggingService.writeLog("close connections", "debug");
        connectionFRLLO.close();
        connectionFBDP.close();
    }

    public String getErrorCodeID(String description) throws SQLException {
        if (resultErrorCodes.containsKey(description)) {
            return resultErrorCodes.get(description);
        } else {
            String resultCode = String.valueOf(selectErrorCodeId(description));
            if (resultCode.equals("-1")) {
                insertErrorCode(description);
                resultCode = String.valueOf(selectErrorCodeId(description));
            }
            resultErrorCodes.put(description, resultCode);
            return resultCode;
        }
    }

    private String selectErrorCodeId(String description) throws SQLException {
        String index = "-1";
        String comm = selectErrorCodeByDescription
                .replace(DB_SCHEMA_TEG, DB_SCHEMA_NAME)
                .replace("<NAME>", description);
        if (connectionFRLLO != null) {
            Statement st = connectionFRLLO.createStatement();
            ResultSet result = st.executeQuery(comm);
            ResultSetMetaData metaData = result.getMetaData();
            String colname = metaData.getColumnName(1);
            result.next();
            if (result != null && result.isClosed() == false) {
                index = String.valueOf(result.getInt(colname));
            }
            st.close();
        }
        return index;
    }

    private void insertErrorCode(String description) throws SQLException {
        String queue = insertResultErrorCode
                .replace(DB_SCHEMA_TEG, DB_SCHEMA_NAME)
                .replace("<NAME>", description);
        executeUpdate(queue);
    }

    public int insertErrorCodeFromLine(String line) throws Exception {
        String sql = insertErrorCodeStatment
                .replace(DB_SCHEMA_TEG, DB_SCHEMA_NAME)
                + " (ID, NAME) values (" + line + ")";
        return executeInsertUpdateQuery(sql);
    }

    public String getInsertPersonFromLineQuery(String line) throws Exception {
        String sql = insertPersonsStatment
                .replace(DB_SCHEMA_TEG, DB_SCHEMA_NAME);
        String add = BackupPersonLineQueryMaker.prepareSQL(line);
        if (add != null) {
            return sql + add;
        }
        return "";
    }

    public int insertResultErrorFromLine(String line) throws Exception {
        String sql = insertResultErrorStatment
                .replace(DB_SCHEMA_TEG, DB_SCHEMA_NAME)
                + " (GUID, ERROR_CASE, CONVERTED_FILE_ID, RESULT_FILE, LOAD_DATE, NPERS) values (" + line + ")";
        return executeInsertUpdateQuery(sql);
    }

    /**
     * Внести новый файл в БД и вернуть его ID
     *
     */
    public synchronized int insertNewFile(File outFile, boolean isMonetization) throws Exception {
        Date d = new Date(outFile.lastModified());
        SimpleDateFormat dtf = new SimpleDateFormat(DateUtils.datePattern);
        String dateString = dtf.format(d);

        FileSelector fs = new FileSelector(outFile.getName(), isMonetization);
        fetchData(FileSelector.selectMaxIDQuery(isMonetization), fs, null);
        int index = fs.getNextFileID();

        String replVal = "values("
                + String.valueOf(index)
                + ", '"
                + outFile.getName()
                + "', '"
                + dateString
                + "') ";
        String statment = "";

        if (isMonetization) {
            statment = insertMonetizationFileStatment.replace(DB_SCHEMA_TEG, DB_SCHEMA_NAME).replace("values(?,?,?)", replVal);
        } else {
            statment = insertFileStatment.replace(DB_SCHEMA_TEG, DB_SCHEMA_NAME).replace("values(?,?,?)", replVal);
        }
        LoggingService.writeLog("write file: " + statment, "debug");
        connectionFRLLO.createStatement().executeUpdate(statment);
        connectionFRLLO.commit();
        getConvertedFiles();
        return index;
    }

    public int getLoadedFileId(String outName, boolean isMonetization) throws Exception {
        FileSelector fs = new FileSelector(outName, isMonetization);
        try {
            fetchData(fs.selectIDByNameQuery(), fs, null);
            return fs.getFileID();
        } catch (Exception ex) {
            LoggingService.writeLog("error while execute: " + fs.selectIDByNameQuery(), "error ");
            LoggingService.writeLog(ex);
        }
        return -1;
    }

    /**
     * Получить записи из контрольной таблицы по СНИЛС
     *
     * @param npers
     * @return
     * @throws Exception
     */
    public ResultSet selectConvertedPersonMap(String npers) throws Exception {
        String query = selectPersonByNPERSstatment.replaceAll(DB_SCHEMA_TEG, DB_SCHEMA_NAME).replaceAll("<NPERS>", npers);
        query += " and isloaded is not null and isloaded > 0 order by FILE_ID desc";
        LoggingService.writeLogIfDummy(LOG_CHANGES_CHECKER_QUERY, "query: " + query, "debug");
        Statement st = connectionFRLLO.createStatement();
        ResultSet result = st.executeQuery(query);
        return result;
    }

    public String getInsertFBDPErrorQuery(String npers, String nvpID, String region, String date, String why) {
        String sql = insertFBDPErrorStatment
                .replace(DB_SCHEMA_TEG, DB_SCHEMA_NAME)
                + " (NPERS, ID_NVP, REGION, CDATE, REASON) values ("
                + "'" + npers + "',"
                + "'" + nvpID + "',"
                + "'" + region + "',"
                + "'" + date + "',"
                + "'" + why + "')";
        return sql;
    }

    public int createStatisticsLine(String start, String end) throws Exception {
        String comm = FRLLOSQLFactory.getNextStatisticsIndex.replaceAll(DB_SCHEMA_TEG, DB_SCHEMA_NAME);
        Statement st = connectionFRLLO.createStatement();
        ResultSet result = st.executeQuery(comm);
        ResultSetMetaData metaData = result.getMetaData();
        int index = 1;
        String colname = metaData.getColumnName(1);
        result.next();
        if (result != null && result.isClosed() == false && metaData != null) {
            if (colname != null) {
                index = result.getInt(colname) + 1;
                if (result.wasNull()) {
                    index = 1;
                }
            }
        }
        st.close();
        comm = FRLLOSQLFactory.insertStatisticsLine.replaceAll(DB_SCHEMA_TEG, DB_SCHEMA_NAME);
        comm += " values (" + index + ", '" + start + "', '" + end + "')";
        st = connectionFRLLO.createStatement();
        st.executeUpdate(comm);
        st.close();
        return index;
    }

    public void updateStatistics(int statisticsIndex, int writedCount, int totalDBSkiped, int totalSkiped) throws Exception {
        String comm = FRLLOSQLFactory.updateStatisticsLine.replaceAll(DB_SCHEMA_TEG, DB_SCHEMA_NAME);
        try {
            comm += " set IS_COMPLETE = 1,"
                    + " CONVERTED = " + writedCount + ","
                    + " IN_DB_SKIP = " + totalDBSkiped + ","
                    + " ERRORS = " + totalSkiped
                    + " where ID = " + statisticsIndex;
            Statement st = connectionFRLLO.createStatement();
            st.executeUpdate(comm);
            st.close();
        } catch (Exception ex) {
            LoggingService.writeLog("exception sql: " + comm, "debug");
            throw ex;
        }
    }

    public boolean hasStatisticsMaxIDForDate(String dateEnd) throws Exception {
        String index = "-1";
        try {
            if (connectionFRLLO == null) {
                throw new ShowMessageAndWaitException("Нет соединения с БД ФРЛЛО");
            }
            Statement st = connectionFRLLO.createStatement();
            String comm = FRLLOSQLFactory.selectStatisticsMaxIDForDate.replaceAll(DB_SCHEMA_TEG, DB_SCHEMA_NAME);
            comm += " where PERIOD_END = '" + dateEnd + "' and IS_COMPLETE = 1";
            ResultSet result = st.executeQuery(comm);
            ResultSetMetaData metaData = result.getMetaData();
            String colname = metaData.getColumnName(1);
            result.next();

            if (result != null && result.isClosed() == false && metaData != null) {
                if (colname != null) {
                    index = String.valueOf(result.getInt(colname));
                    if (result.wasNull()) {
                        index = "-1";
                    }
                }
            }
            st.close();
        } catch (SQLException ex) {
            if (ex.getMessage().contains("Соединение закрыто") || ex.getMessage().contains("SQLSTATE=08003")) {
                throw new ShowMessageAndWaitException(ex.getMessage());
            } else {
                throw ex;
            }
        }
        if (!index.equals("-1")) {
            return true;
        }
        return false;
    }

    public void fetchData(String query, QueryReciver reciver, String[] parseBy) throws Exception {
        Statement st = connectionFBDP.createStatement();
        ResultSet result = st.executeQuery(query);
        while (result.next()) {
            reciver.reciveData(resultToMap(result), parseBy);
        }
        reciver.close();
        st.close();
    }

    public int executeUpdateStandart(String query, HashMap<String, String> vals) throws SQLException {
        int result = -1;
        Statement st = connectionFBDP.createStatement();
        String prepQuery = query.replaceAll(DB_SCHEMA_TEG, DB_SCHEMA_NAME);
        for (Map.Entry<String, String> entry : vals.entrySet()) {
            query.replaceAll(entry.getKey(), entry.getValue());
        }
        st.executeUpdate(prepQuery);
        st.close();
        return result;
    }

}
