package ru.gov.pfr.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public class FRLLOSQLFactory {

    public static String insertFileStatment = "insert into <DB_SCHEMA>.FRLLO_LOAD_FILES (ID, FILENAME, CREATION_DATE) values(?,?,?)";
    public static String insertMonetizationFileStatment = "insert into <DB_SCHEMA>.FRLLO_MONETIZATION_FILES (ID, FILENAME, CREATION_DATE) values(?,?,?)";

    public static String deleteFrlloLoadsForDateStatment = "delete from <DB_SCHEMA>.FRLLO_PERSONS2 where FILE_ID "
            + "in (select id from FRLLO_LOAD_FILES where <DATE_PATTERN>)";
    public static String deleteFrlloLoadsFilesForDateStatment = "delete from <DB_SCHEMA>.FRLLO_LOAD_FILES where <DATE_PATTERN>";

    public static String selectMaxLgotaFileIndex = "select max(ID) as ID from <DB_SCHEMA>.FRLLO_LOAD_FILES";
    public static String selectMaxMonetizationFileIndex = "select max(ID) as ID from <DB_SCHEMA>.FRLLO_MONETIZATION_FILES";
    public static String getNextStatisticsIndex = "select max(ID) from <DB_SCHEMA>.FRLLO_DAILY_STATISTICS";

    public static String selectLoadedFileIdByName = "select min(id) as ID from <DB_SCHEMA>.FRLLO_LOAD_FILES where FILENAME = '<FILENAME>'";
    public static String selectMonetizationFileIdByName = "select min(id) as ID from <DB_SCHEMA>.FRLLO_MONETIZATION_FILES where FILENAME = '<FILENAME>'";

    public static String selectAllConvertedFiles = "select * from <DB_SCHEMA>.FRLLO_LOAD_FILES";
    public static String selectStatisticsMaxIDForDate = "select max(ID) from <DB_SCHEMA>.FRLLO_DAILY_STATISTICS";

    public static String insertPersonsStatment = "insert into <DB_SCHEMA>.FRLLO_PERSONS2 ";
    public static String insertConvertedPersonStatment = "insert into <DB_SCHEMA>.FRLLO_PERSONS2"
            + "(ID,NPERS,ID_NVP,GUID,FILE_ID,ISLOADED,FA,IM,OT,RDAT,"
            + "SEX,CITIZENSHIP,DOCTYPE,SERIAL,DOCNUMBER,ISSUE,"
            + "REGION,BENEFIT,RECEIVE_DATE,CANCEL_DATE,NSU,MFILE_ID,ISMLOADED) values ";

    public static String deleteOldPersonControlRecord ="delete from <DB_SCHEMA>.FRLLO_PERSONS2 where NPERS = <NPERS>";
    public static String insertStatisticsLine = "insert into <DB_SCHEMA>.FRLLO_DAILY_STATISTICS (ID,PERIOD_START,PERIOD_END) ";

    public static String insertResultErrorStatment = "insert into <DB_SCHEMA>.FRLLO_RESULTS_ERROR ";
    public static String insertFBDPErrorStatment = "insert into <DB_SCHEMA>.FRLLO_FBDP_ERRORS ";
    public static String insertLoadedFileStatment = "insert into <DB_SCHEMA>.FRLLO_LOAD_FILES ";
    public static String insertErrorCodeStatment = "insert into <DB_SCHEMA>.RESULT_ERROR_CODE ";
    public static String insertResultErrorCode = "insert into <DB_SCHEMA>.RESULT_ERROR_CODE (ID,NAME) "
            + " values ("
            + " coalesce((select max(id)+1 from <DB_SCHEMA>.RESULT_ERROR_CODE),1) , '<NAME>')";
    public static String selectErrorCodeByDescription = "select ID from <DB_SCHEMA>.RESULT_ERROR_CODE where NAME = '<NAME>'";
    public static String updateResultLoadsStatusStatment = "update <DB_SCHEMA>.FRLLO_PERSONS2 ";

    public static String updateStatisticsLine = "update <DB_SCHEMA>.FRLLO_DAILY_STATISTICS ";

    public static String selectPersonByNPERSstatment = "select ID,NPERS,ID_NVP,GUID,FILE_ID,ISLOADED,FA,IM,OT,RDAT,"
            + "SEX,CITIZENSHIP,DOCTYPE,SERIAL,DOCNUMBER,ISSUE,"
            + "REGION,BENEFIT,RECEIVE_DATE,CANCEL_DATE from <DB_SCHEMA>.FRLLO_PERSONS2 where npers = '<NPERS>'";

    public static String selectPersonByGUIDstatment = "select ID,NPERS,ID_NVP,GUID,FILE_ID,ISLOADED,FA,IM,OT,RDAT,"
            + "SEX,CITIZENSHIP,DOCTYPE,SERIAL,DOCNUMBER,ISSUE,"
            + "REGION,BENEFIT,RECEIVE_DATE,CANCEL_DATE from <DB_SCHEMA>.FRLLO_PERSONS2 where GUID = '<GUID>'";

    public static String deleteFBDPerrorsStatment = "delete from <DB_SCHEMA>.FRLLO_FBDP_ERRORS where NPERS = '<NPERS>'";
    public static String deleteFRLLOerrorsStatment = "delete from <DB_SCHEMA>.FRLLO_RESULTS_ERROR where NPERS = '<NPERS>'";

    public static String clearErrorsFBDPStatment = "delete from <DB_SCHEMA>.FRLLO_RESULTS_ERROR where NPERS = '<NPERS>'";
    public static String clearErrorsFRLLOStatment = "delete from <DB_SCHEMA>.FRLLO_RESULTS_ERROR where NPERS = '<NPERS>'";

    public static final String DB_SCHEMA_TEG = "<DB_SCHEMA>";
    public static final String DB_DATE_START_TEG = "<DATE_START>";
    public static final String DB_DATE_END_TEG = "<DATE_END>";
    public static final String DB_MAN_NPERS_TEG = "<MAN_NPERS>";
    public static final String DB_NPERS_TEG = "<NPERS>";
    public static final String DB_GUID_TEG = "<GUID>";
    public static final String DB_FILENAME_TEG = "<FILENAME>";
    //
    public static final String NPERS_START_WITH = "<NPERS_START_WITH>";
    public static String DB_SCHEMA_NAME = SettingsService.getInstance().getValue("frllo_dbSchemaName");

    static Connection createFRLLOConnection() throws SQLException {
        Connection connection = null;
        try {
            String ip = SettingsService.getInstance().getValue("frllo_ip");
            String port = SettingsService.getInstance().getValue("frllo_port");
            String name = SettingsService.getInstance().getValue("frllo_dbname");
            String login = SettingsService.getInstance().getValue("frllo_login");
            String password = SettingsService.getInstance().getValue("frllo_password");

            Class.forName("com.ibm.db2.jcc.DB2Driver").newInstance();
            connection = DriverManager.getConnection("jdbc:db2://" + ip + ":" + port + "/" + name, login, password);
            if (connection != null) {
                if (connection.isClosed() == false) {
                    return connection;
                }
            }
        } catch (Exception ex) {
            LoggingService.writeLog(ex);
        }
        return null;
    }

    static Connection createFBDPConnection() throws SQLException {
        Connection connection = null;
        try {
            String ip = SettingsService.getInstance().getValue("fbdp_ip");
            String port = SettingsService.getInstance().getValue("fbdp_port");
            String name = SettingsService.getInstance().getValue("fbdp_dbname");
            String login = SettingsService.getInstance().getValue("fbdp_login");
            String password = SettingsService.getInstance().getValue("fbdp_password");

            Class.forName("com.ibm.db2.jcc.DB2Driver").newInstance();
            connection = DriverManager.getConnection("jdbc:db2://" + ip + ":" + port + "/" + name, login, password);
            if (connection != null) {
                if (connection.isClosed() == false) {
                    return connection;
                }
            }
        } catch (Exception ex) {
            LoggingService.writeLog(ex);
        }
        return null;
    }

    static ArrayList<String> createTablesSQLList() {
        ArrayList<String> sql = new ArrayList<>();
        sql.add("CREATE TABLE <DB_SCHEMA>.FRLLO_LOAD_FILES ("
                + "     ID INTEGER NOT NULL , "
                + "     FILENAME VARCHAR(100) NOT NULL , "
                + "     CREATION_DATE VARCHAR(10) NOT NULL,"
                + "     CONSTRAINT CC1607414940120 PRIMARY KEY (ID))");

        sql.add("CREATE TABLE <DB_SCHEMA>.FRLLO_MONETIZATION_FILES ("
                + "     ID INTEGER NOT NULL , "
                + "     FILENAME VARCHAR(100) NOT NULL , "
                + "     CREATION_DATE VARCHAR(10) NOT NULL,"
                + "     CONSTRAINT CC1607224940120 PRIMARY KEY (ID))");

        sql.add("CREATE TABLE <DB_SCHEMA>.FRLLO_PERSONS2(\n"
                + "	ID VARCHAR (40) NOT NULL,\n"
                + "	NPERS VARCHAR (14) NOT NULL,\n"
                + "	ID_NVP VARCHAR (40),\n"
                + "	GUID VARCHAR (40) NOT NULL,\n"
                + "	FILE_ID INTEGER NOT NULL,\n"
                + "	ISLOADED INTEGER NOT NULL,\n"
                + "	FA VARCHAR (50),\n"
                + "	IM VARCHAR (50),\n"
                + "	OT VARCHAR (50),\n"
                + "	RDAT VARCHAR (10),\n"
                + "	SEX VARCHAR (2),\n"
                + "	CITIZENSHIP VARCHAR (4),\n"
                + "	DOCTYPE VARCHAR (5),\n"
                + "	SERIAL VARCHAR (20),\n"
                + "	DOCNUMBER VARCHAR (20),\n"
                + "	ISSUE VARCHAR (10),\n"
                + "	REGION VARCHAR (10),\n"
                + "	BENEFIT VARCHAR (4),\n"
                + "	RECEIVE_DATE VARCHAR (10),\n"
                + "	CANCEL_DATE VARCHAR (10),\n"
                + "	NSU INTEGER,\n"
                + "	MFILE_ID INTEGER,\n"
                + "	ISMLOADED INTEGER,\n"
                + "	CONSTRAINT CC1612446522869 \n"
                + "	PRIMARY KEY (GUID,npers,isloaded))");

        sql.add("CREATE TABLE <DB_SCHEMA>.FRLLO_RESULTS_ERROR (\n"
                + "	GUID VARCHAR (40) NOT NULL ,\n"
                + "	ERROR_CASE INTEGER NOT NULL ,\n"
                + "	CONVERTED_FILE_ID INTEGER NOT NULL ,\n"
                + "	RESULT_FILE VARCHAR (100) NOT NULL ,\n"
                + "	LOAD_DATE VARCHAR (10) NOT NULL ,\n"
                + "     NPERS VARCHAR (14) NOT NULL,\n"
                + "	CONSTRAINT CC1615539527000 PRIMARY KEY (GUID, ERROR_CASE, NPERS)  \n"
                + "	)");
        
        sql.add("CREATE TABLE <DB_SCHEMA>.FRLLO_MRESULTS_ERROR (\n"
                + "	GUID VARCHAR (40) NOT NULL ,\n"
                + "	ERROR_CASE INTEGER NOT NULL ,\n"
                + "	CONVERTED_FILE_ID INTEGER NOT NULL ,\n"
                + "	RESULT_FILE VARCHAR (100) NOT NULL ,\n"
                + "	LOAD_DATE VARCHAR (10) NOT NULL ,\n"
                + "     NPERS VARCHAR (14) NOT NULL, \n"
                + "	CONSTRAINT CC1615539527000 PRIMARY KEY (GUID, ERROR_CASE, NPERS)  \n"
                + "	)");
        //
        sql.add("CREATE TABLE <DB_SCHEMA>.RESULT_ERROR_CODE ("
                + "     ID INTEGER NOT NULL,"
                + "     NAME VARCHAR (300) NOT NULL,"
                + "     CONSTRAINT CC1615542347584 PRIMARY KEY (ID, NAME))");
        //
        sql.add("CREATE TABLE NATA.FRLLO_FBDP_ERRORS(\n"
                + "	NPERS VARCHAR (14) NOT NULL,\n"
                + "	ID_NVP VARCHAR (40),\n"
                + "	REGION VARCHAR (10),\n"
                + "	CDATE VARCHAR (30) NOT NULL,\n"
                + "	REASON VARCHAR (400),\n"
                + "	CONSTRAINT CC1612446544449 \n"
                + "	PRIMARY KEY (NPERS,CDATE))");
        //
        sql.add("CREATE INDEX NATA.persons_guid\n"
                + "ON  NATA.FRLLO_PERSONS2 (guid)");
        sql.add("CREATE INDEX NATA.persons_npers\n"
                + "ON  NATA.FRLLO_PERSONS2 (npers)");

        //
        sql.add("CREATE TABLE NATA.FRLLO_DAILY_STATISTICS(\n"
                + "	ID INTEGER NOT NULL,\n"
                + "	PERIOD_START VARCHAR (10) NOT NULL,\n"
                + "	PERIOD_END VARCHAR (10) NOT NULL,\n"
                + "	IS_COMPLETE INTEGER,\n"
                + "	CONVERTED INTEGER,\n"
                + "	IN_DB_SKIP INTEGER,\n"
                + "	ERRORS INTEGER,\n"
                + "	CONSTRAINT CC1613216522869 \n"
                + "	PRIMARY KEY (ID))");

        sql.add("CREATE INDEX NATA.FRLLO_STAT_ID\n"
                + "ON NATA.FRLLO_DAILY_STATISTICS (id)");

        sql.add("CREATE INDEX NATA.FRLLO_STAT_START\n"
                + "ON NATA.FRLLO_DAILY_STATISTICS (PERIOD_START)");
        sql.add("CREATE INDEX NATA.FRLLO_STAT_END\n"
                + "ON NATA.FRLLO_DAILY_STATISTICS (PERIOD_END)");

        return sql;

    }
}
