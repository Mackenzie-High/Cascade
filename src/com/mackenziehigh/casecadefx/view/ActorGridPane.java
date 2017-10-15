package com.mackenziehigh.casecadefx.view;

import java.util.function.Consumer;
import java.util.stream.IntStream;
import javafx.application.Platform;
import javafx.event.EventType;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;

/**
 *
 */
public final class ActorGridPane
        extends AnchorPane
{

    private static final int VIEWPORT_HEIGHT = 21;

    private static final int VIEWPORT_WIDTH = 21;

    private static final int TILE_SIZE = 128;

    private final MenuItem itemAddAction = new MenuItem("Add: Action");

    private final MenuItem itemAddConnectionCB = new MenuItem("Add: CB");

    private final MenuItem itemAddConnectionBC = new MenuItem("Add: BC");

    private final MenuItem itemActionA = new MenuItem("Action: A");

    private final MenuItem itemActionB = new MenuItem("Action: B");

    private final MenuItem itemActionC = new MenuItem("Action: C");

    private final MenuItem itemActionD = new MenuItem("Action: D");

    private final ContextMenu tileMenu = new ContextMenu();

    private final TilePane tiles = new TilePane();

    private final ScrollPane tilesScroll = new ScrollPane();

    private double zoomLevel = 1;

    private final Canvas[][] viewport = new Canvas[VIEWPORT_HEIGHT][VIEWPORT_WIDTH];

    private final int[][][] viewportMap = new int[VIEWPORT_HEIGHT][VIEWPORT_WIDTH][2];

    private final Button buttonMoveNorth = new Button("N");

    private final Button buttonMoveSouth = new Button("S");

    private final Button buttonMoveWest = new Button("W");

    private final Button buttonMoveEast = new Button("E");

    private final Button buttonMoveNorthWest = new Button("NW");

    private final Button buttonMoveNorthEast = new Button("NE");

    private final Button buttonMoveSouthWest = new Button("SW");

    private final Button buttonMoveSouthEast = new Button("SE");

    private final Button buttonCenter = new Button("C");

    private final GridPane movementController = new GridPane();

    public ActorGridPane ()
    {

        movementController.add(buttonMoveNorthWest, 0, 0);
        movementController.add(buttonMoveNorth, 0, 1);
        movementController.add(buttonMoveNorthEast, 0, 2);
        movementController.add(buttonMoveWest, 1, 0);
        movementController.add(buttonCenter, 1, 1);
        movementController.add(buttonMoveEast, 1, 2);
        movementController.add(buttonMoveSouthWest, 2, 0);
        movementController.add(buttonMoveSouth, 2, 1);
        movementController.add(buttonMoveSouthEast, 2, 2);

        final Group tilesGroup = new Group(tiles);

        tilesScroll.setOnScroll(x -> System.out.println("Scroll"));
        tilesScroll.setPrefSize(700, 700);
        tilesScroll.setPannable(true);
        tilesScroll.setContent(tilesGroup);
        tilesScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        tilesScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        tileMenu.getItems().add(itemActionA);
        tileMenu.getItems().add(itemActionB);
        tileMenu.getItems().add(itemActionC);
        tileMenu.getItems().add(itemActionD);

        itemActionA.addEventHandler(EventType.ROOT, e -> moveUp());
        itemActionB.addEventHandler(EventType.ROOT, e -> moveDown());
        itemActionC.addEventHandler(EventType.ROOT, e -> moveLeft());
        itemActionD.addEventHandler(EventType.ROOT, e -> moveRight());

        getChildren().add(tilesScroll);

        AnchorPane.setTopAnchor(tilesScroll, 0.0);
        AnchorPane.setBottomAnchor(tilesScroll, 0.0);
        AnchorPane.setLeftAnchor(tilesScroll, 0.0);
        AnchorPane.setRightAnchor(tilesScroll, 0.0);

        tiles.setAlignment(Pos.CENTER);
        tiles.setPrefColumns(VIEWPORT_WIDTH);
        tiles.setPrefRows(VIEWPORT_HEIGHT);

        for (int y = 0; y < VIEWPORT_HEIGHT; y++)
        {
            for (int x = 0; x < VIEWPORT_WIDTH; x++)
            {
                final int adjustedY = VIEWPORT_HEIGHT - y - 1;
                final int adjustedX = x;
                final Canvas cell = new Canvas(TILE_SIZE, TILE_SIZE);
                cell.setOnContextMenuRequested(e -> tileMenu.show(cell, e.getScreenX(), e.getScreenY()));
                viewport[adjustedY][adjustedX] = cell;
                viewportMap[adjustedY][adjustedX][0] = -y;
                viewportMap[adjustedY][adjustedX][1] = x;
                tiles.getChildren().add(cell);
                redraw(adjustedY, adjustedX);
            }
        }

        addMouseZoomFunctionality();
    }

    private void addMouseZoomFunctionality ()
    {
        final Consumer<ScrollEvent> onScroll = event ->
        {
            if (event.getDeltaY() > 0)
            {
                zoomLevel = zoomLevel * 1.25;
                scaleXProperty().setValue(zoomLevel);
                scaleYProperty().setValue(zoomLevel);
            }
            else if (zoomLevel > 1.00 && event.getDeltaY() < 0)
            {
                zoomLevel = zoomLevel * 0.80;
                scaleXProperty().setValue(zoomLevel);
                scaleYProperty().setValue(zoomLevel);
            }
        };

        addEventHandler(ScrollEvent.SCROLL, x -> Platform.runLater(() -> onScroll.accept(x)));
    }

    /**
     * Move the viewport up one row.
     */
    private void moveUp ()
    {
        System.out.println("Move Up");

        /**
         * Shift the viewport-map up by one row.
         */
        for (int y = 0; y < VIEWPORT_HEIGHT; y++)
        {
            for (int x = 0; x < VIEWPORT_WIDTH; x++)
            {
                viewportMap[y][x][0] = viewportMap[y][x][0] + 1;
                viewportMap[y][x][1] = viewportMap[y][x][1] + 0;
            }
        }

        /**
         * Shift the rows up one row.
         */
        for (int y = VIEWPORT_HEIGHT - 2; y >= 0; y--)
        {
            for (int x = VIEWPORT_WIDTH - 1; x >= 0; x--)
            {
                moveCell(y, x, y + 1, x);
            }
        }

        /**
         * Clear the bottom row.
         */
        IntStream.range(0, VIEWPORT_WIDTH).forEach(x -> clearCell(0, x));

        /**
         * Request that the bottom row be redrawn asynchronously.
         */
        IntStream.range(0, VIEWPORT_WIDTH).forEach(x -> redraw(0, x));
    }

    /**
     * Move the viewport down one row.
     */
    private void moveDown ()
    {
        System.out.println("Move Down");

        /**
         * Shift the viewport-map down by one row.
         */
        for (int y = 0; y < VIEWPORT_HEIGHT; y++)
        {
            for (int x = 0; x < VIEWPORT_WIDTH; x++)
            {
                viewportMap[y][x][0] = viewportMap[y][x][0] - 1;
                viewportMap[y][x][1] = viewportMap[y][x][1] + 0;
            }
        }

        /**
         * Shift the rows down one row.
         */
        for (int y = 1; y < VIEWPORT_HEIGHT; y++)
        {
            for (int x = 0; x < VIEWPORT_WIDTH; x++)
            {
                moveCell(y, x, y - 1, x);
            }
        }

        /**
         * Clear the top row.
         */
        IntStream.range(0, VIEWPORT_WIDTH).forEach(x -> clearCell(VIEWPORT_HEIGHT - 1, x));

        /**
         * Request that the top row be redrawn asynchronously.
         */
        IntStream.range(0, VIEWPORT_WIDTH).forEach(x -> redraw(VIEWPORT_HEIGHT - 1, x));
    }

    /**
     * Move the viewport left one column.
     */
    private void moveLeft ()
    {
        System.out.println("Move Left");

        /**
         * Shift the viewport-map left by one column.
         */
        for (int y = 0; y < VIEWPORT_HEIGHT; y++)
        {
            for (int x = 0; x < VIEWPORT_WIDTH; x++)
            {
                viewportMap[y][x][0] = viewportMap[y][x][0] + 0;
                viewportMap[y][x][1] = viewportMap[y][x][1] - 1;
            }
        }

        /**
         * Shift the columns left one row.
         */
        for (int y = 0; y < VIEWPORT_HEIGHT; y++)
        {
            for (int x = 1; x < VIEWPORT_WIDTH; x++)
            {
                moveCell(y, x, y, x - 1);
            }
        }

        /**
         * Clear the rightmost column.
         */
        IntStream.range(0, VIEWPORT_HEIGHT).forEach(y -> clearCell(y, VIEWPORT_WIDTH - 1));

        /**
         * Request that the rightmost column be redrawn asynchronously.
         */
        IntStream.range(0, VIEWPORT_HEIGHT).forEach(y -> redraw(y, VIEWPORT_WIDTH - 1));
    }

    /**
     * Move the viewport right one column.
     */
    private void moveRight ()
    {
        System.out.println("Move Right");

        /**
         * Shift the viewport-map right by one column.
         */
        for (int y = 0; y < VIEWPORT_HEIGHT; y++)
        {
            for (int x = 0; x < VIEWPORT_WIDTH; x++)
            {
                viewportMap[y][x][0] = viewportMap[y][x][0] + 0;
                viewportMap[y][x][1] = viewportMap[y][x][1] + 1;
            }
        }

        /**
         * Shift the columns right one row.
         */
        for (int y = 0; y < VIEWPORT_HEIGHT; y++)
        {
            for (int x = VIEWPORT_WIDTH - 2; x >= 0; x--)
            {
                moveCell(y, x, y, x + 1);
            }
        }

        /**
         * Clear the leftmost column.
         */
        IntStream.range(0, VIEWPORT_HEIGHT).forEach(y -> clearCell(y, 0));

        /**
         * Request that the leftmost column be redrawn asynchronously.
         */
        IntStream.range(0, VIEWPORT_HEIGHT).forEach(y -> redraw(y, 0));
    }

    private void redraw (final int y,
                         final int x)
    {
        final Canvas cell = viewport[y][x];
        final GraphicsContext g = cell.getGraphicsContext2D();
        g.setFill((x + y) % 2 == 0 ? Color.RED : Color.BLUE);
        g.fillRect(0, 0, TILE_SIZE, TILE_SIZE);
        g.setStroke(Color.BLACK);
        g.strokeText("(" + viewportMap[y][x][0] + "," + viewportMap[y][x][1] + ")", 25, 25);
        g.stroke();
    }

    private void clearCell (final int y,
                            final int x)
    {
        final Canvas cell = viewport[y][x];
        final GraphicsContext g = cell.getGraphicsContext2D();
        g.setFill(Color.WHITE);
        g.fill();
    }

    private void moveCell (final int ya,
                           final int xa,
                           final int yb,
                           final int xb)
    {
        final SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        final WritableImage image = viewport[ya][xa].snapshot(params, null);
        viewport[yb][xb].getGraphicsContext2D().drawImage(image, 0, 0);
    }

    public void setCell ()
    {

    }
}
