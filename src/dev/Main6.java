package dev;

import com.mackenziehigh.cascade.powerplants.PeriodicPowerplant;
import dev.util.Bus;
import dev.util.Printer;
import dev.util.UnaryOperation;

/**
 *
 */
public final class Main6
{
    public static void main (String[] args)
    {
        final PeriodicPowerplant plant = new PeriodicPowerplant();

        final Bus<Long> bus1 = new Bus<>(Long.class, 10, 10);
        final Bus<Long> bus2 = new Bus<>(Long.class, 10, 10);
        final Printer printer = new Printer();
        final UnaryOperation<Long> op1 = UnaryOperation.create(Long.class, x -> x + 10);
        final UnaryOperation<Long> op2 = UnaryOperation.create(Long.class, x -> x + 20);
        final UnaryOperation<Long> op3 = UnaryOperation.create(Long.class, x -> x + 30);
        final UnaryOperation<Long> op4 = UnaryOperation.create(Long.class, x -> x + 40);

        op1.input().connect(bus1.output(1));
        op2.input().connect(bus1.output(2));
        op3.input().connect(bus1.output(3));
        op4.input().connect(bus1.output(4));

        op1.output().connect(bus2.input(1));
        op2.output().connect(bus2.input(2));
        op3.output().connect(bus2.input(3));
        op4.output().connect(bus2.input(4));

        printer.input().connect(bus2.output(1));

        plant.add(bus1.reactor);
        plant.add(bus2.reactor);
        plant.add(printer.reactor);
        plant.add(op1.reactor);
        plant.add(op2.reactor);
        plant.add(op3.reactor);
        plant.add(op4.reactor);
        plant.start();

        bus1.input(0).send(100L);
    }

}
