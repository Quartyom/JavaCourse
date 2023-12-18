package com.quartyom.QuPackets;

import java.io.Serializable;
import java.sql.Date;

public class OrderForUser implements Serializable {
    public long id;
    public String subtype;
    public String description;
    public int spot;
    public int canselPriceKop;
    public Date orderDate;
}
