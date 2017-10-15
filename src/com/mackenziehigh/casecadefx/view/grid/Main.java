package com.mackenziehigh.casecadefx.view.grid;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 *
 * @author mackenzie
 */
public class Main
        extends Application
{

    @Override
    public void start (Stage primaryStage)
    {
        final TileGridPane grid = new TileGridPane(64, 21);

        final AnchorPane root = new AnchorPane(grid);
        AnchorPane.setTopAnchor(grid, 0.0);
        AnchorPane.setRightAnchor(grid, 0.0);
        AnchorPane.setBottomAnchor(grid, 0.0);
        AnchorPane.setLeftAnchor(grid, 0.0);

        final Scene scene = new Scene(root, 900, 700);

        primaryStage.setTitle("Main");
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
