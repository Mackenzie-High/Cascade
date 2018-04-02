package com.mackenziehigh.cascade.util.actorsfx;

import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.CascadeStage;
import com.mackenziehigh.cascade.CascadeToken;
import com.mackenziehigh.cascade.scripts.LambdaScript;
import com.mackenziehigh.cascade.util.actors.faces.OneToOne;
import java.util.Objects;
import java.util.Optional;
import javafx.scene.control.Button;

/**
 *
 */
public final class ButtonActor
        implements CascadeActor.Builder,
                   OneToOne<ButtonActor>
{
    private final CascadeStage stage;

    private final Button button;

    public ButtonActor (final CascadeStage stage,
                        final Button button)
    {
        this.stage = Objects.requireNonNull(stage, "stage");
        this.button = Objects.requireNonNull(button, "button");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ButtonActor setDataInput (CascadeToken input)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ButtonActor setDataOutput (CascadeToken input)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<CascadeToken> getDataInput ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<CascadeToken> getDataOutput ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeStage stage ()
    {
        return stage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeActor build ()
    {
        final LambdaScript.Builder script = LambdaScript.newBuilder();

        final CascadeActor actor = stage.newActor(script.build());

        button.onActionProperty().addListener(x -> onClick());

        return actor;
    }

    private void onClick ()
    {

    }
}
