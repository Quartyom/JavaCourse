package com.quartyom.courseclient.SceneControllers;

import com.quartyom.courseclient.QuApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.io.IOException;

public class ResultSceneController extends SceneController{

    Parent root;
    Label label;
    TextArea textArea;
    Button okButton;

    public static String labelText;
    public static String textAreaText;
    public static Class whereToReturn;
    public static SceneController whereToReturnObject;

    public ResultSceneController(QuApplication application) {
        super(application);
        try {
            root = FXMLLoader.load(application.getClass().getResource("result-view.fxml"));

            label = (Label) root.lookup("#label");
            textArea = (TextArea) root.lookup("#textArea");
            okButton = (Button) root.lookup("#okButton");

        }
        catch (IOException e) {
        }

        scene = new Scene(root);
    }


    @Override
    public void update() {
        label.setText(labelText);
        textArea.setText(textAreaText);
        okButton.setOnAction(actionEvent -> {
            if (whereToReturnObject != null) {
                application.setScene(whereToReturnObject);
            }
            else if (whereToReturn != null) {
                application.setScene(whereToReturn);
            }
        });
    }

    @Override
    public void stop() {}
}
