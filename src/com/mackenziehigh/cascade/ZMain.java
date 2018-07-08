package com.mackenziehigh.cascade;

/**
 *
 */
public class ZMain
{
    private final Reactor reactor = Cascade.newReactor();

    private final Input<String> input = reactor.newArrayInput(String.class, 100);

    private final Reaction r1 = reactor
            .newReaction()
            .require(input)
            .onMatch(() -> System.out.println("X = " + input.pollOrNull()));

    public static void main (String[] args)
    {
        final ZMain main = new ZMain();

        main.input.send("123");

        while (main.reactor.crank())
        {
            // Pass
        }
    }
}
