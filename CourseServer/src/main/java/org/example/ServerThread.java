package org.example;

import com.quartyom.QuPackets.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

class ServerThread extends Thread {
    private static final Logger log = LoggerFactory.getLogger(ServerThread.class);
    Socket socket;
    SessionsHolder sessionsHolder;
    public ServerThread(SessionsHolder sessionsHolder, Socket socket) {
        this.sessionsHolder = sessionsHolder;
        this.socket = socket;
        try {
            this.messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            log.error("hash: " + e.getMessage());
        }
    }

    QuDB db = new QuDB();
    QuSession session;

    Random random = new Random();
    MessageDigest messageDigest;


    @Override
    public void run() {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ) {
            log.info("Thread started");
            while (!isInterrupted()) {
                QuPacket inPacket;
                QuPacket outPacket = new QuPacket();
                try {
                    inPacket = (QuPacket) in.readObject();

                    if (inPacket.sessionId != null) {   // если сессия указана, получаем
                        session = sessionsHolder.getSession(inPacket.sessionId);
                        if (session != null) {
                            session.lastUpdated = System.currentTimeMillis();
                        }
                    }

                    switch (inPacket.type) {
                        case "auth":
                            auth(inPacket, outPacket);
                            break;
                        case "register":
                            register(inPacket, outPacket);
                            break;
                        case "getAdvPlatforms":
                            getAdvPlatforms(outPacket);
                            break;
                        case "getOrdersForAdvPlatform":
                            getOrdersForAdvPlatform(inPacket, outPacket);
                            break;
                        case "getOrdersForUser":
                            getOrdersForUser(outPacket);
                            break;
                        case "getHistoryForUser":
                            getHistoryForUser(outPacket);
                            break;
                        case "toBook":
                            toBook(inPacket, outPacket);
                            break;
                        case "getUserDeposit":
                            getUserDeposit(outPacket);
                            break;
                        case "popupUserDeposit":
                            popupUserDeposit(outPacket);
                            break;
                        case "canselOrder":
                            canselOrder(inPacket, outPacket);
                            break;
                        default:
                            outPacket.type = "error";
                            outPacket.data = "unknown request type: " + inPacket.type;
                    }

                } catch (ClassNotFoundException e) {
                    log.error(e.getMessage());
                    outPacket.type = "error";
                    outPacket.data = "undefined request";
                }
                out.writeObject(outPacket);
                out.flush();
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
        log.info("Thread stopped");

    }

    private void auth(QuPacket inPacket, QuPacket outPacket){
        try {
            LoginData data = (LoginData) inPacket.data;
            String login = data.login;
            String password = data.password;
            if (Validator.minLoginLen > login.length() || login.length() > Validator.maxLoginLen){
                outPacket.type = "error";
                outPacket.data = "Некорректный логин";
            }
            else if (Validator.minPasswordLen > password.length() || password.length() > Validator.maxPasswordLen){
                outPacket.type = "error";
                outPacket.data = "Некорректный пароль";
            }
            else {
                messageDigest.update(password.getBytes());
                password = new String(messageDigest.digest());
                session = db.auth(login, password);
                if (session != null) {
                    // получаем уникальный id
                    long sessionId = random.nextLong();
                    sessionsHolder.addSession(sessionId, session);
                    outPacket.type = "ok";
                    outPacket.sessionId = sessionId;

                } else {
                    outPacket.type = "error";
                    outPacket.data = "Неверный логин или пароль";
                }
            }
        }
        catch (ClassCastException ex){
            log.error(ex.getMessage());
            outPacket.type = "error";
            outPacket.data = "Некорректный пакет";
        }
    }

    private void register(QuPacket inPacket, QuPacket outPacket){
        try {
            LoginData data = (LoginData) inPacket.data;
            String login = data.login;
            String password = data.password;
            if (Validator.minLoginLen > login.length() || login.length() > Validator.maxLoginLen){
                outPacket.type = "error";
                outPacket.data = "Некорректный логин";
            }
            else if (Validator.minPasswordLen > password.length() || password.length() > Validator.maxPasswordLen){
                outPacket.type = "error";
                outPacket.data = "Некорректный пароль";
            }
            else {
                messageDigest.update(password.getBytes());
                password = new String(messageDigest.digest());
                if (db.register(login, password)) {
                    outPacket.type = "ok";
                } else {
                    outPacket.type = "error";
                    outPacket.data = "Пользователь с таким логином уже зарегистрирован";
                }
            }
        }
        catch (ClassCastException ex){
            log.error(ex.getMessage());
            outPacket.type = "error";
            outPacket.data = "Некорректный пакет";
        }
    }

    private void getAdvPlatforms(QuPacket outPacket){
        ArrayList<AdvPlatform> platforms = db.getAdvPlatforms();
        if (platforms != null) {
            outPacket.type = "ok";
            outPacket.data = platforms;
        }
        else {
            outPacket.type = "error";
            outPacket.data = "Проблемы с базой данных";
        }
    }

    public void getOrdersForAdvPlatform(QuPacket inPacket, QuPacket outPacket){
        try {
            AdvPlatform platform = (AdvPlatform) inPacket.data;
            if (platform != null) {
                ArrayList<Order> orders = db.getOrdersForAdvPlatform(platform.id);
                if (orders != null) {
                    outPacket.type = "ok";
                    outPacket.data = orders;
                }
                else {
                    outPacket.type = "error";
                    outPacket.data = "Проблемы с базой данных";
                }
                return;
            }
        }
        catch (ClassCastException ex) { log.error(ex.getMessage()); }
        outPacket.type = "error";
        outPacket.data = "Некорректный запрос";
    }

    public void toBook(QuPacket inPacket, QuPacket outPacket){
        try {
            if (session == null) {
                outPacket.type = "unauthorized";
                return;
            }

            ArrayList<Order> orders = (ArrayList<Order>) inPacket.data;
            if (orders != null) {

                ArrayList<Integer> results = db.toBook(orders, session.userId);

                if (results != null) {
                    outPacket.type = "ok";
                    outPacket.data = results;
                }
                else {
                    outPacket.type = "error";
                    outPacket.data = "Проблемы с базой данных";
                }
                return;
            }
        }
        catch (ClassCastException ex) { log.error(ex.getMessage()); }
        outPacket.type = "error";
        outPacket.data = "Некорректный запрос";
    }

    public void popupUserDeposit(QuPacket outPacket){
        try {
            if (session == null) {
                outPacket.type = "unauthorized";
                return;
            }

            long deposit = db.popupUserDeposit(session.userId);

            if (deposit != -1) {
                outPacket.type = "ok";
                outPacket.data = deposit;
            }
            else {
                outPacket.type = "error";
                outPacket.data = "Проблемы с базой данных";
            }
            return;

        }
        catch (ClassCastException ex) { log.error(ex.getMessage()); }
        outPacket.type = "error";
        outPacket.data = "Некорректный запрос";
    }

    public void getUserDeposit(QuPacket outPacket){
        try {
            if (session == null) {
                outPacket.type = "unauthorized";
                return;
            }

            long deposit = db.getUserDeposit(session.userId);

            if (deposit != -1) {
                outPacket.type = "ok";
                outPacket.data = deposit;
            }
            else {
                outPacket.type = "error";
                outPacket.data = "Проблемы с базой данных";
            }
            return;

        }
        catch (ClassCastException ex) { log.error(ex.getMessage()); }
        outPacket.type = "error";
        outPacket.data = "Некорректный запрос";
    }

    public void getOrdersForUser(QuPacket outPacket) {
        try {
            if (session == null) {
                outPacket.type = "unauthorized";
                return;
            }
            ArrayList<OrderForUser> orders = db.getOrdersForUser(session.userId);
            if (orders != null) {
                outPacket.type = "ok";
                outPacket.data = orders;
            }
            else {
                outPacket.type = "error";
                outPacket.data = "Проблемы с базой данных";
            }
            return;

        }
        catch (ClassCastException ex) { log.error(ex.getMessage()); }
        outPacket.type = "error";
        outPacket.data = "Некорректный запрос";
    }
    public void getHistoryForUser(QuPacket outPacket) {
        try {
            if (session == null) {
                outPacket.type = "unauthorized";
                return;
            }
            ArrayList<OrderForUser> orders = db.getHistoryForUser(session.userId);
            if (orders != null) {
                outPacket.type = "ok";
                outPacket.data = orders;
            }
            else {
                outPacket.type = "error";
                outPacket.data = "Проблемы с базой данных";
            }
            return;

        }
        catch (ClassCastException ex) { log.error(ex.getMessage()); }
        outPacket.type = "error";
        outPacket.data = "Некорректный запрос";
    }

    public void canselOrder(QuPacket inPacket, QuPacket outPacket) {
        try {
            if (session == null) {
                outPacket.type = "unauthorized";
                return;
            }

            int result = db.canselOrder(session.userId, (long)inPacket.data);

            switch (result) {
                case 0:
                    outPacket.type = "error";
                    outPacket.data = "Невозможно отменить заказ";
                    return;
                case 1:
                    outPacket.type = "ok";
                    return;
                default:
                    outPacket.type = "error";
                    outPacket.data = "Проблемы с базой данных";
                    return;
            }
        }
        catch (ClassCastException ex) { log.error(ex.getMessage()); }
        outPacket.type = "error";
        outPacket.data = "Некорректный запрос";
    }


}
