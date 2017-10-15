package com.mackenziehigh.casecadefx.view;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

/**
 *
 * @author mackenzie
 */
public final class MainPane
        extends BorderPane
{
    private final MenuItem itemOpen = new MenuItem("Open");

    private final MenuItem itemSaveAs = new MenuItem("Save As");

    private final MenuItem itemSave = new MenuItem("Save");

    private final MenuItem itemExit = new MenuItem("Exit");

    private final MenuItem itemAbout = new MenuItem("About");

    private final Menu menuFile = new Menu("File");

    private final Menu menuHelp = new Menu("Help");

    private final MenuBar menuBar = new MenuBar();

    private final AnchorPane overlay = new AnchorPane();

    private final SystemEditorPane systemEditor = new SystemEditorPane();

    private final ActorEditorPane actorEditor = new ActorEditorPane();

    private final AllocatorPane allocatorEditor = new AllocatorPane();

    private final PowerplantPane powerplantEditor = new PowerplantPane();

    private final EnvironmentPane environmentEditor = new EnvironmentPane();

    private final IncludesPane includesEditor = new IncludesPane();

    private final Tab tab1 = new Tab("Grid", systemEditor);

    private final Tab tab2 = new Tab("Actors", actorEditor);

    private final Tab tab3 = new Tab("Allocators", allocatorEditor);

    private final Tab tab4 = new Tab("Powerplants", powerplantEditor);

    private final Tab tab5 = new Tab("Environment", environmentEditor);

    private final Tab tab6 = new Tab("Includes", includesEditor);

    private final TabPane tabs = new TabPane(tab1, tab2, tab3, tab4, tab5, tab6);

    private final StackPane stack = new StackPane(tabs);

    public MainPane ()
    {
        menuFile.getItems().add(itemOpen);
        menuFile.getItems().add(itemSaveAs);
        menuFile.getItems().add(itemSave);
        menuFile.getItems().add(new SeparatorMenuItem());
        menuFile.getItems().add(itemExit);
        menuBar.getMenus().add(menuFile);
        menuHelp.getItems().add(itemAbout);
        menuBar.getMenus().add(menuHelp);

        tabs.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

        setTop(menuBar);
        setCenter(stack);

        tabs.setStyle("-fx-background-color: LIGHTGREY;");
    }
}
