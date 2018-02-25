package com.mackenziehigh.cascade.redo2;

import com.mackenziehigh.cascade.CascadeLogger;
import com.mackenziehigh.cascade.CascadeToken;

/**
 *
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
     * @return the actor that contains the script(), logger(), allocator(), etc.
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
     * @return the allocator that the actor() currently uses.
     */
    public default CascadeAllocator allocator ()
    {
        return actor().allocator();
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
     * @param event identifies the event being produced.
     * @param stack contains the content of the message.
     * @return this.
     */
    public CascadeContext send (CascadeToken event,
                                CascadeOperand stack);

}
