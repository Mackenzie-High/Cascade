package dev;

import dev.util.PeriodicPowerplant;
import dev.util.Printer;
import dev.util.Splitter;
import dev.util.UnaryOperation;

/**
 *
 */
public final class Main5
{
    public static void main (String[] args)
    {
        final PeriodicPowerplant plant = new PeriodicPowerplant();

        final Printer printer1 = new Printer();
        final Printer printer2 = new Printer();
        final Printer printer3 = new Printer();
        final Printer printer4 = new Printer();
        final Splitter<Long> splitter1 = new Splitter<>(Long.class);
        final Splitter<Long> splitter2 = new Splitter<>(Long.class);
        final Splitter<Long> splitter3 = new Splitter<>(Long.class);
        final UnaryOperation<Long> op1 = UnaryOperation.create(Long.class, x -> x + 1);
        final UnaryOperation<Long> op2 = UnaryOperation.create(Long.class, x -> x + 2);
        final UnaryOperation<Long> op3 = UnaryOperation.create(Long.class, x -> x + 3);
        final UnaryOperation<Long> op4 = UnaryOperation.create(Long.class, x -> x + 4);

        splitter1.outputX().connect(splitter2.input());
        splitter1.outputY().connect(splitter3.input());

        splitter2.outputX().connect(op1.input());
        splitter2.outputY().connect(op2.input());

        splitter3.outputX().connect(op3.input());
        splitter3.outputY().connect(op4.input());

        op1.output().connect(printer1.input());
        op2.output().connect(printer2.input());
        op3.output().connect(printer3.input());
        op4.output().connect(printer4.input());

        plant.add(printer1.reactor);
        plant.add(printer2.reactor);
        plant.add(printer3.reactor);
        plant.add(printer4.reactor);
        plant.add(splitter1.reactor);
        plant.add(splitter2.reactor);
        plant.add(splitter3.reactor);
        plant.add(op1.reactor);
        plant.add(op2.reactor);
        plant.add(op3.reactor);
        plant.add(op4.reactor);
        plant.start();

        splitter1.input().send(100L);
    }

}
