package com.quartyom.courseclient.SceneControllers;

import com.quartyom.QuPackets.OrderForUser;
import com.quartyom.QuPackets.QuPacket;
import com.quartyom.courseclient.QuApplication;
import com.quartyom.courseclient.ServerAdapter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class ShowHistorySceneController extends SceneController{
    Parent root;
    Label depositLabel;
    Button backButton, popupButton;
    ScrollPane scrollPane;
    ObservableList<Parent> items;
    URL tileResource;

    private static final Logger scenesLog = LoggerFactory.getLogger("scenes");
    private static final Logger serverLog = LoggerFactory.getLogger("server");

    public ShowHistorySceneController(QuApplication application) {
        super(application);
        try {
            root = FXMLLoader.load(application.getClass().getResource("show-platforms-view.fxml"));
            tileResource = application.getClass().getResource("history-platform-tile.fxml");

            depositLabel = (Label) root.lookup("#depositLabel");
            backButton = (Button) root.lookup("#backButton");
            popupButton = (Button) root.lookup("#popupButton");
            scrollPane = (ScrollPane) root.lookup("#scrollPane");

            backButton.setOnAction(action -> application.setScene(MainMenuSceneController.class));
            popupButton.setOnAction(action -> getUserDeposit(true));

            items = FXCollections.observableArrayList();
        }
        catch (IOException e) {
            serverLog.error(e.getMessage());
        }

        scene = new Scene(root);
    }

    public ArrayList<OrderForUser> getHistoryForUser(){
        try {
            QuPacket packet = new QuPacket("getHistoryForUser");
            packet.sessionId = application.sessionId;
            QuPacket response = ServerAdapter.doRequest(application.properties, packet);
            if (response != null) {
                try {
                    var res = (ArrayList<OrderForUser>) response.data;
                    if (res != null) {
                        return res;
                    }
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
        return new ArrayList<>();
    }

    public void getUserDeposit(boolean toPopup){
        try {
            QuPacket packet = new QuPacket(toPopup ? "popupUserDeposit" : "getUserDeposit");
            packet.sessionId = application.sessionId;
            QuPacket response = ServerAdapter.doRequest(application.properties, packet);
            if (response != null) {
                switch (response.type) {
                    case "unauthorized":
                        AuthSceneController.whereToReturn = new ShowHistorySceneController(application);
                        application.setScene(AuthSceneController.class);
                        break;
                    case "ok":
                        try {
                            long currentDepositKop = (long) response.data;
                            depositLabel.setText(currentDepositKop / 100.0f + "р");
                        }
                        catch (Exception e) {
                            serverLog.error(e.getMessage());
                        }
                        break;
                    default:
                        serverLog.error(response.data.toString());
                }
            }
            else {
                serverLog.error("invalid response");
            }
        } catch (IOException e) {
            serverLog.error(e.getMessage());
        }
    }
    private String getDescriptionText(OrderForUser order){
        return "%s: %s, место %d".formatted(order.subtype, order.description, order.spot);
    }

    @Override
    public void update() {
        getUserDeposit(false);

        items.clear();
        for (OrderForUser item : getHistoryForUser()){
            try {
                Parent tile = FXMLLoader.load(tileResource);

                Label descriptionLabel = (Label) tile.lookup("#descriptionLabel");
                Label dateLabel = (Label) tile.lookup("#dateLabel");
                Label priceLabel = (Label) tile.lookup("#priceLabel");

                String description = getDescriptionText(item);
                descriptionLabel.setText(description);

                priceLabel.setText(item.canselPriceKop / 100.0f + "р");
                dateLabel.setText(item.orderDate.toString());

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
