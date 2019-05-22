package net.manicmachine.controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainWindow extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../view/Main.fxml"));
        Parent root = fxmlLoader.load();

        MainController mainController = fxmlLoader.getController();
        mainController.setStage(primaryStage);

        primaryStage.setMinHeight(450);
        primaryStage.setMinWidth(316);
        primaryStage.setWidth(primaryStage.getMinWidth());

        Scene scene = new Scene(root, primaryStage.getMinWidth(), primaryStage.getMaxHeight());

        primaryStage.setTitle("Login Status Checker");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Cleanup resources before closing
        primaryStage.setOnCloseRequest((event) -> {
            mainController.closePsSession();
            mainController.closeThreadPool();
        });
    }

}
