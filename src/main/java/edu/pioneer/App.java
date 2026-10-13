package edu.pioneer;

import edu.pioneer.ui.MainWindow;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Launches the Pioneer Delivery Operator Workbench. */
public final class App extends Application {
    @Override
    public void start(Stage stage) {
        MainWindow window = new MainWindow(stage);
        Scene scene = new Scene(window, 1120, 760);
        scene.getStylesheets().add(App.class.getResource("/workbench.css").toExternalForm());
        stage.setTitle("Pioneer Delivery — Delivery Analysis");
        stage.setMinWidth(800);
        stage.setMinHeight(560);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] arguments) {
        launch(arguments);
    }
}
