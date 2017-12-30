
import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeSchema;
import com.mackenziehigh.cascade.CascadeToken;
import com.mackenziehigh.cascade.cores.Cores;
import com.mackenziehigh.cascade.cores.builders.Clock;
import com.mackenziehigh.cascade.cores.builders.TallyLimiter;
import com.mackenziehigh.cascade.internal.ConcreteSchema;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class Main
{
    public static void main (String[] args)
    {
        final CascadeSchema cs = new ConcreteSchema().named("Schema1");

        cs.addDynamicPool().named("default").withMinimumSize(0).withMaximumSize(256);
        cs.addFixedPool().named("pool2").withMinimumSize(512).withMaximumSize(768).withBufferCount(10);
        cs.addCompositePool().named("pool3").withMemberPool("default").withMemberPool("pool2");

        cs.addPump().named("pump1").withThreadCount(3);

        cs.usingPool("default").usingPump("pump1");

        final Clock clock1 = Cores.newClock();
        clock1.event.set(CascadeToken.create("tickTock"));
        clock1.periodNanos.set(TimeUnit.MILLISECONDS.toNanos(1000));
        clock1.formatAsMonotonicElapsedNanos.set(true);

        cs.addReactor()
                .named("clock")
                .withCore(clock1.build())
                .withArrayQueue(100);

        final TallyLimiter tally = new TallyLimiter();
        tally.input.set(CascadeToken.create("tickTock"));
        tally.output.set(CascadeToken.create("tickTock2"));
        tally.limit.set(3L);

        cs.addReactor()
                .named("tally")
                .withCore(tally.build())
                .withArrayQueue(100);

        cs.addReactor()
                .named("printer1")
                .withArrayQueue(100)
                .withCore(Cores.from(x -> System.out.println("X = " + x.message().asString() + ", Thread = " + Thread.currentThread().getId())))
                .subscribeTo("tickTock2");

        final Cascade cas = cs.build();

        cas.start();
    }

}
