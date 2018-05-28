package dev;

import com.mackenziehigh.cascade.powerplants.PeriodicPowerplant;
import dev.util.Printer;
import dev.util.Reduction;

/**
 *
 */
public final class Main7
{
    public static void main (String[] args)
    {
        final PeriodicPowerplant plant = new PeriodicPowerplant();

        final Reduction<Long> sum1 = Reduction.summationLong();
        final Reduction<Long> sum2 = Reduction.summationLong();
        final Reduction<Long> sum3 = Reduction.summationLong();
        final Printer printer = new Printer();

        sum1.output().connect(sum2.input());
        sum2.output().connect(sum3.input());
        sum3.output().connect(printer.input());

        plant.add(sum1.reactor);
        plant.add(sum2.reactor);
        plant.add(sum3.reactor);
        plant.add(printer.reactor);
        plant.start();

        sum1.reactor.start();
        sum2.reactor.start();
        sum3.reactor.start();
        printer.reactor.start();

        sum1.input().send(100L);
        sum1.input().send(100L);
        sum1.input().send(100L);
    }

}
