/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.gov.pfr.service;

import java.net.ServerSocket;

/**
 *
 * @author kneretin
 */
public class SingleInstanceService {

    private static SingleInstanceService instance;
    private ServerSocket socket;
    private int socketPort = 12389;
    private boolean isSingleInstance = false;

    public static SingleInstanceService getInstance() {
        if (instance == null) {
            instance = new SingleInstanceService();
        }
        return instance;
    }

    private SingleInstanceService() {
        try {
            socket = new ServerSocket(this.socketPort);
            isSingleInstance = true;
        } catch (Exception ex) {
            isSingleInstance = false;
        }
    }

    public boolean isSingleInstance() {
        return isSingleInstance;
    }

    public void closePortOnExit() throws Exception {
        if (socket != null) {
            socket.close();
        }
    }

}
