package dev;

import dev.util.Adder;
import dev.util.PeriodicPowerplant;
import dev.util.Printer;

/**
 *
 */
public final class Main4
{
    public static void main (String[] args)
    {
        final PeriodicPowerplant plant = new PeriodicPowerplant();

        final Adder adder = new Adder();
        final Printer printer = new Printer();
        plant.add(adder.reactor);
        plant.add(printer.reactor);

        adder.left().connect(adder.output());
        adder.right().connect(adder.output());

        printer.input().connect(adder.output());

        adder.left().send(1L);
        adder.right().send(2L);

        plant.start();
    }

}
