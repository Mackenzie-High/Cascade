package com.mackenziehigh.cascade2;

import java.util.Map;

/**
 * Provides the threading.
 */
public interface Engine
{
    public Map<Token, MessageHandler> handlers ();

    public void start ();

    public void stop ();

    public void signal (Token source);
}
