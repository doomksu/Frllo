package ru.gov.pfr.controller;

import ru.gov.pfr.enteryPoint.EnteryPoint;
import ru.gov.pfr.fbdpReader.FBDPBaseReader;
import ru.gov.pfr.fbdpReader.FrlloConverter;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import ru.gov.pfr.service.ConnectionService;
import ru.gov.pfr.service.ConvertedLoaderService;
import ru.gov.pfr.service.LoggingService;
import ru.gov.pfr.service.PersonDeleter;
import ru.gov.pfr.service.SchedulerService;
import ru.gov.pfr.service.SettingsService;
import ru.gov.pfr.service.StatisticsService;
import ru.gov.pfr.service.queryRecivers.FBDPerrorsWriter;
import ru.gov.pfr.service.queryRecivers.FRLLOErrorsClearer;
import ru.gov.pfr.service.queryRecivers.FRLLOerrorsWriter;
import ru.gov.pfr.utils.AutoThrowStack;
import ru.gov.pfr.utils.DateUtils;

public class MainWindowController extends CommonController {

    @FXML
    private Pane schedulerButtonFlagPane;
    @FXML
    private Label countLabel, lastCheckedLabel, versionLabel;
    @FXML
    private Button schedulerButton;

    @FXML
    private DatePicker startDateDatePicker, endDateDatePicker, startClearDateDatePicker, endClearDateDatePicker, lgotaMinEndDate;
    @FXML
    private TextField frlloHostField, frlloNameField, frlloPortField, frlloLoginField, frlloPasswordField, frlloSchemaField;
    @FXML
    private TextField dbFBDPHostField, dbFBDPNameField, dbFBDPPortField, dbFBDPLoginField, dbFBDPPasswordField, dbFBDPSchemaField;
    @FXML
    private TextArea infoTextArea, statusInfoTextArea;
    @FXML
    private TextField pathToConverted, pathToResults, outConvertedFolderPath;
    @FXML
    private TextField schedeulerStartTime, schedulerSleepInterval;
    @FXML
    private TextField filesToDeletePersonTextField, sniltToDeleteTextField;
    @FXML
    private CheckBox foreignBirthDocumentCheckBox, saveConvertedInformationCheckBox, ussrPasportAsDifferentCheckBox, autoStartSchedulerCheckBox;
    @FXML
    private ToggleGroup group;
    @FXML
    private RadioButton rb_writeAll, rb_writeOpen, rb_writeClosed;
    @FXML
    private Pane frlloConnectionIndicator, fbdpConnectionIndicator;
    @FXML
    private TextField maxPackagesSize;

    private AutoThrowStack messagesStack;
    private String homeFolderPathString;

    @Override
    public void setEnteryPoint(EnteryPoint epoint) {
        super.setEnteryPoint(epoint);
        frlloHostField.setText(SettingsService.getInstance().getValue("frllo_ip"));
        frlloNameField.setText(SettingsService.getInstance().getValue("frllo_dbname"));
        frlloPortField.setText(SettingsService.getInstance().getValue("frllo_port"));
        frlloLoginField.setText(SettingsService.getInstance().getValue("frllo_login"));
        frlloPasswordField.setText(SettingsService.getInstance().getValue("frllo_password"));
        frlloSchemaField.setText(SettingsService.getInstance().getValue("frllo_dbSchemaName"));
        //FBDP_fields
        dbFBDPHostField.setText(SettingsService.getInstance().getValue("fbdp_ip"));
        dbFBDPNameField.setText(SettingsService.getInstance().getValue("fbdp_dbname"));
        dbFBDPPortField.setText(SettingsService.getInstance().getValue("fbdp_port"));
        dbFBDPLoginField.setText(SettingsService.getInstance().getValue("fbdp_login"));
        dbFBDPPasswordField.setText(SettingsService.getInstance().getValue("fbdp_password"));
        dbFBDPSchemaField.setText(SettingsService.getInstance().getValue("fbdp_dbSchemaName"));

        boolean foreign = Boolean.valueOf(SettingsService.getInstance().getValue("foreignBirthDocument"));
        boolean ussrPassport = Boolean.valueOf(SettingsService.getInstance().getValue("ussrPasportAsDifferent"));
        boolean autoStartScheduler = Boolean.valueOf(SettingsService.getInstance().getValue("autoStartScheduler"));
        boolean doSave = Boolean.valueOf(SettingsService.getInstance().getValue("saveConvertedInformation"));

        pathToResults.setText(SettingsService.getInstance().getValue("resultsPath"));
        pathToConverted.setText(SettingsService.getInstance().getValue("loadedPath"));
        outConvertedFolderPath.setText(SettingsService.getInstance().getValue("outConvertedFolderPath"));

        schedeulerStartTime.setText(SettingsService.getInstance().getValue("schedulerStartTime"));
        schedulerSleepInterval.setText(SettingsService.getInstance().getValue("schedulerSleepInterval"));
        maxPackagesSize.setText(SettingsService.getInstance().getValue("maxPackagesSize"));

        try {
            LocalDate minCloseDate = LocalDate.parse(SettingsService.getInstance().getValue("minimum_lgota_cancel_date"));
            lgotaMinEndDate.setValue(minCloseDate);
        } catch (Exception ex) {
            LoggingService.writeLog(ex);
        }

        foreignBirthDocumentCheckBox.setSelected(foreign);
        saveConvertedInformationCheckBox.setSelected(doSave);
        ussrPasportAsDifferentCheckBox.setSelected(ussrPassport);
        autoStartSchedulerCheckBox.setSelected(autoStartScheduler);
        refreshServerPanes();
        messagesStack = new AutoThrowStack(50);
        File tempFile = new File("");
        homeFolderPathString = tempFile.getAbsolutePath().replace("\\.", "");
        versionLabel.setText("версия: " + EnteryPoint.version);
        schedulerButtonFlagPane.getStyleClass().add("green_button");

        schedeulerStartTime.textProperty().addListener((observable, oldValue, newValue) -> { //проверка паттерна заполнения времени запуска планировщика 
            if (SchedulerService.isStartTimePatternValid(newValue)) {
                schedeulerStartTime.getStyleClass().removeAll("invalid");
            } else {
                schedeulerStartTime.getStyleClass().add("invalid");
            }
        });
        schedulerSleepInterval.textProperty().addListener((observable, oldValue, newValue) -> {
            if (SchedulerService.isSleepIntervalPatternValid(newValue)) {
                schedulerSleepInterval.getStyleClass().removeAll("invalid");
            } else {
                schedulerSleepInterval.getStyleClass().add("invalid");
            }
        });
        outConvertedFolderPath.textProperty().addListener((observable, oldValue, newValue) -> {
            File folder = new File(newValue);
            if (folder.isDirectory()) {
                outConvertedFolderPath.getStyleClass().removeAll("invalid");
            } else {
                outConvertedFolderPath.getStyleClass().add("invalid");
            }
        });
        pathToConverted.textProperty().addListener((observable, oldValue, newValue) -> {
            File folder = new File(newValue);
            if (folder.isDirectory()) {
                pathToConverted.getStyleClass().removeAll("invalid");
            } else {
                pathToConverted.getStyleClass().add("invalid");
            }
        });
        pathToResults.textProperty().addListener((observable, oldValue, newValue) -> {
            File folder = new File(newValue);
            if (folder.isDirectory()) {
                pathToResults.getStyleClass().removeAll("invalid");
            } else {
                pathToResults.getStyleClass().add("invalid");
            }
        });
        maxPackagesSize.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.matches("\\d[0-9]*")) {
                maxPackagesSize.getStyleClass().removeAll("invalid");
                try {
                    int intval = Integer.parseInt(newValue);
                } catch (Exception ex) {
                    maxPackagesSize.getStyleClass().add("invalid");
                }
            } else {
                maxPackagesSize.getStyleClass().add("invalid");
            }
        });

        group = new ToggleGroup();
        rb_writeAll.setToggleGroup(group);
        rb_writeAll.setUserData(new RadioButtonId("writeAll"));
        rb_writeOpen.setToggleGroup(group);
        rb_writeOpen.setUserData(new RadioButtonId("writeOpen"));
        rb_writeClosed.setToggleGroup(group);
        rb_writeClosed.setUserData(new RadioButtonId("writeClosed"));
        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                String id = group.getSelectedToggle().getUserData().toString();
                SettingsService.getInstance().setValueFromString("writeFilter:" + id);
                SettingsService.getInstance().saveSettings();
            }
        });
        String filter = SettingsService.getInstance().getValue("writeFilter");
        switch (filter) {
            case "writeAll":
                rb_writeAll.setSelected(true);
                break;
            case "writeOpen":
                rb_writeOpen.setSelected(true);
                break;
            case "writeClosed":
                rb_writeClosed.setSelected(true);
                break;
        }
        if (autoStartScheduler) {
            switchScheduledServiceAction();
        }
    }

    @FXML
    public void openSelectFileDialog() {
        FileChooser mergedFileChooser = new FileChooser();
        mergedFileChooser.setTitle("выбрать файл");
        File homeFolder = new File(homeFolderPathString);
        mergedFileChooser.setInitialDirectory(homeFolder);
        File selectedFile = mergedFileChooser.showOpenDialog(scene.getWindow());
        if (selectedFile != null) {
            if (selectedFile.isFile() == false) {
                showErrorMessage("Ошибка - необходимо выбрать файл");
            } else {
                try {
                    enteryPoint.startConvertationByFile(selectedFile);
                } catch (Exception ex) {
                    LoggingService.writeLog(ex);
                    showErrorMessage(ex.getMessage());
                }
            }
        }
    }

    @FXML
    public void saveSettingsButtonAction() {
        if (saveSettings()) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Готово");
                    alert.setHeaderText("Готово");
                    alert.setContentText("Установки сохранены");
                    alert.show();
                }
            });
        }
    }

    public boolean saveSettings() {
        resetFRLLODbSettings();
        resetFBDPDbSettings();
        String foreignBirthDocument = foreignBirthDocumentCheckBox.isSelected() ? "true" : "false";
        String ussrPasportAsDifferent = ussrPasportAsDifferentCheckBox.isSelected() ? "true" : "false";
        String saveConvertedInformation = saveConvertedInformationCheckBox.isSelected() ? "true" : "false";
        String autoStartScheduler = autoStartSchedulerCheckBox.isSelected() ? "true" : "false";

        SettingsService.getInstance().setValueFromString("foreignBirthDocument:" + foreignBirthDocument);
        SettingsService.getInstance().setValueFromString("ussrPasportAsDifferent:" + ussrPasportAsDifferent);
        SettingsService.getInstance().setValueFromString("saveConvertedInformation:" + saveConvertedInformation);
        SettingsService.getInstance().setValueFromString("autoStartScheduler:" + autoStartScheduler);
        if (SchedulerService.isStartTimePatternValid(schedeulerStartTime.getText())) {
            SettingsService.getInstance().setValueFromString("schedulerStartTime:" + schedeulerStartTime.getText());
        }
        try {
            String val = maxPackagesSize.getText();
            int intVal = Integer.parseInt(val);
            SettingsService.getInstance().setValueFromString("maxPackagesSize:" + intVal);
        } catch (Exception ex) {
            LoggingService.writeLog("not Integer: " + maxPackagesSize.getText(), "error");
        }

        if (SchedulerService.isSleepIntervalPatternValid(schedulerSleepInterval.getText())) {
            SettingsService.getInstance().setValueFromString("schedulerSleepInterval:" + schedulerSleepInterval.getText());
        }
        SettingsService.getInstance().setValueFromString("outConvertedFolderPath:" + outConvertedFolderPath.getText());
        if (!lgotaMinEndDate.getValue().toString().isEmpty()) {
            String val = lgotaMinEndDate.getValue().format(DateTimeFormatter.ofPattern(DateUtils.datePattern));
            SettingsService.getInstance().setValueFromString("minimum_lgota_cancel_date:" + val);
        }

        SettingsService.getInstance().saveSettings();
        return true;
    }

    @FXML
    public void reconnectFBDPButtonAction() {
        saveSettings();
        try {
            ConnectionService.getInstance().reconnectFBDP();
            if (ConnectionService.getInstance().getConnectionFBDP().isClosed()) {
                showErrorMessage("Соединение с БД ФБДП закрыто");
            } else {
                showMessage("Соединение с БД ФБДП открыто", Alert.AlertType.INFORMATION);
            }
        } catch (Exception ex) {
            LoggingService.writeLog(ex);
            showErrorMessage("Ошибка при попытке установить соединение с БД ФБДП");
        }
        try {
            ConnectionService.getInstance().reconnectFRLLO();
            if (ConnectionService.getInstance().getConnectionFRLLO().isClosed()) {
                showErrorMessage("Соединение с БД ФРЛЛО закрыто");
            } else {
                showMessage("Соединение с БД ФРЛЛО открыто", Alert.AlertType.INFORMATION);
            }
        } catch (Exception ex) {
            LoggingService.writeLog(ex);
            showErrorMessage("Ошибка при попытке установить соединение БД ФРЛЛО");
        }
        refreshServerPanes();
    }

    @FXML
    public void reconnectFRLLOButtonAction() {
        try {
            resetFRLLODbSettings();
            ConnectionService.getInstance().reconnectFRLLO();
            if (ConnectionService.getInstance().getConnectionFRLLO().isClosed()) {
                showErrorMessage("Соединение с БД ФРЛЛО закрыто");
            } else {
                showMessage("Соединение с БД ФРЛЛО открыто", Alert.AlertType.INFORMATION);
            }
        } catch (SQLException ex) {
            LoggingService.writeLog(ex);
            showErrorMessage("Ошибка при попытке установить соединение БД ФРЛЛО");
        }
        refreshServerPanes();
    }

    @FXML
    public void closeApp() {
        enteryPoint.exit();
    }

    @FXML
    public void switchScheduledServiceAction() {
        enteryPoint.switchScheduler();
    }

    @FXML
    public void handle_requestByDates() {
        if (!enteryPoint.isWorkInProgress()) {
            if (checkDatePickers()) {
                LocalDate start = startDateDatePicker.getValue();
                LocalDate end = endDateDatePicker.getValue();
                LoggingService.writeLog("Запрос в интервале дат: с " + start + " по " + end + "\r\n", "debug");
                try {
                    enteryPoint.startPeriodConversation(start, end);
                } catch (Exception ex) {
                    LoggingService.writeLog(ex);
                    showErrorMessage(ex.getMessage());
                }
            } else {
                LoggingService.writeLog("Указан неправильный диапазон дат: "
                        + startDateDatePicker.getValue()
                        + " - "
                        + endDateDatePicker.getValue(), "error");
                showMessage("Указан неправильный диапазон дат", Alert.AlertType.ERROR);
            }
        } else {
            LoggingService.writeLog("entery point is  busy", "debug");
            showMessage("Система занята", Alert.AlertType.WARNING);
        }
    }

    @FXML
    public void showMessage(String message, Alert.AlertType type) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Alert alert = new Alert(type);
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
            }
        });
    }

    @FXML
    public void showErrorMessage(String message) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText(null);
                alert.setContentText("При выполнении операции возникла ошибка: \n" + message);
                alert.show();
            }
        });
    }

    @FXML
    public void showDone() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Готово");
                alert.setHeaderText("Готово");
                alert.setContentText("Обработка завершена");
                alert.show();
            }
        });
    }

    @FXML
    public void handle_placeYesterdayDates() {
        LocalDate yesterday = LocalDate.now();
        yesterday = yesterday.minusDays(1);
        LocalDate today = LocalDate.now();
        startDateDatePicker.setValue(yesterday);
        endDateDatePicker.setValue(today);
    }

    @FXML
    public void handle_convertBySnilsFile() {
        FileChooser mergedFileChooser = new FileChooser();
        mergedFileChooser.setTitle("выбрать файл");
        File homeFolder = new File(homeFolderPathString);
        mergedFileChooser.setInitialDirectory(homeFolder);
        File selectedFile = mergedFileChooser.showOpenDialog(scene.getWindow());
        if (selectedFile != null) {
            if (selectedFile.isFile() == false) {
                showErrorMessage("Ошибка - необходимо выбрать файл");
            } else {
                try {
                    enteryPoint.startListConversation(selectedFile, false, false);
                } catch (Exception ex) {
                    LoggingService.writeLog(ex);
                    showErrorMessage(ex.getMessage());
                }
            }
        }
    }

    @FXML
    public void handle_startChangesChecker() {
        try {
            enteryPoint.startControllCheckerConversation();
        } catch (Exception ex) {
            LoggingService.writeLog(ex);
            showErrorMessage(ex.getMessage());
        }
    }

    @FXML
    public void clearFRLLORecords() {
        LocalDate start = startClearDateDatePicker.getValue();
        LocalDate end = endClearDateDatePicker.getValue();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DateUtils.datePattern);
        MainWindowController controller = this;
        LoggingService.writeLog("ClearFRLLORecords", "debug");
        if (start != null && end != null) {
            if (start.isBefore(end) || start.isEqual(end)) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Очистка записей ФРЛЛО");
                        alert.setContentText("Будут удалены записи о конвертированных данных за период\r\n"
                                + "с " + start.format(dtf)
                                + " по " + end.format(dtf)
                        );
                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.get() == ButtonType.OK) {
                            LoggingService.writeLog("Run clear", "debug");
                            try {
                                String resultMessage = ConnectionService.getInstance().clearFRLLO(controller, start, end);
                                showMessage(resultMessage, Alert.AlertType.INFORMATION);
                            } catch (Exception ex) {
                                LoggingService.writeLog(ex);
                                showErrorMessage("Ошибка при очистке БД ФРЛЛО");
                            }
                        }
                    }
                });
            } else {
                showErrorMessage("Установите корректный отрезок времени");
            }
        } else {
            showErrorMessage("Установите даты для очистки");
        }
    }

    @FXML
    public void runConvertedLoading() {
        LoggingService.writeLog("runConvertedLoading - save settings", "debug");
        savePathToProtocolFolders();
        saveSettings();
        try {
            LoggingService.writeLog("runConvertedLoading - initiate", "debug");
            ConvertedLoaderService.getInstance().initiateConvertedReader();
        } catch (Exception ex) {
            showStatusInfo("Ошибка при запуске загрузчика конвертации");
            LoggingService.writeLog(ex);
        }
    }

    @FXML
    public void runResultsLoading() {
        LoggingService.writeLog("runResultsAnaliz - save settings", "debug");
        savePathToProtocolFolders();
        saveSettings();
        try {
            LoggingService.writeLog("runConvertedLoading - initiate", "debug");
            ConvertedLoaderService.getInstance().initiateResultsReader();
        } catch (InterruptedException ex) {
            showStatusInfo("Ошибка при запуске загрузчика результатов");
            LoggingService.writeLog(ex);
        }
    }

    @FXML
    public void handle_writeStatistics() {
        StatisticsService.getInstance().writeStatistics(this);
    }

    @FXML
    public void handle_writeFBDPerrors() {
        FBDPerrorsWriter errorWriter = new FBDPerrorsWriter(this);
        errorWriter.writeErrors();
    }

    @FXML
    public void handle_writeFRLLOerrors() {
        FRLLOerrorsWriter errorWriter = new FRLLOerrorsWriter(this);
        errorWriter.writeErrors();
    }

    @FXML
    public void handle_checkErrorTables() {
        FRLLOErrorsClearer clearer = new FRLLOErrorsClearer(this);
        clearer.clear();
    }

    @FXML
    public void handle_deletePersonFromFiles() {
        PersonDeleter pd = new PersonDeleter(this, filesToDeletePersonTextField.getText(), sniltToDeleteTextField.getText());
        pd.deletePerson();
    }

    public void showDoneWithSomeErrors(FrlloConverter converter) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Готово");
                alert.setHeaderText("Готово");
                alert.setContentText("Обработка завершена - файлы находятся в папке \\out. "
                        + "\r\n Программа обнаружила некоторые ошибки - отброшено записей:"
                        + converter.getGlobalStatistics().getSkipedInvalidCount() + " подробности в логе:  frllo_log.txt");
                alert.show();
            }
        });
    }

    synchronized public void showTempInfo(String info) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                infoTextArea.setText(LoggingService.getDateTimeString() + "\r\n" + info);
            }
        });
    }

    synchronized public void showCount(String count, String info) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                countLabel.setText(count);
                infoTextArea.setText(LoggingService.getDateTimeString() + "\r\n" + info);
            }
        });
    }

    public synchronized void showStatusInfo(String info) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                messagesStack.insertString(info);
                statusInfoTextArea.setText(messagesStack.getMessages());
                LoggingService.writeLog(info, "debug");
            }
        });
    }

    public void showLastRunLabel(String dateLabel) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                lastCheckedLabel.setText("Последняя проверка: " + dateLabel);
            }
        });
    }

    public void resetFRLLODbSettings() {
        SettingsService.getInstance().setValueFromString("frllo_login:" + frlloLoginField.getText());
        SettingsService.getInstance().setValueFromString("frllo_password:" + frlloPasswordField.getText());
        SettingsService.getInstance().setValueFromString("frllo_ip:" + frlloHostField.getText());
        SettingsService.getInstance().setValueFromString("frllo_port:" + frlloPortField.getText());
        SettingsService.getInstance().setValueFromString("frllo_dbSchemaName:" + frlloSchemaField.getText());
        SettingsService.getInstance().setValueFromString("frllo_dbname:" + frlloNameField.getText());
        savePathToProtocolFolders();
        SettingsService.getInstance().saveSettings();
    }

    public void resetFBDPDbSettings() {
        SettingsService.getInstance().setValueFromString("fbdp_login:" + dbFBDPLoginField.getText());
        SettingsService.getInstance().setValueFromString("fbdp_password:" + dbFBDPPasswordField.getText());
        SettingsService.getInstance().setValueFromString("fbdp_ip:" + dbFBDPHostField.getText());
        SettingsService.getInstance().setValueFromString("fbdp_port:" + dbFBDPPortField.getText());
        SettingsService.getInstance().setValueFromString("fbdp_dbSchemaName:" + dbFBDPSchemaField.getText());
        SettingsService.getInstance().setValueFromString("fbdp_dbname:" + dbFBDPNameField.getText());
        savePathToProtocolFolders();
        SettingsService.getInstance().saveSettings();
    }

    private void savePathToProtocolFolders() {
        SettingsService.getInstance().setValueFromString("resultsPath:" + pathToResults.getText());
        SettingsService.getInstance().setValueFromString("loadedPath:" + pathToConverted.getText());
        SettingsService.getInstance().saveSettings();
    }

    private void refreshServerPanes() {
        try {
            fbdpConnectionIndicator.getStyleClass().removeAll("running_ok");
            fbdpConnectionIndicator.getStyleClass().add("running_error");
            if (ConnectionService.getInstance().getConnectionFBDP() != null) {
                if (ConnectionService.getInstance().getConnectionFBDP().isClosed() == false) {
                    fbdpConnectionIndicator.getStyleClass().removeAll("running_error");
                    fbdpConnectionIndicator.getStyleClass().add("running_ok");
                }
            }
            frlloConnectionIndicator.getStyleClass().removeAll("running_ok");
            frlloConnectionIndicator.getStyleClass().add("running_error");
            if (ConnectionService.getInstance().getConnectionFRLLO() != null) {
                if (ConnectionService.getInstance().getConnectionFRLLO().isClosed() == false) {
                    frlloConnectionIndicator.getStyleClass().removeAll("running_error");
                    frlloConnectionIndicator.getStyleClass().add("running_ok");
                }
            }
        } catch (SQLException ex) {
            showErrorMessage("Ошибка при попытке установить соединение с БД ФБДП");
        }
    }

    public void switchSchedulerButtonText(boolean runAbbility) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (runAbbility) {
                    schedulerButtonFlagPane.getStyleClass().removeAll("red_button");
                    schedulerButtonFlagPane.getStyleClass().add("green_button");
                    schedulerButton.setText("Запустить планировщик");
                } else {
                    schedulerButtonFlagPane.getStyleClass().removeAll("green_button");
                    schedulerButtonFlagPane.getStyleClass().add("red_button");
                    schedulerButton.setText("Остановить планировщик");
                }
            }
        });
    }

    public void switchSchedulerButtonText(String newLabel) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                schedulerButton.setText(newLabel);
            }
        });
    }

    private boolean checkDatePickers() {
        LocalDate start = startDateDatePicker.getValue();
        LocalDate end = endDateDatePicker.getValue();
        if (start != null && end != null) {
            if (end.isAfter(start)) {
                return true;
            }
        }
        return false;
    }

    public TextArea getInfoTextArea() {
        return infoTextArea;
    }

    public void showStatiticsExistsDialog(String dayToValue, FBDPBaseReader reader) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setTitle("Запись уже БД");
                alert.setHeaderText("В БД уже есть запись статистики с датой окночания " + dayToValue);
                alert.setContentText("Повторить запрос на дату?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    try {
                        reader.proceedWithRead();
                    } catch (Exception ex) {
                        LoggingService.writeLog(ex);
                    }
                }
            }
        });
    }

    private class RadioButtonId {

        private String id = "";

        public RadioButtonId(String str) {
            id = str;
        }

        @Override
        public String toString() {
            return id;
        }

    }

}
