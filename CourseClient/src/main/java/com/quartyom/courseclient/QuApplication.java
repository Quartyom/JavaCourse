package com.quartyom.courseclient;

import com.quartyom.courseclient.SceneControllers.*;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Properties;

public class QuApplication extends Application {
    public Stage stage;
    public final Properties properties = new Properties();
    private static final Logger log = LoggerFactory.getLogger(QuApplication.class);

    private HashMap<Class,SceneController> sceneControllers;
    public Long sessionId;
    public String login;
    public AppData appData;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        log.info("app started");
        readProperties();
        loadAppState();

        sceneControllers = new HashMap<>();
        addSceneController(new AuthSceneController(this));
        addSceneController(new RegisterSceneController(this));
        addSceneController(new ResultSceneController(this));
        addSceneController(new MainMenuSceneController(this));
        setScene(AuthSceneController.class);

        stage.setTitle("Adv agency client");
        stage.setMinWidth(600);
        stage.setMinHeight(400);
        stage.getIcons().add(new Image("logo.png"));    // hardcoded, don't change
        stage.setOnCloseRequest( actionEvent -> sceneControllers.forEach((k, v) -> v.stop()) ); // закрыть все сцены

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public void saveAppState(){
        try (FileOutputStream fos = new FileOutputStream(properties.getProperty("appDataFile"));
             ObjectOutputStream oos = new ObjectOutputStream(fos))
        {
            oos.writeObject(appData);
            log.info("Saved app state");
        } catch (IOException e) {
            log.error("Couldn't save app state, " + e.getMessage());
        }
    }

    public void loadAppState(){
        try (FileInputStream fis = new FileInputStream(properties.getProperty("appDataFile"));
            ObjectInputStream ois = new ObjectInputStream(fis))
        {
            appData = (AppData) ois.readObject();
            log.info("Loaded app state");
        }
        catch (IOException | ClassNotFoundException e) {
            appData = new AppData();
            log.error("Couldn't load app state, " + e.getMessage());
        }
    }

    public void readProperties(){
        try (InputStream input = new FileInputStream("app.properties")) {
            properties.load(input);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void addSceneController(SceneController sceneController){
        sceneControllers.put(sceneController.getClass(), sceneController);
    }

    private SceneController currentSceneController;

    public void setScene(Class name){
        if (currentSceneController != null) {
            currentSceneController.stop();
        }
        currentSceneController = sceneControllers.get(name);
        currentSceneController.update();
        stage.setScene(currentSceneController.getScene());
        log.info("Set scene " + name);
    }

    public void setScene(SceneController sceneController){
        addSceneController(sceneController);
        setScene(sceneController.getClass());
    }

    public void showResult(String labelText, String textAreaText, Class whereToReturn){
        ResultSceneController.labelText = labelText;
        ResultSceneController.textAreaText = textAreaText;
        ResultSceneController.whereToReturn = whereToReturn;
        setScene(ResultSceneController.class);
    }


}