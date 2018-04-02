package com.mackenziehigh.cascade.util.actorsfx;

import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeStage;
import com.mackenziehigh.cascade.CascadeToken;
import com.mackenziehigh.cascade.scripts.LambdaScript;
import com.mackenziehigh.cascade.util.Final;
import com.mackenziehigh.cascade.util.actors.faces.OneToOne;
import java.util.Objects;
import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.control.TextField;

/**
 *
 */
public final class TextFieldActor
        implements CascadeActor.Builder,
                   OneToOne<TextFieldActor>
{
    private final CascadeStage stage;

    private final TextField field;

    private final Final<CascadeToken> inputToken = Final.empty();

    private final Final<CascadeToken> outputToken = Final.empty();

    public TextFieldActor (final CascadeStage stage,
                           final TextField field)
    {
        this.stage = Objects.requireNonNull(stage, "stage");
        this.field = Objects.requireNonNull(field, "field");
    }

    @Override
    public TextFieldActor setDataInput (CascadeToken input)
    {
        inputToken.set(input);
        return this;
    }

    @Override
    public TextFieldActor setDataOutput (CascadeToken output)
    {
        outputToken.set(output);
        return this;
    }

    @Override
    public Optional<CascadeToken> getDataInput ()
    {
        return inputToken.get();
    }

    @Override
    public Optional<CascadeToken> getDataOutput ()
    {
        return outputToken.get();
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

        script.bindOnMessage(inputToken.get().get(), (ctx, evt, msg) -> onInput(msg));

        final CascadeActor actor = stage.newActor(script.build());
        return actor;
    }

    private void onInput (final CascadeStack stack)
    {
        final String text = stack.peekAsObject().toString();
        Platform.runLater(() -> field.setText(text));
    }
}
