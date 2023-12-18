package com.quartyom.courseclient.SceneControllers;

import com.quartyom.QuPackets.LoginData;
import com.quartyom.QuPackets.QuPacket;
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

public class RegisterSceneController extends SceneController{
    private static final Logger scenesLog = LoggerFactory.getLogger("scenes");
    private static final Logger serverLog = LoggerFactory.getLogger("server");
    Parent root;
    Label wrongLabel;
    TextField loginText, passwordText, repeatText;
    Button enterButton, regButton;

    public RegisterSceneController(QuApplication application) {
        super(application);
        try {
            root = FXMLLoader.load(application.getClass().getResource("register-view.fxml"));

            wrongLabel = (Label) root.lookup("#wrongLabel");
            loginText = (TextField) root.lookup("#loginText");
            passwordText = (TextField) root.lookup("#passwordText");
            repeatText = (TextField) root.lookup("#repeatText");
            enterButton = (Button) root.lookup("#enterButton");
            regButton = (Button) root.lookup("#regButton");

            enterButton.setOnAction(action -> {
                application.setScene(AuthSceneController.class);
            });
            regButton.setOnAction(action -> {
                if (register()) {
                    application.setScene(AuthSceneController.class);
                }
                else {
                    wrongLabel.setVisible(true);
                }
            });

        }
        catch (IOException e) {
            scenesLog.info(e.getMessage());
        }

        scene = new Scene(root);
    }

    public boolean register(){
        String login = loginText.getText();
        String password = passwordText.getText();
        String repeat = repeatText.getText();

        if (Validator.minLoginLen > login.length() || login.length() > Validator.maxLoginLen){
            wrongLabel.setText("Длина логина должна быть от %d до %d".formatted(Validator.minLoginLen, Validator.maxLoginLen));
            scenesLog.info("wrong login input");
            return false;
        }
        else if (Validator.minPasswordLen > password.length() || password.length() > Validator.maxPasswordLen){
            wrongLabel.setText("Длина пароля должна быть от %d до %d".formatted(Validator.minLoginLen, Validator.maxLoginLen));
            scenesLog.info("wrong password input");
            return false;
        }
        else if (!password.equals(repeat)){
            wrongLabel.setText("Пароли не совпадают");
            scenesLog.info("passwords not same");
            return false;
        }

        QuPacket packet = new QuPacket("register");
        LoginData data = new LoginData();
        data.login = login;
        data.password = password;
        packet.data = data;

        try {
            QuPacket response = ServerAdapter.doRequest(application.properties, packet);
            if (response != null) {
                if (response.type.equals("ok")){
                    serverLog.info("registered");
                    return true;
                }
                else {
                    wrongLabel.setText((String)response.data);
                    serverLog.info("not registered");
                    return false;
                }
            }
            else {
                wrongLabel.setText("Некорректный ответ сервера");
                serverLog.error("invalid response");
            }
        } catch (IOException e) {
            wrongLabel.setText("Ошибка связи с сервером");
            serverLog.error(e.getMessage());
        }
        return false;
    }

    @Override
    public void update() {
        wrongLabel.setVisible(false);
    }

    @Override
    public void stop() {}
}
