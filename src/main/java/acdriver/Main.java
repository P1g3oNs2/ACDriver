/**
 * big shoutout to karlaparla for jvjoyinterface
 * clash of clans best player kakanevihmauss
 */

package acdriver;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        //System.setProperty("net.java.games.input.librarypath", new File("lib").getAbsolutePath()); // stupid fucking piece of shit
        //System.setProperty("jvjoyinterface", new File("lib").getAbsolutePath());
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 480, 360);
        stage.setTitle("ACDriver");
        stage.setScene(scene);
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}