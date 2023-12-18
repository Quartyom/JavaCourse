package com.quartyom.courseclient;

import com.quartyom.QuPackets.AdvPlatform;

import java.io.Serializable;
import java.util.HashSet;

public class AppData implements Serializable {
    public String login;
    public String password;
    public HashSet<Long> favouritePlatforms;

    public AppData(){
        favouritePlatforms = new HashSet<>();
    }

}
