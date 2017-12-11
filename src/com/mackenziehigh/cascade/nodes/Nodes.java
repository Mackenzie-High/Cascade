package com.mackenziehigh.cascade.nodes;

import com.mackenziehigh.cascade.nodes.builders.*;
import java.util.function.Supplier;

/**
 *
 */
public interface Nodes
{
    public Supplier<ForwarderBuilder> FORWARDER = () -> new ForwarderBuilder();

    public Supplier<PrinterBuilder> PRINTER = () -> new PrinterBuilder();
}
