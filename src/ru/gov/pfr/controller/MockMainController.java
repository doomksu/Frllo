package ru.gov.pfr.controller;

import ru.gov.pfr.enteryPoint.EnteryPoint;
import ru.gov.pfr.service.LoggingService;

/**
 *
 * @author Kirill Neretin
 */
public class MockMainController extends MainWindowController{

    @Override
    public void setEnteryPoint(EnteryPoint epoint) {
        LoggingService.writeLog("enteryPoint seted to mock controller", "debug");
    }
    
}
