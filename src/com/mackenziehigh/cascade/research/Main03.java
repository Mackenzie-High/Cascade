package com.mackenziehigh.cascade.research;

import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.CascadeSchema;

/**
 *
 */
public final class Main03
{
    public static void main (final String[] args)
    {
        final CascadeSchema cs = new CascadeSchema();
        cs.enter("mackenziehigh");
        cs.usingPump("P1");
        cs.usingPool("default");

//        cs.addNode("n1", Nodes.PRINTER.get().build());
        final Cascade cas = cs.build();
        cas.start();

        final CascadeAllocator.OperandStack stack = cas.allocator().pools().get("default").allocator().newOperandStack();
        stack.push(100);

        cas.nodes().get("a").protoContext().outputs().get(0).async(stack);
    }
}
