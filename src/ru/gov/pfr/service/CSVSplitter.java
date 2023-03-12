/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.gov.pfr.service;

import ru.gov.pfr.controller.MainWindowController;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import javafx.scene.control.Alert.AlertType;

/**
 *
 * @author kneretin
 */
public class CSVSplitter {

    private File csv;
    private File folder;
    private MainWindowController controller;

    public CSVSplitter(File csv, MainWindowController controller) {
        folder = new File("table_PERSONS2");
        folder.mkdir();
        this.csv = csv;
        this.controller = controller;
    }

    public void splitFile() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(csv), "cp1251"));
                    int lines = 0;
                    int partFileIndex = 1;
                    String string = "";
                    BufferedWriter writer = null;
                    while ((string = reader.readLine()) != null) {
                        if (writer == null) {
                            String newFileName = "persons_" + partFileIndex;
                            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(folder.getAbsolutePath() + "\\" + newFileName), "Cp1251"));
                            controller.showStatusInfo("создан файл: " + newFileName);
                        }
                        writer.write(string);
                        writer.newLine();
                        writer.flush();
                        lines++;
                        if (lines == 100000) {
                            writer.flush();
                            writer.close();
                            lines = 0;
                            partFileIndex++;
                            writer = null;
                        }
                    }
                    writer.flush();
                    writer.close();
                    reader.close();
                    controller.showMessage("Разделение файла таблицы PERSONS2 завершено:\r\n Файлы в папке "
                    + getFolder().getAbsolutePath(), AlertType.INFORMATION);
                    
                } catch (Exception ex) {
                    LoggingService.writeLog(ex);
                }
            }
        };
        Thread t = new Thread(r);
        t.start();
    }

    public File getFolder() {
        return folder;
    }

}
