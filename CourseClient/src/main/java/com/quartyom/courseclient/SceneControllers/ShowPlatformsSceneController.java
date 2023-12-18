package com.quartyom.courseclient.SceneControllers;

import com.quartyom.QuPackets.AdvPlatform;
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
import java.util.HashSet;

public class ShowPlatformsSceneController extends SceneController{
    Parent root;
    Label depositLabel;
    Button backButton, popupButton;
    ScrollPane scrollPane;
    ObservableList<Parent> items;

    URL tileResource;

    private static final Logger scenesLog = LoggerFactory.getLogger("scenes");
    private static final Logger serverLog = LoggerFactory.getLogger("server");

    public ShowPlatformsSceneController(QuApplication application) {
        super(application);
        try {
            root = FXMLLoader.load(application.getClass().getResource("show-platforms-view.fxml"));
            tileResource = application.getClass().getResource("adv-platform-tile.fxml");

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

    public ArrayList<AdvPlatform> getAdvPlatforms(){
        try {
            QuPacket response = ServerAdapter.doRequest(application.properties, new QuPacket("getAdvPlatforms"));
            if (response != null) {
                try {
                    return (ArrayList<AdvPlatform>) response.data;
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

    private String getDescriptionText(AdvPlatform platform){
        return "%s: %s, место %d".formatted(platform.subtype, platform.description, platform.spot);
    }
    public void getUserDeposit(boolean toPopup){
        try {
            QuPacket packet = new QuPacket(toPopup ? "popupUserDeposit" : "getUserDeposit");
            packet.sessionId = application.sessionId;
            QuPacket response = ServerAdapter.doRequest(application.properties, packet);
            if (response != null) {
                switch (response.type) {
                    case "unauthorized":
                        AuthSceneController.whereToReturn = new ShowPlatformsSceneController(application);
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

    @Override
    public void update() {
        getUserDeposit(false);
        HashSet<Long> favouritePlatforms = application.appData.favouritePlatforms;

        items.clear();
        for (AdvPlatform item : getAdvPlatforms()){
            try {
                Parent tile = FXMLLoader.load(tileResource);

                Label descriptionLabel = (Label) tile.lookup("#descriptionLabel");
                Label priceLabel = (Label) tile.lookup("#priceLabel");
                Label freeDatesLabel = (Label) tile.lookup("#freeDatesLabel");
                Button detailsButton = (Button) tile.lookup("#detailsButton");
                Button toFavButton = (Button) tile.lookup("#toFavButton");

                String description = getDescriptionText(item);
                descriptionLabel.setText(description);
                String price = item.priceKop / 100.0f + "p";
                priceLabel.setText(price);
                freeDatesLabel.setText("%d свободных дат".formatted(item.kFreeDates));

                detailsButton.setOnAction(action -> {
                    FreeDatesSceneController.advPlatformId = item.id;
                    FreeDatesSceneController.description = description;
                    FreeDatesSceneController.price = price;
                    application.setScene(new FreeDatesSceneController(application));
                });

                if (favouritePlatforms.contains(item.id)) {
                    tile.setStyle("-fx-background-color: rgb(150,255,50)");
                    toFavButton.setText("Убрать");
                    toFavButton.setOnAction(action -> {
                        favouritePlatforms.remove(item.id);
                        application.saveAppState();
                        tile.setStyle("-fx-background-color: white");
                        toFavButton.setText("В избранное");
                    });
                    items.add(0,tile);  // в начало
                }
                else {
                    tile.setStyle("-fx-background-color: white");
                    toFavButton.setText("В избранное");
                    toFavButton.setOnAction(action -> {
                        favouritePlatforms.add(item.id);
                        application.saveAppState();
                        tile.setStyle("-fx-background-color: rgb(150,255,50)");
                        toFavButton.setText("Убрать");
                    });
                    items.add(tile);
                }

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
