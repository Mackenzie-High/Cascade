package com.mackenziehigh.cascade.internal.pumps3;

import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import com.mackenziehigh.cascade.internal.pumps3.Connector.Connection;
import java.util.Map;
import java.util.function.Consumer;

/**
 *
 */
public interface Engine
{
    public Map<Connection, Consumer<OperandStack>> connections ();

    public boolean isRunning ();

    public void start ();

    public void stop ();
}
