package com.quartyom.courseclient.SceneControllers;

import com.quartyom.QuPackets.LoginData;
import com.quartyom.QuPackets.QuPacket;
import com.quartyom.courseclient.AppData;
import com.quartyom.courseclient.QuApplication;
import com.quartyom.courseclient.ServerAdapter;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Properties;

public class AuthSceneController extends SceneController{
    private static final Logger scenesLog = LoggerFactory.getLogger("scenes");
    private static final Logger serverLog = LoggerFactory.getLogger("server");
    Parent root;
    Label wrongLabel;
    TextField loginText, passwordText;
    Button enterButton, regButton;
    public static SceneController whereToReturn;

    public AuthSceneController(QuApplication application) {
        super(application);
        try {
            root = FXMLLoader.load(application.getClass().getResource("auth-view.fxml"));

            wrongLabel = (Label) root.lookup("#wrongLabel");
            loginText = (TextField) root.lookup("#loginText");
            passwordText = (TextField) root.lookup("#passwordText");
            enterButton = (Button) root.lookup("#enterButton");
            regButton = (Button) root.lookup("#regButton");

            enterButton.setOnAction(action -> {
                if (auth()){
                    if (whereToReturn != null) {
                        application.setScene(whereToReturn);
                    }
                    else {
                        application.setScene(MainMenuSceneController.class);
                    }
                }
                else {
                    wrongLabel.setVisible(true);
                }
                passwordText.clear();
            });
            regButton.setOnAction(action -> {
                application.setScene(RegisterSceneController.class);
            });

        }
        catch (IOException e) {
            scenesLog.error(e.getMessage());
        }

        scene = new Scene(root);
    }

    public boolean auth(){
        String login = loginText.getText();
        String password = passwordText.getText();

        if (Validator.minLoginLen > login.length() || login.length() > Validator.maxLoginLen){
            wrongLabel.setText("Длина логина должна быть от %d до %d".formatted(Validator.minLoginLen, Validator.maxLoginLen));
            scenesLog.info("wrong login input");
            return false;
        }
        else if (Validator.minPasswordLen > password.length() || password.length() > Validator.maxPasswordLen){
            wrongLabel.setText("Длина пароля должна быть от %d до %d".formatted(Validator.minPasswordLen, Validator.maxPasswordLen));
            scenesLog.info("wrong password input");
            return false;
        }

        QuPacket packet = new QuPacket("auth");
        LoginData data = new LoginData();
        data.login = loginText.getText();
        data.password = passwordText.getText();
        packet.data = data;

        try {
            QuPacket response = ServerAdapter.doRequest(application.properties, packet);
            if (response != null) {
                if (response.type.equals("ok")){
                    application.sessionId = response.sessionId;
                    application.login = login;
                    application.appData.login = login;
                    application.appData.password = password;
                    application.saveAppState();
                    serverLog.info("logged in");
                    return true;
                }
                else {
                    wrongLabel.setText((String) response.data);
                    serverLog.info("not logged");
                    return false;
                }
            }
            else {
                wrongLabel.setText("Некорректный ответ сервера");
                serverLog.error("invalid response");
            }
        } catch (IOException e) {
            serverLog.error(e.getMessage());
        }
        wrongLabel.setText("Ошибка связи с сервером");
        return false;
    }


    @Override
    public void update() {
        wrongLabel.setVisible(false);

        AppData appData = application.appData;
        if (appData != null) {
            loginText.setText(appData.login);
            passwordText.setText(appData.password);
        }
        else {
            passwordText.clear();
        }
    }

    @Override
    public void stop() {}
}
