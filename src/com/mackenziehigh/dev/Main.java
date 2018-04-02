package com.mackenziehigh.dev;

import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.CascadeStage;
import com.mackenziehigh.cascade.CascadeToken;
import com.mackenziehigh.cascade.Cascades;
import com.mackenziehigh.cascade.util.actors.CommonActors;
import com.mackenziehigh.cascade.util.actorsfx.TextFieldActor;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 */
public final class Main
        extends Application
{

    private final Button button1 = new Button("B1");

    private final TextField field1 = new TextField();

    private final TextField field2 = new TextField();

    private final VBox main = new VBox(button1, field1, field2);

    @Override
    public void start (final Stage primaryStage)
    {

        final BorderPane root = new BorderPane();
//        root.setTop(menuBar);
        root.setCenter(main);

        createCascade();

        final Scene scene = new Scene(root, 500, 500);
        primaryStage.setOnCloseRequest(x -> System.exit(0));
        primaryStage.setResizable(false);
        primaryStage.setTitle("Grid Calculator");
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

    private void createCascade ()
    {
        final Cascade cascade = Cascades.newCascade();
        final CascadeStage stage = cascade.newStage();
        stage.incrementThreadCount();

        final CascadeToken ticks = CascadeToken.token("ticks");

        stage.newActor(CommonActors.CLOCK)
                .setPeriod(Duration.ofMillis(1000))
                .setDataOutput(ticks)
                .setDelay(Duration.ofSeconds(1))
                .useFixedDelay()
                .sendElapsed(TimeUnit.SECONDS)
                .build();

        final CascadeActor actor1 = new TextFieldActor(stage, field1).setDataInput(ticks).build();
        final CascadeActor actor2 = new TextFieldActor(stage, field2).setDataInput(ticks).build();
        actor1.subscribe(ticks);
        actor2.subscribe(ticks);
        stage.newActor(CommonActors.STDOUT).setInput(ticks).build();

    }
}
