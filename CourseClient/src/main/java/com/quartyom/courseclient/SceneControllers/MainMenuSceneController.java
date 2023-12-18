package com.quartyom.courseclient.SceneControllers;

import com.quartyom.QuPackets.QuPacket;
import com.quartyom.courseclient.QuApplication;
import com.quartyom.courseclient.ServerAdapter;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MainMenuSceneController extends SceneController{
    Parent root;
    Label loginLabel;
    TextArea textArea;
    Button adButton, ordersButton, historyButton, exitButton;

    private static final Logger scenesLog = LoggerFactory.getLogger("scenes");
    private static final Logger serverLog = LoggerFactory.getLogger("server");

    public MainMenuSceneController(QuApplication application) {
        super(application);
        try {
            root = FXMLLoader.load(application.getClass().getResource("main-menu-view.fxml"));

            loginLabel = (Label) root.lookup("#loginLabel");
            textArea = (TextArea) root.lookup("#textArea");
            adButton = (Button) root.lookup("#adButton");
            ordersButton = (Button) root.lookup("#ordersButton");
            historyButton = (Button) root.lookup("#historyButton");
            exitButton = (Button) root.lookup("#exitButton");

            ordersButton.setOnAction(action -> application.setScene(new ShowOrdersSceneController(application)));
            adButton.setOnAction(action -> application.setScene(new ShowPlatformsSceneController(application)));
            historyButton.setOnAction(action -> application.setScene(new ShowHistorySceneController(application)));

            exitButton.setOnAction(action -> {
                application.setScene(AuthSceneController.class);
            });


        }
        catch (IOException e) {
            serverLog.error(e.getMessage());
        }

        scene = new Scene(root);
    }

    @Override
    public void update() {
        if (textArea != null) {
            textArea.setText("");
        }
        if (application.login != null){
            loginLabel.setText(application.login);
        }
    }

    @Override
    public void stop() {}
}
