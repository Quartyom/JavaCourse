package com.quartyom.courseclient.SceneControllers;

import com.quartyom.courseclient.QuApplication;
import javafx.scene.Scene;

import java.util.Properties;

public abstract class SceneController {
    protected Scene scene;

    protected QuApplication application;
    protected Properties properties;

    public SceneController(QuApplication application){
        this.application = application;
        this.properties = application.properties;
    }

    public Scene getScene(){
        return scene;
    }

    public abstract void update();
    public abstract void stop();
}
