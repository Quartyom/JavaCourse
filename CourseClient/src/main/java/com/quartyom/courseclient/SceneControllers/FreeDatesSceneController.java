package com.quartyom.courseclient.SceneControllers;

import com.quartyom.QuPackets.AdvPlatform;
import com.quartyom.QuPackets.Order;
import com.quartyom.QuPackets.QuPacket;
import com.quartyom.courseclient.QuApplication;
import com.quartyom.courseclient.ServerAdapter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Date;

public class FreeDatesSceneController extends SceneController{
    Parent root;
    Label priceLabel, descriptionLabel;
    Button backButton, nextButton;
    HashSet<java.sql.Date> chosenDates;
    ScrollPane scrollPane;
    ObservableList<Parent> items;
    URL tileResource;

    public static long advPlatformId;
    public static String description, price;

    private static final Logger scenesLog = LoggerFactory.getLogger("scenes");
    private static final Logger serverLog = LoggerFactory.getLogger("server");

    public FreeDatesSceneController(QuApplication application) {
        super(application);
        try {
            root = FXMLLoader.load(application.getClass().getResource("free-dates-view.fxml"));
            tileResource = application.getClass().getResource("free-date-tile.fxml");

            priceLabel = (Label) root.lookup("#priceLabel");
            descriptionLabel = (Label) root.lookup("#descriptionLabel");
            backButton = (Button) root.lookup("#backButton");
            nextButton = (Button) root.lookup("#nextButton");
            scrollPane = (ScrollPane) root.lookup("#scrollPane");

            backButton.setOnAction(action -> application.setScene(new ShowPlatformsSceneController(application)));
            nextButton.setOnAction(action -> {
                toBook();
            });

            items = FXCollections.observableArrayList();
            chosenDates = new HashSet<>();
        }
        catch (IOException e) {
            serverLog.error(e.getMessage());
        }

        scene = new Scene(root);
    }

    public String resultToStr(int result){
        switch (result) {
            case -1:
                return "Недостаточно средств";
            case 0:
                return "Дата занята";
            case 1:
                return "Забронировано";
            default:
                return "Произошла ошибка";
        }
    }

    public void toBook(){
        ArrayList<Order> orders = new ArrayList<>();
        for (var item : chosenDates) {
            Order order = new Order();
            order.advPlatformId = advPlatformId;
            order.orderDate = item;
            orders.add(order);
        }
        QuPacket packet = new QuPacket("toBook");
        packet.sessionId = application.sessionId;
        packet.data = orders;
        try {
            QuPacket response = ServerAdapter.doRequest(application.properties, packet);

            if (response != null) {
                switch (response.type) {
                    case "ok":
                        try {

                            StringBuilder string = new StringBuilder();
                            ArrayList<Date> dates = new ArrayList<>(chosenDates);
                            ArrayList<Integer> results = (ArrayList<Integer>) response.data;
                            for (int i = 0; i < results.size(); i++) {
                                string.append(dates.get(i)).append(" ").append(resultToStr(results.get(i)));
                                string.append("\n");
                            }
                            ResultSceneController.labelText = "Результат";
                            ResultSceneController.textAreaText = string.toString();
                        }
                        catch (Exception ex) {
                            ResultSceneController.labelText = "Ошибка";
                            ResultSceneController.textAreaText = "Некорректный ответ сервера";
                        }
                        break;
                    case "error":
                        ResultSceneController.labelText = "Ошибка";
                        ResultSceneController.textAreaText = response.data.toString();
                        break;
                    case "unauthorized":
                        AuthSceneController.whereToReturn = new FreeDatesSceneController(application);
                        application.setScene(AuthSceneController.class);
                        return;
                }
            }
            else {
                ResultSceneController.labelText = "Ошибка";
                ResultSceneController.textAreaText = "Некорректный ответ сервера";
                serverLog.error("invalid response");
            }
        } catch (IOException e) {
            ResultSceneController.labelText = "Ошибка";
            ResultSceneController.textAreaText = "Ошибка сервера";
            serverLog.error(e.getMessage());
        }
        ResultSceneController.whereToReturnObject = new ShowPlatformsSceneController(application);
        application.setScene(ResultSceneController.class);
    }

    public ArrayList<Order> getOrdersForAdvPlatform(){
        QuPacket packet = new QuPacket("getOrdersForAdvPlatform");
        AdvPlatform platform = new AdvPlatform();
        platform.id = advPlatformId;
        packet.data = platform;
        try {
            QuPacket response = ServerAdapter.doRequest(application.properties, packet);
            if (response != null) {
                try {
                    return (ArrayList<Order>) response.data;
                }
                catch (Exception e) {
                    serverLog.error(e.getMessage());
                }
            }
            else {
                serverLog.error("invalid response");
            }
        } catch (IOException e) {
            serverLog.error(e.getMessage());
        }
        return null;
    }

    public HashSet<Long> getBookedDates(){
        ArrayList<Order> orders = getOrdersForAdvPlatform();
        if (orders == null) { return null; }
        HashSet<Long> dates = new HashSet<>();
        for (var item : orders) {
            dates.add(item.orderDate.getTime());
        }
        return dates;
    }

    public ArrayList<Long> getDates(){
        ArrayList<Long> dates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        for (int i = 0; i < 14; i++){           // 2 weeks
            dates.add(calendar.getTimeInMillis());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return dates;
    }

    private String getDescriptionText(AdvPlatform platform){
        return "%s: %s, место %d".formatted(platform.subtype, platform.description, platform.spot);
    }

    @Override
    public void update() {
        descriptionLabel.setText(description);
        priceLabel.setText(price);

        items.clear();
        ArrayList<Long> dates = getDates();
        HashSet<Long> bookedDates = getBookedDates();
        if (bookedDates == null) {
            return;
        }

        for (int i = 0; i < 7; i++){
            try {
                Parent tile = FXMLLoader.load(tileResource);

                CheckBox date1box = (CheckBox) tile.lookup("#date1");
                CheckBox date8box = (CheckBox) tile.lookup("#date8");

                java.sql.Date date1 = new java.sql.Date(dates.get(i));  // отключает недоступные даты
                date1box.setText(date1.toString());
                date1box.setDisable(bookedDates.contains(dates.get(i)));

                java.sql.Date date8 = new java.sql.Date(dates.get(i + 7));
                date8box.setText(date8.toString());
                date8box.setDisable(bookedDates.contains(dates.get(i + 7)));

                date1box.setOnAction(action -> {
                    if (date1box.isSelected()) {
                        chosenDates.add(date1);
                    }
                    else {
                        chosenDates.remove(date1);
                    }
                });
                date8box.setOnAction(action -> {
                    if (date8box.isSelected()) {
                        chosenDates.add(date8);
                    }
                    else {
                        chosenDates.remove(date8);
                    }
                });

                items.add(tile);
            }
            catch (Exception ex){
                scenesLog.error(ex.getMessage());
            }
        }

        scrollPane.setContent(new ListView<>(items));
    }

    @Override
    public void stop() {}
}
