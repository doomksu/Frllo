package ru.gov.pfr.service;

import ru.gov.pfr.enteryPoint.EnteryPoint;

public class ServiceRunner {

    private static final String SINGLE_INSTANCE = "SINGLE_INSTANCE";

    public ServiceRunner(EnteryPoint ep) {
        LoggingService.getInstance();
        LoggingService.writeLog("runServices - start", "debug");
        try {

            LoggingService.writeLogIfDummy(SINGLE_INSTANCE, "run single instance", "debug");
            SettingsService.getInstance();
            ConnectionService.getInstance();
            ConvertedLoaderService.getInstance().setEnteryPoint(ep);
        } catch (Exception ex) {
            LoggingService.writeLog(ex);
        }
        LoggingService.writeLog("runServices done", "debug");
        LoggingService.writeLog("version: " + EnteryPoint.version, "debug");
    }

    public void setCanceled() throws Exception {

        ConnectionService.getInstance().closeConnections();
    }

}
