package com.mackenziehigh.loader.exceptions;

/**
 * Configuration Problems.
 */
public class InvalidConfigurationException
        extends RuntimeException
{
    public InvalidConfigurationException ()
    {
        super();
    }

    public InvalidConfigurationException (String string)
    {
        super(string);
    }

    public InvalidConfigurationException (String string,
                                          Throwable thrwbl)
    {
        super(string, thrwbl);
    }

    public InvalidConfigurationException (Throwable thrwbl)
    {
        super(thrwbl);
    }
}
