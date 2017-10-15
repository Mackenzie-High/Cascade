package com.mackenziehigh.casecadefx;

import com.mackenziehigh.casecadefx.view.MainPane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 *
 * @author mackenzie
 */
public class CascadeFX
        extends Application
{

    @Override
    public void start (Stage primaryStage)
    {

        final MainPane main = new MainPane();

        StackPane root = new StackPane();
        root.getChildren().add(main);

        Scene scene = new Scene(root, 900, 700);

        primaryStage.setTitle("Cascade");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main (String[] args)
    {
        launch(args);

    }

}
