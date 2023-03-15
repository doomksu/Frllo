package ru.gov.pfr.enteryPoint;

import ru.gov.pfr.controller.MainWindowController;
import ru.gov.pfr.fbdpReader.ExportedFileReader;
import ru.gov.pfr.fbdpReader.FBDPBaseReader;
import ru.gov.pfr.fbdpReader.FBDPReaderBySnilsInFRLLO;
import ru.gov.pfr.fbdpReader.FBDPReaderBySnilsList;
import ru.gov.pfr.fbdpReader.FRLLOSourceReader;
import java.io.File;
import java.time.LocalDate;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ru.gov.pfr.service.ConnectionService;
import ru.gov.pfr.service.LoggingService;
import ru.gov.pfr.service.SchedulerService;
import ru.gov.pfr.service.ServiceRunner;
import ru.gov.pfr.service.SettingsService;
import ru.gov.pfr.service.SingleInstanceService;

public class EnteryPoint extends Application {

    private Stage frontStage;
    private Stage modalStage;
    private FRLLOSourceReader reader;
    private volatile boolean isWorkInProgress = false;
    private ServiceRunner serviceRunner;
    private SchedulerService schedulerService;
    private TreeSet<String> snilsMap = new TreeSet<>();
    private MainWindowController controller;
    public static String version = "1.26 15.03.2023";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        runServices();
        showFrontStage();
    }

    private void runServices() {
        if (SettingsService.getInstance().checkDummyFile("SINGLE_INSTANCE") && !SingleInstanceService.getInstance().isSingleInstance()) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    showFrontStage();
                    LoggingService.writeLog("app is already running", "error");
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибка");
                    alert.setHeaderText(null);
                    alert.setContentText("Другой экземпляр приложения уже запущен.\r\n"
                            + "Завершите его и проверьте диспетчер задач перед перезапуском программы.");
                    alert.showAndWait();
                    exit();
                }
            });
        } else {
            new ServiceRunner(this);
        }
    }

    public void switchScheduler() {
        if (schedulerService != null) {
            schedulerService.stopLooping();
            controller.switchSchedulerButtonText(true);
            controller.switchSchedulerButtonText("Запустить планировщик");
            schedulerService = null;
        } else {
            EnteryPoint ep = this;
            controller.switchSchedulerButtonText(false);
            controller.switchSchedulerButtonText("Остановить  планировщик");
            new Thread() {
                public void run() {
                    serviceRunner = new ServiceRunner(ep);
                    schedulerService = new SchedulerService(ep);
                    Thread schedulerThread = new Thread(schedulerService, "schedulerThread");
                    schedulerThread.run();
                }
            }.start();
        }
    }

    public void showFrontStage() {
        try {
            frontStage = new Stage();
            FXMLLoader loader = new FXMLLoader(EnteryPoint.class.getResource("/view/main.fxml"));
            HBox pane = (HBox) loader.load();
            MainWindowController mController = loader.getController();
            controller = mController;
            controller.setEnteryPoint(this);
            Scene scene = new Scene(pane);
            frontStage.setScene(scene);
            frontStage.setResizable(true);
            frontStage.setTitle("Конвертация ФРЛЛО: " + SettingsService.getInstance().getRootFolder());
            frontStage.show();
            controller.setScene(scene);
            frontStage.setOnCloseRequest(
                    new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    LoggingService.writeLog("close button pressed", "debug");
                    if (reader != null) {
                        try {
                            reader.stop();
                            reader = null;
                        } catch (Exception ex) {
                            Logger.getLogger(EnteryPoint.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (reader != null) {
                        try {
                            reader.stop();
                            schedulerService.stopLooping();
                            serviceRunner.setCanceled();
                        } catch (Exception ex) {
                            LoggingService.writeLog(ex);
                            controller.showErrorMessage("Ошибка при закрытии процессов: \r\n" + ex.getMessage());
                        }
                        reader = null;
                    }
                    exit();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            LoggingService.writeLog(ex);
        }
    }

    public void startConvertationByFile(File byBasicFile) throws Exception {
        if (!isWorkInProgress) {
            reader = null;
            createFileReader(byBasicFile);
            launchReader();
        } else {
            controller.showStatusInfo("Программа занята другим процессом  " + LoggingService.getDateTimeString());
        }
    }

    public void startYesterdayConversation() throws Exception {
        if (!isWorkInProgress) {
            createTodayBaseReader();
            launchReader();
        }
    }

    public void startPeriodConversation(LocalDate start, LocalDate end) throws Exception {
        if (!isWorkInProgress) {
            createPeriodBaseReader(start, end);
            launchReader();
        } else {
            controller.showStatusInfo("Программа занята другим процессом  " + LoggingService.getDateTimeString());
        }
    }

    public void startListConversation(File file, boolean byId, boolean onlyMonetization) throws Exception {
        if (!isWorkInProgress) {
            createListBasicBaseReader(file, byId, onlyMonetization);
            launchReader();
        } else {
            controller.showStatusInfo("Программа занята другим процессом  " + LoggingService.getDateTimeString());
        }
    }

    public void startControllCheckerConversation() throws Exception {
        if (!isWorkInProgress) {
            createCheckBaseReader();
            launchReader();
        } else {
            controller.showStatusInfo("Программа занята другим процессом  " + LoggingService.getDateTimeString());
        }

    }

    public void createFileReader(File file) throws Exception {
        ExportedFileReader efr = new ExportedFileReader(this);
        efr.setFile(file);
        reader = efr;
    }

    public void createTodayBaseReader() throws Exception {
        if (ConnectionService.getInstance().getConnectionFBDP() != null) {
            reader = new FBDPBaseReader(this);
        } else {
            controller.showErrorMessage("Нет соединения с БД ФБДП\r\n");
        }
    }

    public void createPeriodBaseReader(LocalDate start, LocalDate end) throws Exception {
        if (ConnectionService.getInstance().getConnectionFBDP() != null) {
            reader = new FBDPBaseReader(this, start, end);
            reader.setDates(start, end);
        } else {
            controller.showErrorMessage("Нет соединения с БД ФБДП\r\n");
        }
    }

    public void createListBasicBaseReader(File file, boolean byId, boolean onlyMonetization) throws Exception {
        if (ConnectionService.getInstance().getConnectionFBDP() != null) {
            reader = new FBDPReaderBySnilsList(this, file, byId, onlyMonetization);
        } else {
            controller.showErrorMessage("Нет соединения с БД ФБДП\r\n");
        }
    }

    public void createCheckBaseReader() throws Exception {
        if (ConnectionService.getInstance().getConnectionFBDP() != null) {
            reader = new FBDPReaderBySnilsInFRLLO(this);
            launchReader();
        } else {
            controller.showErrorMessage("Нет соединения с БД ФБДП\r\n");
        }
    }

    private void launchReader() {
        try {
            if (reader != null) {
                Thread t = new Thread(reader);
                t.start();
            }
        } catch (Exception ex) {
            LoggingService.writeLog(ex);
            controller.showErrorMessage("Ошибка при запуске процесса конвертации:\r\n"
                    + ex.getMessage());
        }
    }

    public void exit() {
        try {
            if (ConnectionService.getInstance().getConnectionFRLLO() != null) {
                ConnectionService.getInstance().getConnectionFRLLO().close();
            }
            if (ConnectionService.getInstance().getConnectionFBDP() != null) {
                ConnectionService.getInstance().getConnectionFBDP().close();
            }
            SingleInstanceService.getInstance().closePortOnExit();
            System.exit(0);
        } catch (Exception ex) {
            LoggingService.writeLog(ex);
        }
    }

    public void makeErrorAlarm(String message) {
        LoggingService.writeLog("ReaderError: " + message, "error");
        controller.showErrorMessage("Ошибка при конвертации: \r\n" + message);
    }

    public boolean isSnilsWriten(String snils) {
        return snilsMap.contains(snils);
    }

    public void setSnilsWriten(String snils) {
        snilsMap.add(snils);
    }

    public Stage getModalStage() {
        return modalStage;
    }

    public TextArea getStatusTextArea() {
        return controller.getInfoTextArea();
    }

    public MainWindowController getMainController() {
        return controller;
    }

    public void setIsWorkInProgress(boolean isWorkInProgress) {
        this.isWorkInProgress = isWorkInProgress;
    }

    public boolean isWorkInProgress() {
        return isWorkInProgress;
    }

}
