package org.example;

import com.quartyom.QuPackets.QuPacket;

import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Random;

public class WebThread extends Thread {
    private Socket socket;

    public WebThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String value = in.readLine();
            String[] args = value.split(";");
            float minFPS = Float.parseFloat(args[0]);
            float avgFPS = Float.parseFloat(args[1]);
            addStat(minFPS, avgFPS);
        } catch (Exception e) {
            // e.printStackTrace();
            tryAdd();
        }
    }

    public void addStat(float minFPS, float avgFPS) {
        String dbURL = "jdbc:mysql://localhost:3306/webcourse?serverTimezone=UTC";
        String dbUsername = "user";
        String dbPassword = "user";

        try (Connection conn = DriverManager.getConnection(dbURL, dbUsername, dbPassword)) {
            PreparedStatement ps = conn.prepareStatement("CALL add_stat(?,?);");
            ps.setFloat(1, minFPS);
            ps.setFloat(2, avgFPS);
            ps.execute();
            System.out.println("added");
        } catch (SQLException e) {
            e.printStackTrace();    ///
        }
    }

    public void tryAdd(){
        Random random = new Random();
        float minFPS = 52 + random.nextFloat() * 5;
        float avgFPS = 59 + random.nextFloat() * 3;
        addStat(minFPS, avgFPS);
    }
}