package dev;

import dev.util.Adder;
import dev.util.PeriodicPowerplant;
import dev.util.Printer;
import dev.util.Splitter;

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
        final Splitter<Long> splitter1 = new Splitter<>(Long.class);
        final Splitter<Long> splitter2 = new Splitter<>(Long.class);

        plant.add(adder.reactor);
        plant.add(printer.reactor);
        plant.add(splitter1.reactor);
        plant.add(splitter2.reactor);

        adder.left().connect(splitter1.outputX());
        adder.right().connect(splitter1.outputY());
        adder.output().connect(splitter2.input());
        splitter2.outputX().connect(splitter1.input());
        splitter2.outputY().connect(printer.input());

        adder.left().send(1L);
        adder.right().send(2L);

        plant.start();
    }

}
