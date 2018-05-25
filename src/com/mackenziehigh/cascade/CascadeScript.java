package com.mackenziehigh.cascade;

/**
 * Scripts define how actors behave in-response to events.
 */
public interface CascadeScript
{

    /**
     * Lambda function whose signature is the same as the onSetup() event-handler.
     */
    @FunctionalInterface
    public interface OnSetupFunction
    {
        public void accept (CascadeContext ctx)
                throws Throwable;
    }

    /**
     * Lambda function whose signature is the same as the onMessage() event-handler.
     */
    @FunctionalInterface
    public interface OnMessageFunction
    {
        public void accept (CascadeContext ctx,
                            CascadeToken event,
                            CascadeStack stack)
                throws Throwable;
    }

    /**
     * Lambda function whose signature is the same as the onUnhandledException() event-handler.
     */
    @FunctionalInterface
    public interface OnExceptionFunction
    {
        public void accept (CascadeContext ctx,
                            Throwable cause)
                throws Throwable;
    }

    /**
     * Lambda function whose signature is the same as the onClose() event-handler.
     */
    @FunctionalInterface
    public interface OnCloseFunction
    {
        public void accept (CascadeContext ctx)
                throws Throwable;
    }

    /**
     * Use this method to define the event-handler for incoming messages.
     *
     * @param handler will be executed whenever a message is received.
     * @return this.
     */
    public CascadeScript onMessage (OnMessageFunction handler);

    /**
     * Retrieve the event-message event-handler.
     *
     * @return the event-message event-handler.
     */
    public OnMessageFunction onMessage ();

    /**
     * Retrieve the event-handler that will be executed to setup the actor.
     *
     * @return the setup event-handler.
     */
    public OnSetupFunction onSetup ();

    /**
     * Use this method to define the event-handler to use to setup the actor.
     *
     * @param handler will be executed when the actor is started.
     * @return this.
     */
    public CascadeScript onSetup (OnSetupFunction handler);

    /**
     * Retrieve the event-handler that will be executed to close the actor.
     *
     * @return the close event-handler.
     */
    public OnCloseFunction onClose ();

    /**
     * Use this method to define the event-handler to use to close the actor.
     *
     * @param handler will be executed when the actor is closed.
     * @return this.
     */
    public CascadeScript onClose (OnCloseFunction handler);

    /**
     * Retrieve the event-handler that will be executed,
     * if any other event-handler throws an exception.
     *
     * @return the exception event-handler.
     */
    public OnExceptionFunction onException ();

    /**
     * Use this method to define the event-handler to use to setup the actor.
     *
     * @param handler will be executed when the actor is started.
     * @return this.
     */
    public CascadeScript onException (OnExceptionFunction handler);
}
