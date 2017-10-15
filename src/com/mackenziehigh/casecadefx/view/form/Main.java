package com.mackenziehigh.casecadefx.view.form;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
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
        //FormDialog.showDialog();

        final String css = FormDialog.class.getResource("/Styles.css").toExternalForm();

        final FormPane f = new FormPane("TestForm", css);
        f.addLabel("Lab1").alignText(Pos.TOP_RIGHT).label().setText("Mars");
        f.addLabel("Lab2").setBackgroundColor(Color.RED).label().setText("Venus");
        f.addTextField("Lab3").field().setText("Vulcan");
        f.addTextArea("Lab4").area().setText("Saturn");
        f.addListBox("Lab5").listView().getItems().add("Pluto");
        f.addChoiceBox("Lab6").choiceBox().getItems().add("Autumn");
        f.addPagination("Lab7").pagination().setPageCount(7);
        f.addRadioButton("Lab8", "Jovian").button().setText("Jupiter");
        f.addRadioButton("Lab9", "Jovian").button().setText("Uranus");
        f.addCheckBox("Lab10").checkbox().setText("Apollo Rocket?");

        f.setSpacing(30);
        f.setPadding(30, 30, 30, 30);

        final AnchorPane root2 = new AnchorPane(f);
        final ScrollPane root1 = new ScrollPane(root2);
        root1.setFitToWidth(true);
        AnchorPane.setTopAnchor(f, 0.0);
        AnchorPane.setRightAnchor(f, 0.0);
        AnchorPane.setBottomAnchor(f, 0.0);
        AnchorPane.setLeftAnchor(f, 0.0);

        final Scene scene = new Scene(root1, 900, 700);

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
