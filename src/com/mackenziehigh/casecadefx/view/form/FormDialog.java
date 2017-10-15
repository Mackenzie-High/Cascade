package com.mackenziehigh.casecadefx.view.form;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

/**
 *
 */
public final class FormDialog
        extends Application
{
    private FormDialog ()
    {
        // Pass
    }

    @Override
    public void start (final Stage stage)
            throws Exception
    {
        Circle circ = new Circle(40, 40, 30);
        Group root = new Group(circ);
        Scene scene = new Scene(root, 400, 300);

        stage.setTitle("My JavaFX Application");
        stage.setScene(scene);
        stage.show();
    }

    public static void showDialog ()
    {
        launch();
    }
}
