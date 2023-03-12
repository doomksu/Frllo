package ru.gov.pfr.fbdpReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import ru.gov.pfr.enteryPoint.EnteryPoint;
import ru.gov.pfr.service.LoggingService;

public class ExportedFileReader extends FRLLOSourceReader implements Runnable {

    private File file;
    private final String NO_FILE_ERROR = "Файл для конвертации не установлен";

    public ExportedFileReader(EnteryPoint ep) throws Exception {
        super(ep);
    }

    @Override
    protected void read() throws Exception {
        String string;
//        LoggingService.writeLog("read file: " + file.getAbsolutePath(), "debug");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "Cp1251"));
        enteryPoint.getMainController().showCount(String.valueOf(converter.getGlobalStatistics().getReadedPersonsCount()), converter.getCountInfo());
        while ((string = reader.readLine()) != null) {
            converter.parsePersonLine(string);
            if (isCanceled) {
                break;
            }
        }
        enteryPoint.getMainController().showCount(String.valueOf(converter.getGlobalStatistics().getReadedPersonsCount() + " - Завершено"),
                converter.getCountInfo());
        reader.close();
        isDone = true;
        LoggingService.writeLog("count info: ", "debug");
        LoggingService.writeLog("read done", "debug");
    }

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    protected void closeSource() {
    }

    @Override
    protected List<String> getSourcesError() throws Exception {
        ArrayList<String> errors = new ArrayList<>();
        if (file == null || !file.exists()) {
            errors.add(NO_FILE_ERROR);
        }
        return errors;
    }

}
