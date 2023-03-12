package ru.gov.pfr.service.convertedReaders;

import ru.gov.pfr.controller.MainWindowController;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import ru.gov.pfr.service.ConnectionService;
import ru.gov.pfr.service.LoggingService;
import ru.gov.pfr.service.SettingsService;
import ru.gov.pfr.service.queryRecivers.PersonSelector;
import ru.gov.pfr.utils.XMLPerson;

public class ConvertedFileReader implements Callable<Boolean> {

    private File convertedFile;
    private MainWindowController controller;
    private boolean isMonetization;
    private int BUFFER_SIZE = 100;
    private volatile int fileID = -1;
    private int readedCount;

    public ConvertedFileReader(File convertedFile, MainWindowController controller) {
        this.convertedFile = convertedFile;
        this.controller = controller;

        if (convertedFile.getName().contains("monitization-")) {
            this.isMonetization = true;
        } else {
            this.isMonetization = false;
        }
        try {
            int buffer = Integer.parseInt(SettingsService.getInstance().getValue("BUFFER_SIZE"));
            BUFFER_SIZE = buffer;
        } catch (Exception ex) {
            LoggingService.writeLog("cant parse number fo buffer size: "
                    + SettingsService.getInstance().getValue("BUFFER_SIZE"), "error");
        }
        try {
            checkFile();
        } catch (Exception ex) {
            LoggingService.writeLog("error on check file: " + convertedFile.getName(), "error");
        }
    }

    @Override
    public Boolean call() throws Exception {
        readAndWriteConverted();
        return true;
    }

    public void readAndWriteConverted() throws Exception {
        readedCount = 0;
        boolean write = true;
        int bufferSize = 0;
        ArrayList<String> bufferQuery = new ArrayList<>();
        String status = LoggingService.getDateTimeString() + " Начало чтения файла ID: " + fileID + "\r\n" + convertedFile.getName();
        controller.showCount("", status);
        controller.showStatusInfo(status);

        if (fileID <= 0) {
            LoggingService.writeLog("unknoun file: " + convertedFile.getName(), "error");
            return;
        }
        String string;
        XMLPerson xmlPerson = null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(convertedFile), "utf8"));
        while ((string = reader.readLine()) != null) {
            if (xmlPerson != null) {
                xmlPerson.parseString(string);
            }
            if (string.contains("</document>")) {
                write = true;
                readedCount++;
                if (xmlPerson != null) {
                    xmlPerson.constructControllString();
                    if (isMonetization) {
                        bufferQuery.add(ConnectionService.getInstance()
                                .getUpdatePersonResultsStatment(xmlPerson.getConvertGUID(), 0, String.valueOf(fileID), isMonetization));
                    } else {
                        bufferQuery.addAll(ConnectionService.getInstance().getInsertConvertedQuery(xmlPerson, String.valueOf(fileID)));
                    }
                }
                bufferSize++;
                if (bufferSize == BUFFER_SIZE) {
                    ConnectionService.getInstance().executeInsertUpdateQuery(bufferQuery);
                    bufferSize = 0;
                    LoggingService.writeLog("bufferQuery: " + bufferQuery.get(0), "debug");
                    bufferQuery.clear();
                }
            }
            if (string.contains("<document>")) {
                xmlPerson = new XMLPerson(isMonetization);
            }
            if (readedCount % BUFFER_SIZE == 0 && write) {
                status = LoggingService.getDateTimeString() + getStatusMessage();
                controller.showCount(String.valueOf(readedCount), status);
                write = false;
            }
        }
        if (bufferSize > 0) {
            ConnectionService.getInstance().executeInsertUpdateQuery(bufferQuery);
            bufferSize = 0;
            bufferQuery = null;
        }

        String mes = LoggingService.getDateTimeString() + " Завершено чтение файла ID: " + fileID + " прочтено: " + readedCount + "\r\n" + convertedFile.getName();
        controller.showStatusInfo(mes);
        controller.showCount(String.valueOf(readedCount), mes);
        reader.close();
        convertedFile.delete();
    }

    public String getStatusMessage() {
        return " Чтение файла ID: " + fileID + " прочтено: " + readedCount + "\r\n" + convertedFile.getName();
    }

    public int readFirstPersonAndFindFileID() throws Exception {
        String string;
        XMLPerson xmlPerson = null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(convertedFile), "utf8"));
        while ((string = reader.readLine()) != null) {
            if (xmlPerson != null) {
                xmlPerson.parseString(string);
            }
            if (string.contains("</document>")) {
                PersonSelector ps = new PersonSelector(xmlPerson.getConvertGUID());
                ConnectionService.getInstance().fetchData(ps.getQuery(), ps, null);
                fileID = ps.getFileID(isMonetization);
                reader.close();
                break;
            }
            if (string.contains("<document>")) {
                xmlPerson = new XMLPerson(isMonetization);
            }
        }
        return fileID;
    }

    /**
     * Проверить есть ли в БД файл с таким именем, если нет - записать
     */
    public void checkFile() throws Exception {
        fileID = ConnectionService.getInstance().getLoadedFileId(convertedFile.getName(), isMonetization);
        if (fileID <= 0) {
            fileID = readFirstPersonAndFindFileID();
            if (fileID <= 0) {
                ConnectionService.getInstance().insertNewFile(convertedFile, isMonetization);
            }
            fileID = ConnectionService.getInstance().getLoadedFileId(convertedFile.getName(), isMonetization);
            if (fileID <= 0) {
                LoggingService.writeLog("finally file not found in DB : " + convertedFile.getName(), "error");
            }
        }
    }

    public File getFile() {
        return convertedFile;
    }

    public int getReadedCount() {
        return readedCount;
    }

}
