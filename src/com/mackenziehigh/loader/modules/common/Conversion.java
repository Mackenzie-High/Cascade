package com.mackenziehigh.loader.modules.common;

import com.mackenziehigh.loader.ConfigObject;
import com.mackenziehigh.loader.Controller;
import com.mackenziehigh.loader.Module;

/**
 * An instance of this class performs a predefined object
 * conversion on a message (M) received from one topic (X)
 * and then forwards the message (M) to another topic (Y).
 */
public final class Conversion
        implements Module
{
    @Override
    public boolean start ()
    {
        return true;
    }

    @Override
    public boolean setup (Controller controller,
                          String name,
                          ConfigObject configuration)
    {
        return true;
    }

}
