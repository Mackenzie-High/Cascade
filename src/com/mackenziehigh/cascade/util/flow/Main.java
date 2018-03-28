package com.mackenziehigh.cascade.util.flow;

/**
 *
 * @author mackenzie
 */
public class Main
{
    public static void main (String[] args)
    {
        final FlowGraph g = new FlowGraph();

        final Straight sin = g.streamIn(null);
        final Straight sot = g.streamOut(null);
        final Straight a = g.newStraight();
        final Straight b = g.newStraight();
        final Fanout c = g.newFanout();
        final Straight e = g.newStraight();

        a.output(b);
        c.output(a);
        c.output(b);

    }
}
