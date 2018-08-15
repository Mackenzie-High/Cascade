
import com.mackenziehigh.cascade.Powerplants;
import com.mackenziehigh.cascade.Reactor;
import com.mackenziehigh.cascade.Reactor.Input;
import com.mackenziehigh.cascade.Reactors;
import java.util.concurrent.Executors;

/**
 *
 */
public final class Main01
{
    public static void main (String[] args)
            throws InterruptedException
    {
        final Reactor rx = Reactors.newReactor();
        
        final Input<String> ix = rx.newInput(String.class);
        rx.newReaction().require(ix).onMatch(() ->
        {
            System.out.println("XX = " + ix.pollOrNull());
        });

        rx.poweredBy(Powerplants.newExecutorPowerplant(Executors.newSingleThreadExecutor()));

        ix.send("Heloo");

        Thread.sleep(1000);
    }
}
