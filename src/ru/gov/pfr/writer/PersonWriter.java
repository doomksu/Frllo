package ru.gov.pfr.writer;

import ru.gov.pfr.fbdpReader.FrlloConverter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import ru.gov.pfr.personEntities.PersonDataSource;
import ru.gov.pfr.service.ConnectionService;
import ru.gov.pfr.service.LoggingService;
import ru.gov.pfr.service.SettingsService;
import ru.gov.pfr.service.XSDValidator;
import ru.gov.pfr.utils.ConverterStatistics;

public class PersonWriter {

    private int PACKAGE_LIMIT = 100000;
    private BufferedWriter lgotaWriter;
    private BufferedWriter lgotaJournalWriter;
    private BufferedWriter monitizationWriter;
    private BufferedWriter monitizationJournalWriter;

    private File lgotaFile;
    private File lgotaJournalFile;
    private File monitizationFile;
    private File monitizationJournalFile;

    private ConverterStatistics globalStatistics;
    private ConverterStatistics tempStatistics;
    private int lgotaWrited = 0;
    private int monitizationWrited = 0;

    private HashSet<String> guids;
    private HashMap<File, Boolean> checkedFiles;
    private int outFileID;
    private boolean saveToDB;
    private String filePrefix = "";
    private final FrlloConverter converter;

    public PersonWriter(FrlloConverter converter) {
        this.converter = converter;
        guids = new HashSet<>();
        checkedFiles = new HashMap<>();
        saveToDB = Boolean.parseBoolean(SettingsService.getInstance().getValue("saveConvertedInformation"));
        String folderPath = SettingsService.getInstance().getValue("outConvertedFolderPath");
        File folder = new File(folderPath);
        folder.mkdir();

        try {
            int val = Integer.parseInt(SettingsService.getInstance().getValue("maxPackagesSize"));
            PACKAGE_LIMIT = val;
        } catch (Exception ex) {
            LoggingService.writeLog("max pacakage size is not an Integer: " + SettingsService.getInstance().getValue("maxPackagesSize"), "debug");
            PACKAGE_LIMIT = 100000;
        }
    }

    public void write(PersonDataSource person) throws Exception {
        while (guids.contains(person.getGuid())) {
            guids.add(person.getGuid());
        }
        if (lgotaWriter == null) {
            recreateWriter();
        }
        if (person.isMonetization()) {
            if (monitizationWriter == null) {
                recreateMonitizationWriter();
            }
            monitizationWriter.write(person.makeXMLStructure().makeMonetizationXML());
            monitizationWriter.flush();
            monitizationWrited++;
        }
        if (saveToDB && person.isDBWriteble()) {
            ConnectionService.getInstance().writeLoadedPerson(person, String.valueOf(getFileID()));
        }
        lgotaWriter.write(person.makeXMLStructure().makeLgotaXML());
        lgotaWriter.flush();
        lgotaWrited++;
        if (lgotaWrited == PACKAGE_LIMIT) {
            LoggingService.writeLog(">> packageLimit: " + lgotaWrited + "  to file: " + lgotaFile.getAbsolutePath(), "debug");
            recreateWriter();
            LoggingService.writeLog(">> packageLimit new file: " + lgotaFile.getAbsolutePath(), "debug");
        }
        if (monitizationWrited == PACKAGE_LIMIT) {
            recreateMonitizationWriter();
        }
        converter.getGlobalStatistics().addWritedCount();
        converter.getTempStatistics().addWritedCount();
        writeNPERSJournal(person);
    }

    protected void writeNPERSJournal(PersonDataSource person) {
        String folder = SettingsService.getInstance().getValue("outConvertedFolderPath");
        if (!folder.endsWith("\\")) {
            folder += "\\";
        }
        if (lgotaFile.exists()) {
            String lgotaJournalName = "";
            String lgotaFileName = lgotaFile.getName();
            if (lgotaFileName.contains("open-") || lgotaFileName.contains("open_")) {
                lgotaJournalName += "открытая льгота СНИЛСы-";
                lgotaJournalName += lgotaFileName.replace("open-", "").replace("open_", "").replace(".xml", ".csv");
            }
            if (lgotaFileName.contains("closed-") || lgotaFileName.contains("closed_")) {
                lgotaJournalName += "закрытая льгота СНИЛСы-";
                lgotaJournalName += lgotaFileName.replace("closed-", "").replace("closed_", "").replace(".xml", ".csv");
            }
            try {
                if (lgotaJournalFile == null || !lgotaJournalName.equals(lgotaJournalFile.getName())) {
                    lgotaJournalFile = new File(folder + lgotaJournalName);
                    lgotaJournalFile.createNewFile();
                    lgotaJournalWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(lgotaJournalFile), "cp1251"));
                }
                lgotaJournalWriter.newLine();
                lgotaJournalWriter.write(person.getNPERS());
                lgotaJournalWriter.flush();
            } catch (Exception ex) {
                LoggingService.writeLog(ex);
            }
        }
        if (person.isMonetization()) {
            if (monitizationFile.exists()) {
                String monitizationJournalName = "";
                String monitizationFileName = monitizationFile.getName();
                if (monitizationFileName.contains("open-") || monitizationFileName.contains("open_")) {
                    monitizationJournalName += "открытая монетизация СНИЛСы-";
                    monitizationJournalName += monitizationFileName.replace("monitization-open-", "").replace(".xml", ".csv");
                }
                if (monitizationFileName.contains("-closed") || monitizationFileName.contains("closed_")) {
                    monitizationJournalName += "закрытая монетизация СНИЛСы-";
                    monitizationJournalName += monitizationFileName.replace("monitization-closed-", "").replace(".xml", ".csv");
                }
                try {
                    if (monitizationJournalFile == null || !monitizationJournalName.equals(monitizationJournalFile.getName())) {
                        monitizationJournalFile = new File(folder + monitizationJournalName);
                        monitizationJournalFile.createNewFile();
                        monitizationJournalWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(monitizationJournalFile), "cp1251"));
                    }
                    monitizationJournalWriter.newLine();
                    monitizationJournalWriter.write(person.getNPERS());
                    monitizationJournalWriter.flush();
                } catch (Exception ex) {
                    LoggingService.writeLog(ex);
                }
            }
        }
    }

    public void recreateWriter() throws Exception {
        String folder = SettingsService.getInstance().getValue("outConvertedFolderPath");
        if (!folder.endsWith("\\")) {
            folder += "\\";
        }
        closePrevWriter(lgotaWriter, lgotaFile);
        lgotaWrited = 0;
        lgotaFile = new File(folder + filePrefix + UUID.randomUUID().toString() + ".xml");
        checkedFiles.put(lgotaFile, Boolean.FALSE);
        if (lgotaFile.exists()) {
            lgotaFile.delete();
        }
        lgotaWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(lgotaFile), "UTF8"));
        boolean saveConvertedInformation = Boolean.parseBoolean(SettingsService.getInstance().getValue("saveConvertedInformation"));
        if (saveConvertedInformation && ConnectionService.getInstance().getConnectionFRLLO() != null) {
            outFileID = ConnectionService.getInstance().insertNewFile(lgotaFile, false);
        }
        writeHeader(lgotaWriter);
    }

    public void recreateMonitizationWriter() throws Exception {
        String folder = SettingsService.getInstance().getValue("outConvertedFolderPath");
        if (!folder.endsWith("\\")) {
            folder += "\\";
        }
        closePrevWriter(monitizationWriter, monitizationFile);
        monitizationWrited = 0;
        monitizationFile = new File(folder + "monitization-" + filePrefix + UUID.randomUUID().toString() + ".xml");
        checkedFiles.put(monitizationFile, Boolean.FALSE);
        if (monitizationFile.exists()) {
            monitizationFile.delete();
        }
        monitizationWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(monitizationFile), "UTF8"));
        writeHeader(monitizationWriter);
    }

    /**
     * Дописать подвал, провести валидацию и закрыть файл
     *
     * @param writer
     * @param file
     * @throws IOException
     */
    private void closePrevWriter(BufferedWriter writer, File file) throws IOException {
        LoggingService.writeLog("call closePrevWriter(): ", "debug");
        if (writer != null) {
            writeFooter(writer);
            writer.flush();
            writer.close();
        }
        if (file != null) {
            XSDValidator validator = new XSDValidator();
            checkedFiles.put(file, validator.checkFile(file));
        }
        guids.clear();
        writer = null;
    }

    public void closeWriters() throws Exception {
        try {
            closePrevWriter(lgotaWriter, lgotaFile);
            if (lgotaJournalWriter != null) {
                lgotaJournalWriter.close();
            }
            if (monitizationWriter != null) {
                closePrevWriter(monitizationWriter, monitizationFile);
                monitizationJournalWriter.close();
            }
            lgotaWriter = null;
            monitizationWriter = null;
        } catch (IOException ex) {
            LoggingService.writeLog(ex.getMessage(), "error");
        }
    }

    private void writeHeader(BufferedWriter writer) throws IOException {
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
        writer.write("<root>\r\n");
        writer.write("<info_sys_code>1.001</info_sys_code>\r\n");
        writer.write("<documents>\r\n");
        writer.flush();
    }

    private void writeFooter(BufferedWriter writer) throws IOException {
        if (writer != null) {
            writer.write("</documents>\r\n");
            writer.write("</root>\r\n");
            writer.flush();
        }
    }

    public void close() throws Exception {
        guids.clear();
        if (lgotaWriter != null) {
            closePrevWriter(lgotaWriter, lgotaFile);
            XSDValidator validator = new XSDValidator();
            checkedFiles.put(lgotaFile, validator.checkFile(lgotaFile));
            lgotaWrited = 0;
        }
        if (monitizationWriter != null) {
            closePrevWriter(monitizationWriter, monitizationFile);
            XSDValidator validator = new XSDValidator();
            checkedFiles.put(monitizationFile, validator.checkFile(monitizationFile));
            monitizationWrited = 0;
        }
    }

    public String getWriterInfo() {
        String mes = "\r\n";
        for (File file : checkedFiles.keySet()) {
            mes += "file: " + file.getAbsolutePath() + " is valid: " + checkedFiles.get(file) + "\r\n";
        }
        return mes;
    }

    public int getCurrentPacakgeWrited() {
        return lgotaWrited;
    }

    public int getFileID() {
        return outFileID;
    }

    public HashMap<File, Boolean> getCheckedFiles() {
        return checkedFiles;
    }

    public boolean isSaveToDB() {
        return saveToDB;
    }

    public void setSaveToDB(boolean saveToDB) {
        this.saveToDB = saveToDB;
    }

    public ConverterStatistics getGlobalStatistics() {
        return globalStatistics;
    }

    public void setGlobalStatistics(ConverterStatistics globalStatistics) {
        this.globalStatistics = globalStatistics;
    }

    public ConverterStatistics getTempStatistics() {
        return tempStatistics;
    }

    public void setTempStatistics(ConverterStatistics tempStatistics) {
        this.tempStatistics = tempStatistics;
    }

    public String getCurrentFileName() {
        String names = "";
        if (lgotaFile != null) {
            names += lgotaFile.getName();
        }
        if (monitizationFile != null) {
            names += "   " + monitizationFile.getName();
        }
        return names;
    }
    

    public void setCurrentWriterKey(String key) {
        filePrefix = key;
    }

}
