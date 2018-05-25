package dev;

import dev.actors.Adder;
import dev.actors.Printer;

/**
 *
 */
public final class Main3
{
    public static void main (String[] args)
    {
        final Adder adder = new Adder();
        final Printer printer = new Printer();
        printer.input().connect(adder.result());

        adder.left().send(100);
        adder.right().send(200);

        adder.reactor.crank();
        printer.reactor.crank();
    }

}
