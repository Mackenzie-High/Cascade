package com.mackenziehigh.casecadefx.view.grid;

import com.google.common.base.Preconditions;
import javafx.event.EventType;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

/**
 * TODO: Only need to redraw tiles on change. Most diagrams are sparse!
 */
public final class TileGridPane
        extends BorderPane
{
    public interface TileSupplier
    {
        public Tile get (int y,
                         int x);
    }

    public interface Tile
    {

    }

    private final int viewportHeight;

    private final int viewportWidth;

    private final int tileSize;

    private final MenuItem itemActionA = new MenuItem("Action: A");

    private final MenuItem itemActionB = new MenuItem("Action: B");

    private final MenuItem itemActionC = new MenuItem("Action: C");

    private final MenuItem itemActionD = new MenuItem("Action: D");

    private final ContextMenu tileMenu = new ContextMenu();

    private final ScrollPane tilesScroll = new ScrollPane();

    private double zoomLevel = 1;

    private final int[][][] viewportMap;

    private final Button buttonMoveNorth = new Button("N");

    private final Button buttonMoveSouth = new Button("S");

    private final Button buttonMoveWest = new Button("W");

    private final Button buttonMoveEast = new Button("E");

    private final AnchorPane anchorNorth = new AnchorPane(buttonMoveNorth);

    private final AnchorPane anchorSouth = new AnchorPane(buttonMoveSouth);

    private final AnchorPane anchorEast = new AnchorPane(buttonMoveEast);

    private final AnchorPane anchorWest = new AnchorPane(buttonMoveWest);

    private final Canvas canvas;

    private final GraphicsContext context;

    /**
     * Sole Constructor.
     *
     * @param tileSize is the height and width of each tile.
     * @param pillarCount is the number of rows and columns.
     */
    public TileGridPane (final int tileSize,
                         final int pillarCount)
    {
        Preconditions.checkArgument(tileSize > 0, "tileSize <= 0");
        Preconditions.checkArgument(pillarCount % 2 != 0, "pillarCount must be odd");

        this.tileSize = tileSize;
        this.viewportHeight = pillarCount;
        this.viewportWidth = pillarCount;
        this.viewportMap = new int[viewportHeight][viewportWidth][2];
        this.canvas = new Canvas(viewportWidth * tileSize, viewportHeight * tileSize);
        this.context = canvas.getGraphicsContext2D();

        tilesScroll.setPannable(false);
        tilesScroll.setHvalue(0.5);
        tilesScroll.setVvalue(0.5);
        tilesScroll.setFitToHeight(true);
        tilesScroll.setFitToWidth(true);
        tilesScroll.setContent(canvas);
        tilesScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        tilesScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        tilesScroll.hvalueProperty().addListener((observable, oldValue, newValue) -> tilesScroll.setHvalue(0.5));
        tilesScroll.vvalueProperty().addListener((observable, oldValue, newValue) -> zoom(newValue.doubleValue()));

        tilesScroll.setOnMouseMoved(e ->
        {
            //System.out.println("Y = " + e.getY() + ", MinY = " + (tilesScroll.getViewportBounds().getMinY()) + ", MaxY = " + tilesScroll.getViewportBounds().getMaxY());
            System.out.println("Y = " + e.getY() + ", H = " + tilesScroll.getHeight());
            System.out.println(e);
            if (e.getY() <= tilesScroll.getHeight() / 10)
            {
                System.out.println("Up");
                moveUp();
            }
            else if (e.getY() >= 9 * (tilesScroll.getHeight() / 10))
            {
                System.out.println("Down");
                moveDown();
            }
            else
            {
                System.out.println("No Move");
            }
        });

        tileMenu.getItems().add(itemActionA);
        tileMenu.getItems().add(itemActionB);
        tileMenu.getItems().add(itemActionC);
        tileMenu.getItems().add(itemActionD);

        buttonMoveNorth.setOnAction(e -> moveUp());
        buttonMoveSouth.setOnAction(e -> moveDown());
        buttonMoveWest.setOnAction(e -> moveLeft());
        buttonMoveEast.setOnAction(e -> moveRight());

        itemActionA.addEventHandler(EventType.ROOT, e -> moveUp());
        itemActionB.addEventHandler(EventType.ROOT, e -> moveDown());
        itemActionC.addEventHandler(EventType.ROOT, e -> moveLeft());
        itemActionD.addEventHandler(EventType.ROOT, e -> moveRight());

        canvas.setOnContextMenuRequested(e -> showTileContextMenu(e));
        canvas.setOnMouseClicked(e -> onMouseClick(e));

        setTop(anchorNorth);
        setRight(anchorEast);
        setBottom(anchorSouth);
        setLeft(anchorWest);
        setCenter(tilesScroll);
        setAlignment(tilesScroll, Pos.CENTER);

        AnchorPane.setTopAnchor(tilesScroll, 0.0);
        AnchorPane.setBottomAnchor(tilesScroll, 0.0);
        AnchorPane.setLeftAnchor(tilesScroll, 0.0);
        AnchorPane.setRightAnchor(tilesScroll, 0.0);

        /**
         * Make the NORTH button span across the top.
         */
        AnchorPane.setLeftAnchor(buttonMoveNorth, 0.0);
        AnchorPane.setRightAnchor(buttonMoveNorth, 0.0);

        /**
         * Make the SOUTH button span across the bottom.
         */
        AnchorPane.setLeftAnchor(buttonMoveSouth, 0.0);
        AnchorPane.setRightAnchor(buttonMoveSouth, 0.0);

        /**
         * Make the EAST button span from top to bottom.
         */
        AnchorPane.setTopAnchor(buttonMoveEast, 0.0);
        AnchorPane.setBottomAnchor(buttonMoveEast, 0.0);

        /**
         * Make the WEST button span from top to bottom.
         */
        AnchorPane.setTopAnchor(buttonMoveWest, 0.0);
        AnchorPane.setBottomAnchor(buttonMoveWest, 0.0);

        final int limit = (pillarCount - 1) / 2;
        for (int y = -limit; y < limit; y++)
        {
            for (int x = -limit; x < limit; x++)
            {
                final int adjustedY = limit + y;
                final int adjustedX = limit + x;
                viewportMap[adjustedY][adjustedX][0] = -y;
                viewportMap[adjustedY][adjustedX][1] = x;
                drawCell(adjustedX, adjustedY);
            }
        }
    }

    private void zoom (final double value)
    {
        if (value < 0.5)
        {
            zoomLevel = zoomLevel * 1.25;
            canvas.scaleXProperty().setValue(zoomLevel);
            canvas.scaleYProperty().setValue(zoomLevel);
        }
        else if (value > 0.5)
        {
            zoomLevel = zoomLevel * 0.80;
            canvas.scaleXProperty().setValue(zoomLevel);
            canvas.scaleYProperty().setValue(zoomLevel);
        }

        tilesScroll.setVvalue(0.5);
        tilesScroll.setHvalue(0.5);
    }

    private void showTileContextMenu (final ContextMenuEvent event)
    {
        final int col = ((int) event.getX() / tileSize);
        final int row = viewportHeight - ((int) (event.getY() / tileSize)) - 1;
        System.out.println("TCM = (" + col + ", " + row + ")");
        tileMenu.show(canvas, event.getScreenX(), event.getScreenY());
    }

    private void onMouseClick (final MouseEvent event)
    {
        System.out.println("XXX " + event);
        if (event.getButton().equals(MouseButton.PRIMARY))
        {
            final int col = ((int) event.getX() / tileSize);
            final int row = viewportHeight - ((int) (event.getY() / tileSize)) - 1;
            System.out.println("OnClick = (" + col + ", " + row + ")");
        }
    }

    /**
     * Move the viewport up one row.
     */
    private void moveUp ()
    {
        for (int y = 0; y < viewportHeight; y++)
        {
            for (int x = 0; x < viewportWidth; x++)
            {
                viewportMap[y][x][0] = viewportMap[y][x][0] + 1;
                viewportMap[y][x][1] = viewportMap[y][x][1] + 0;
                drawCell(x, y);
            }
        }
    }

    /**
     * Move the viewport down one row.
     */
    private void moveDown ()
    {
        for (int y = 0; y < viewportHeight; y++)
        {
            for (int x = 0; x < viewportWidth; x++)
            {
                viewportMap[y][x][0] = viewportMap[y][x][0] - 1;
                viewportMap[y][x][1] = viewportMap[y][x][1] + 0;
                drawCell(x, y);
            }
        }
    }

    /**
     * Move the viewport left one column.
     */
    private void moveLeft ()
    {
        for (int y = 0; y < viewportHeight; y++)
        {
            for (int x = 0; x < viewportWidth; x++)
            {
                viewportMap[y][x][0] = viewportMap[y][x][0] + 0;
                viewportMap[y][x][1] = viewportMap[y][x][1] - 1;
                drawCell(x, y);
            }
        }
    }

    /**
     * Move the viewport right one column.
     */
    private void moveRight ()
    {
        for (int y = 0; y < viewportHeight; y++)
        {
            for (int x = 0; x < viewportWidth; x++)
            {
                viewportMap[y][x][0] = viewportMap[y][x][0] + 0;
                viewportMap[y][x][1] = viewportMap[y][x][1] + 1;
                drawCell(x, y);
            }
        }
    }

    private void drawCell (final int x,
                           final int y)
    {
        final double xpos = x * tileSize;
        final double ypos = y * tileSize;
        context.setFill((x + y) % 2 == 0 ? Color.RED : Color.BLUE);
        context.fillRect(xpos, ypos, tileSize, tileSize);
        context.setStroke(Color.BLACK);
        context.strokeText("(" + viewportMap[y][x][1] + "," + viewportMap[y][x][0] + ")", xpos, ypos + 25);
        context.stroke();
    }
}
