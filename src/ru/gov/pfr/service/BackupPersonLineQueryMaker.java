package ru.gov.pfr.service;

class BackupPersonLineQueryMaker {

    static String prepareSQL(String string) {
        String query = " (ID,NPERS,ID_NVP,GUID,FILE_ID,ISLOADED,"
                + "FA,IM,OT,RDAT,SEX,CITIZENSHIP,DOCTYPE,SERIAL,DOCNUMBER,"
                + "ISSUE,REGION,BENEFIT,RECEIVE_DATE,CANCEL_DATE,NSU,MFILE_ID,ISMLOADED) values (";
        String vals = "";
        String[] parts = makeVals(string);
//        LoggingService.writeLog("len: " + parts.length + ": " + string, "debug");
//        if (parts.length >= 21) {
        vals = "'" + parts[0] + "',"
                + "'" + parts[1] + "',"
                + "'" + parts[2] + "',"
                + "'" + parts[3] + "',"//guid
                + parts[4] + ","
                + parts[5] + ","
                + "'" + parts[6] + "',"
                + "'" + parts[7] + "',"
                + "'" + parts[8] + "',"//fio
                + "'" + parts[9] + "',"//rdat
                + "'" + parts[10] + "',"//sex
                + "'" + parts[11] + "',"//citizen
                + "'" + parts[12] + "',"//doct
                + "'" + parts[13] + "',"//ser
                + "'" + parts[14] + "',"//num
                + "'" + parts[15] + "',"//issue
                + "'" + parts[16] + "',"//region
                + "'" + parts[17] + "',"//benefit
                + "'" + parts[18] + "',"//receive_date
                + "'" + parts[19] + "',"//cancel_date
                + parts[20] + ","//nsu
                + (parts[21].isEmpty() ? "-1" : parts[21]) + ","//mfile_id
                + (parts[22].isEmpty() ? "0" : parts[22]) + ")"//ISMLOADED
                ;
//            if (parts.length == 20) {
//                vals += ",,)";//mfile_id, ISMLOADED
//            }
//            if (parts.length == 21) {
//                vals += parts[20] + ",)";  //ISMLOADED
//            }
//            if (parts.length == 22) {
//                vals += parts[20] + "," + parts[21] + ")";
//            }
//        LoggingService.writeLog(">> " + query, "debug");
//        LoggingService.writeLog(">> " + vals, "debug");
        return query + vals;
    }

    private static String[] makeVals(String str) {
        String[] parts = new String[23];
        int index = 0;
        while ((str.contains(",") || str.length() > 0) && index < parts.length) {
            if (str.contains(",")) {
                String sub = str.substring(0, str.indexOf(","));
                parts[index] = sub;
                str = str.substring(str.indexOf(",") + 1);
                index++;
            } else {
                parts[index] = str;
                break;
            }
        }
        for (int i = 0; i < parts.length; i++) {
            if (parts[i] == null) {
                parts[i] = "";
            }
        }
        return parts;
    }

}
