package org.example;

import java.io.Serializable;

public class QuSession implements Serializable {
    public int userId;
    public long lastUpdated = System.currentTimeMillis();
}
