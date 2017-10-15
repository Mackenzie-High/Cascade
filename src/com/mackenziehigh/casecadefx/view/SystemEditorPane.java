package com.mackenziehigh.casecadefx.view;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

/**
 *
 * @author mackenzie
 */
public final class SystemEditorPane
        extends AnchorPane
{

    private final MenuItem itemNewGrid = new MenuItem("New Grid");

    private final MenuItem itemCopy = new MenuItem("Copy");

    private final MenuItem itemRename = new MenuItem("Rename");

    private final MenuItem itemRemove = new MenuItem("Delete");

    private final MenuItem itemAddEnable = new MenuItem("Enable");

    private final MenuItem itemAddDisable = new MenuItem("Disable");

    private final ContextMenu contextMenu = new ContextMenu();

    private final ListView<String> parts = new ListView<>();

    private final AllocatorPane allocatorEditor = new AllocatorPane();

    private final PowerplantPane powerplantEditor = new PowerplantPane();

    private final ActorGridPane actorGridEditor = new ActorGridPane();

    private final Pane content = new Pane();

    private final SplitPane split = new SplitPane(parts, content);

    public SystemEditorPane ()
    {
        contextMenu.getItems().add(itemNewGrid);
        contextMenu.getItems().add(new SeparatorMenuItem());
        contextMenu.getItems().add(itemCopy);
        contextMenu.getItems().add(itemRename);
        contextMenu.getItems().add(itemRemove);
        contextMenu.getItems().add(new SeparatorMenuItem());
        contextMenu.getItems().add(itemAddEnable);
        contextMenu.getItems().add(itemAddDisable);
        parts.setContextMenu(contextMenu);

        getChildren().add(split);

        AnchorPane.setTopAnchor(split, 0.0);
        AnchorPane.setBottomAnchor(split, 0.0);
        AnchorPane.setLeftAnchor(split, 0.0);
        AnchorPane.setRightAnchor(split, 0.0);

        split.setDividerPositions(0.2);

        parts.getItems().add("AAA");
        parts.getItems().add("BBB");
        parts.getItems().add("CCC");

        showGridEditor();
    }

    public void showAllocatorEditor ()
    {

    }

    public void showPowerplantEditor ()
    {

    }

    public void showGridEditor ()
    {
        content.getChildren().clear();
        content.getChildren().add(actorGridEditor);
    }
}
