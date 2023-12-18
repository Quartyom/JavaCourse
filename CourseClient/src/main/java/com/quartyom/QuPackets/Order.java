package com.quartyom.QuPackets;

import java.io.Serializable;
import java.sql.Date;

public class Order implements Serializable {
    public long id;
    public long advPlatformId;
    public long userId;
    public Date orderDate;
}