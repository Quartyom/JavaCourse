package com.quartyom.QuPackets;

import java.io.*;

public class LoginData implements Serializable {
    public String login;
    public String password;

//    @Override
//    public void writeExternal(ObjectOutput out) throws IOException {
//        out.writeObject(login);
//        out.writeObject(password);
//    }
//
//    @Override
//    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
//        login = (String) in.readObject();
//        password = (String) in.readObject();
//    }
}
