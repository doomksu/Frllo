package ru.gov.pfr.service;

import ru.gov.pfr.controller.MainWindowController;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class PersonDeleter {

    private String pathToFolder;
    private File folder;
    private String npers;
    private String[] patterns;
    private MainWindowController controller;
    private final String templateLong = "[0-1]{3}-[0-1]{3}-[0-1]{3} [0-1]{2}";
    private final String templateShort = "[0-1]{11}";

    public PersonDeleter(MainWindowController controller, String folderPath, String template) {
        this.pathToFolder = folderPath;
        this.controller = controller;
        this.npers = template;
    }

    public void deletePerson() {
        if (!isValidParameters()) {
            controller.showStatusInfo("Неверные параметры - удаление получателя невозможно");
            return;
        }
        startFileChecker();
    }

    private boolean isValidParameters() {
        File checkFolder = new File(pathToFolder);
        if (!checkFolder.isDirectory()) {
            controller.showStatusInfo("Неверный путь к папке с файлами");
            return false;
        }
        this.folder = checkFolder;
        if (npers == null || npers.isEmpty()) {
            controller.showStatusInfo("Пустой СНИЛС");
            return false;
        }
        if (!npers.matches(templateLong) && npers.matches(templateShort)) {
            controller.showStatusInfo("Неверный формат СНИЛС для удаления");
            return false;
        }
        patterns = new String[]{
            "<snils>" + shrinkNpers() + "</snils>",};
        return true;
    }

    private void startFileChecker() {
        controller.showStatusInfo("Запуск проверки файлов");
        for (File convertedFile : folder.listFiles()) {
            if (convertedFile.isFile()) {
                try {
                    readAndSearchPatern(convertedFile);
                } catch (Exception ex) {
                    LoggingService.writeLog(ex);
                    controller.showStatusInfo("Ошибка при обработке файла: " + convertedFile.getAbsolutePath());
                }
            }
        }
        controller.showStatusInfo("Проверка завершена");
    }

    private String shrinkNpers() {
        if (npers.matches(templateLong)) {
            npers = npers.replaceAll("-", "").replaceAll(" ", "").trim();
        }
        return npers;
    }

    private void readAndSearchPatern(File convertedFile) throws Exception {
        String string;
        ArrayList<String> buffer = new ArrayList<String>();
        String fileName = convertedFile.getName();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(convertedFile), "utf8"));
        File rewritedFile = new File(folder.getAbsolutePath() + "\\" + fileName + "-temp-");
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(rewritedFile), "utf8"));
        boolean found = false;
        boolean header = true;
        boolean deleted = false;
        XSDValidator validator = new XSDValidator();
        controller.showStatusInfo("Чтение файла: " + convertedFile.getName());
        while ((string = reader.readLine()) != null) {
            if (isMatch(string)) {
                controller.showStatusInfo("Найден СНИЛС: " + npers + " в файле: " + convertedFile.getAbsolutePath());
                found = true;
                deleted = true;
            }
            if (header) {
                writer.write(string + "\r\n");
                writer.flush();
            }
            if (!header) {
                buffer.add(string);
            }
            if (string.contains("<document>")) {
                header = false;
                if (!found) {
                    writeBuffer(buffer, writer);
                } else {
                    LoggingService.writeLog("found pattern in : " + convertedFile.getName(), "debug");
                }
                buffer.clear();
                found = false;
            }
        }
        LoggingService.writeLog("done read file: " + convertedFile.getName(), "debug");
        reader.close();
        if (!found) {
            writeBuffer(buffer, writer);
        }
        writer.flush();
        writer.close();
        if (validator.checkFile(rewritedFile)) {
            controller.showStatusInfo("Валидация пройдена успешено: " + convertedFile.getAbsolutePath());
            LoggingService.writeLog("valid file: " + rewritedFile.getAbsolutePath(), "debug");
        } else {
            controller.showStatusInfo("Невалидный файл: " + convertedFile.getAbsolutePath());
            LoggingService.writeLog("invalid file: " + rewritedFile.getAbsolutePath(), "error");
        }
        if (deleted) {

            String oldName = convertedFile.getName();
            convertedFile.delete();
            rewritedFile.renameTo(convertedFile);
        } else {
            controller.showStatusInfo("СНИЛС: " + npers + " не найден в файле: " + convertedFile.getAbsolutePath());
        }
    }

    private void writeBuffer(ArrayList<String> buffer, BufferedWriter writer) throws IOException {
        for (String string : buffer) {
            writer.write(string + "\r\n");
            writer.flush();
        }
    }

    private boolean isMatch(String str) {
        for (String pattern1 : patterns) {
            if (str.trim().contains(pattern1)) {
//                matches++;
                LoggingService.writeLog("found: " + pattern1, "debug");
                return true;
            }
        }
//        if (matches == patterns.length) {
//            return true;
//        }
        return false;
    }

}
