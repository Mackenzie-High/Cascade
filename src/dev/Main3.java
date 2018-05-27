package dev;

import dev.util.Adder;
import dev.util.PeriodicPowerplant;
import dev.util.Printer;

/**
 *
 */
public final class Main3
{
    public static void main (String[] args)
    {
        final PeriodicPowerplant plant = new PeriodicPowerplant();

        final Adder adder = new Adder();
        final Printer printer = new Printer();
        plant.add(adder.reactor);
        plant.add(printer.reactor);

        printer.input().connect(adder.output());

        adder.left().send(100L);
        adder.right().send(200L);

        plant.start();
    }

}