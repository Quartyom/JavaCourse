package org.example;

import java.util.Properties;

public class AutoSaver extends Thread{
    private Server server;
    private final long saveTimeOut;
    public AutoSaver(Server server){
        this.server = server;
        this.saveTimeOut = Long.parseLong(server.properties.getProperty("autoSaveTimeOutMs"));
    }
    @Override
    public void run(){
        while (true) {
            server.saveAppState();
            try {
                sleep(saveTimeOut);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
