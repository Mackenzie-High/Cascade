package com.mackenziehigh.cascade;

/**
 * Contexts provide scripts with access to the enclosing actor
 * and with convenient helper methods for sending event-messages.
 */
public interface CascadeContext
{
    /**
     * Getter.
     *
     * @return the cascade that contains the stage().
     */
    public default Cascade cascade ()
    {
        return actor().cascade();
    }

    /**
     * Getter.
     *
     * @return the stage that contains the actor().
     */
    public default CascadeStage stage ()
    {
        return actor().stage();
    }

    /**
     * Getter.
     *
     * @return the actor that contains the script(), logger(), etc.
     */
    public CascadeActor actor ();

    /**
     * Getter.
     *
     * @return the script that the actor() executes.
     */
    public default CascadeScript script ()
    {
        return actor().script();
    }

    /**
     * Getter.
     *
     * @return the logger that the actor() currently uses.
     */
    public default CascadeLogger logger ()
    {
        return actor().logger();
    }

    /**
     * This method causes the actor() to broadcast an event-message.
     *
     * <p>
     * This method is a no-op, if no actors are subscribed
     * to receive event-messages from the given event-stream.
     * </p>
     *
     * @param event identifies the event being produced.
     * @param stack contains the content of the message.
     * @return this.
     */
    public default CascadeContext send (final CascadeToken event,
                                        final CascadeStack stack)
    {
        cascade().lookup(event).send(event, stack);
        return this;
    }

}
