package org.example;

import com.quartyom.QuPackets.AdvPlatform;
import com.quartyom.QuPackets.Order;
import com.quartyom.QuPackets.OrderForUser;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;

public class QuDB {
    String dbURL = "jdbc:mysql://localhost:3306/javacourse?serverTimezone=UTC";
    String dbUsername = "root";
    String dbPassword = "Mcommon7302places";
    public QuDB(){}

    public synchronized QuSession auth(String login, String password){
        try (Connection conn = DriverManager.getConnection(dbURL, dbUsername, dbPassword)) {
            PreparedStatement ps = conn.prepareStatement("SELECT id FROM users WHERE login=? AND password=? LIMIT 1");
            ps.setString(1, login);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                QuSession session = new QuSession();
                session.userId = rs.getInt("id");
                return session;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized boolean register(String login, String password){
        try (Connection conn = DriverManager.getConnection(dbURL, dbUsername, dbPassword)) {
            String callFunction = "{ ? = CALL register(?, ?) }";
            CallableStatement statement = conn.prepareCall(callFunction);
            statement.setString(2, login);
            statement.setString(3, password);
            statement.registerOutParameter(1, Types.INTEGER);
            statement.execute();
            return statement.getInt(1) != 0;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public synchronized ArrayList<AdvPlatform> getAdvPlatforms(){
        ArrayList<AdvPlatform> platforms = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(dbURL, dbUsername, dbPassword)) {
            PreparedStatement ps = conn.prepareStatement("CALL show_platforms");
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                AdvPlatform platform = new AdvPlatform();
                platform.id = rs.getLong("id");
                platform.subtype = rs.getString("subtype");
                platform.description = rs.getString("description");
                platform.spot = rs.getInt("spot");
                platform.priceKop = rs.getInt("price_kop");
                platform.kFreeDates = rs.getInt("k_free_dates");
                platforms.add(platform);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return platforms;
    }

    public synchronized ArrayList<Order> getOrdersForAdvPlatform(long advPlatformId) {
        ArrayList<Order> orders = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(dbURL, dbUsername, dbPassword)) {
            PreparedStatement ps = conn.prepareStatement("CALL get_current_orders_for_platform(?)");
            ps.setLong(1, advPlatformId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                Order order = new Order();
                order.id = rs.getLong("id");
                order.advPlatformId = rs.getLong("adv_platform_id"); // equals to input
                order.userId = rs.getLong("user_id");
                order.orderDate = rs.getDate("order_date");
                orders.add(order);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public synchronized ArrayList<OrderForUser> getOrdersForUser(long userId) {
        ArrayList<OrderForUser> orders = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(dbURL, dbUsername, dbPassword)) {
            PreparedStatement ps = conn.prepareStatement("CALL get_current_orders_for_user(?)");
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                OrderForUser order = new OrderForUser();
                order.id = rs.getLong("id");
                order.subtype = rs.getString("subtype");
                order.description = rs.getString("description");
                order.spot = rs.getInt("spot");
                order.canselPriceKop = rs.getInt("cansel_price_kop");
                order.orderDate = rs.getDate("order_date");
                orders.add(order);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public synchronized ArrayList<OrderForUser> getHistoryForUser(long userId) {
        ArrayList<OrderForUser> orders = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(dbURL, dbUsername, dbPassword)) {
            PreparedStatement ps = conn.prepareStatement("CALL get_history_for_user(?)");
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                OrderForUser order = new OrderForUser();
                order.id = rs.getLong("id");
                order.subtype = rs.getString("subtype");
                order.description = rs.getString("description");
                order.spot = rs.getInt("spot");
                order.canselPriceKop = rs.getInt("cansel_price_kop");
                order.orderDate = rs.getDate("order_date");
                orders.add(order);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public synchronized ArrayList<Integer> toBook(ArrayList<Order> orders, long userId){
        ArrayList<Integer> results = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(dbURL, dbUsername, dbPassword)) {

            for (Order order : orders) {
                try {
                    String callFunction = "{ ? = CALL to_book(?,?,?) }";
                    CallableStatement statement = conn.prepareCall(callFunction);
                    statement.setLong(2, order.advPlatformId);
                    statement.setLong(3, userId);
                    statement.setDate(4, order.orderDate);
                    statement.registerOutParameter(1, Types.INTEGER);
                    statement.execute();
                    int result = statement.getInt(1);
                    results.add(result);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    results.add(-2);
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return results;
    }

    public synchronized long popupUserDeposit(long userId){
        try (Connection conn = DriverManager.getConnection(dbURL, dbUsername, dbPassword)) {
            String callFunction = "{ ? = CALL popup_user_deposit(?, 20000) }";
            CallableStatement statement = conn.prepareCall(callFunction);
            statement.setLong(2, userId);
            statement.registerOutParameter(1, Types.INTEGER);
            statement.execute();
            return statement.getLong(1);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public synchronized long getUserDeposit(long userId){
        try (Connection conn = DriverManager.getConnection(dbURL, dbUsername, dbPassword)) {
            String callFunction = "{ ? = CALL get_user_deposit(?) }";
            CallableStatement statement = conn.prepareCall(callFunction);
            statement.setLong(2, userId);
            statement.registerOutParameter(1, Types.INTEGER);
            statement.execute();
            return statement.getLong(1);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public synchronized int canselOrder(long userId, long orderId){
        try (Connection conn = DriverManager.getConnection(dbURL, dbUsername, dbPassword)) {
            String callFunction = "{ ? = CALL cansel_order(?,?) }";
            CallableStatement statement = conn.prepareCall(callFunction);
            statement.setLong(2, userId);
            statement.setLong(3, orderId);
            statement.registerOutParameter(1, Types.INTEGER);
            statement.execute();
            return statement.getInt(1);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

}
