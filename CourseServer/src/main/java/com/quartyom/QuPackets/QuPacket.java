package com.quartyom.QuPackets;

import java.io.*;

public class QuPacket implements Serializable {
    public String type;     // packet type is literally what to do with data
    //public int version = -1;     // supposed that type and version must be compatible for client and server for correct json representation
    public Long sessionId;
    public Object data;

    public QuPacket(){}
    public QuPacket(String type){
        this.type = type;
    }
}
