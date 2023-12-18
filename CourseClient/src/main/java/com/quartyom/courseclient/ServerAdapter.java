package com.quartyom.courseclient;

import com.quartyom.QuPackets.AdvPlatform;
import com.quartyom.QuPackets.QuPacket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;

public class ServerAdapter {

    public static QuPacket doRequest(Properties properties, QuPacket inPacket) throws IOException {
        String host = properties.getProperty("hostUrl");
        int port = Integer.parseInt(properties.getProperty("hostPort"));
        int timeout = Integer.parseInt(properties.getProperty("socketTimeoutMs"));

        try (Socket socket = new Socket(host, port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        ){
            socket.setSoTimeout(timeout);
            out.writeObject(inPacket);
            out.flush();
            try {
                return (QuPacket) in.readObject();
            }
            catch (ClassNotFoundException e) {
                return null;
            }
        }
    }
}
