package ru.gov.pfr.writer;

import ru.gov.pfr.fbdpReader.FrlloConverter;
import java.io.BufferedWriter;
import java.io.File;
import ru.gov.pfr.service.LoggingService;
import ru.gov.pfr.service.SettingsService;

public class PersonWriterFile {

    private int PACKAGE_LIMIT = 100000;
    private String id;
    private BufferedWriter writer;
    private BufferedWriter journalWriter;
    private File file;
    private File journalFile;
    private int writed = 0;
    private boolean isClosedWriter = false;
    private boolean isMonetisation = false;
    private final FrlloConverter converter;

    public PersonWriterFile(FrlloConverter converter) {
        this.converter = converter;
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

}
