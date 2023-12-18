package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;

public class Server {

    private static final Logger log = LoggerFactory.getLogger(Server.class);
    public final Properties properties = new Properties();
    public final int port;
    private final ArrayList<ServerThread> serverThreads;
    public final SessionsHolder sessionsHolder;
    public final SessionsThread sessionsThread;
    public final AutoSaver autoSaver;
    public Server() {
        readProperties();
        port = Integer.parseInt(properties.getProperty("serverPort"));
        sessionsHolder = new SessionsHolder(properties);
        serverThreads = new ArrayList<>();
        sessionsThread = new SessionsThread(sessionsHolder);
        autoSaver = new AutoSaver(this);
        loadAppState();
    }

    public void run() {
        sessionsThread.start();
        autoSaver.start();

        try (ServerSocket serverSocket = new ServerSocket(port)){
            log.info("Server started");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientSocket.setSoTimeout(Integer.parseInt(properties.getProperty("socketTimeoutMs")));
                ServerThread t = new ServerThread(sessionsHolder, clientSocket);
                serverThreads.add(t);
                t.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            serverThreads.forEach(Thread::interrupt);
            autoSaver.interrupt();
            sessionsThread.interrupt();
            log.info("Server stopped");
        }
    }

    public void readProperties(){
        try (InputStream input = new FileInputStream("app.properties")) {
            properties.load(input);
            log.info("Loaded properties");
        } catch (IOException e) {
            log.error("Couldnt load properties: " + e.getMessage());
        }
    }

    public void saveAppState(){
        try (FileOutputStream fos = new FileOutputStream(properties.getProperty("appDataFile"));
             ObjectOutputStream oos = new ObjectOutputStream(fos))
        {
            oos.writeObject(sessionsHolder);
            log.info("Saved server state");
        } catch (IOException e) {
            log.error("Couldn't save server state, " + e.getMessage());
        }
    }

    public void loadAppState(){
        try (FileInputStream fis = new FileInputStream(properties.getProperty("appDataFile"));
             ObjectInputStream ois = new ObjectInputStream(fis))
        {
            sessionsHolder.copyHolder((SessionsHolder) ois.readObject());
            log.info("Loaded server state");
        }
        catch (IOException | ClassNotFoundException e) {
            log.error("Couldn't load server state, " + e.getMessage());
            e.printStackTrace();
        }
    }
}

