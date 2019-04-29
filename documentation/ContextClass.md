## Context Class

**Code**

```java
package com.mackenziehigh.dev;

import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.Cascade.Stage;
import com.mackenziehigh.cascade.Cascade.Stage.Actor;
import com.mackenziehigh.cascade.Cascade.Stage.Actor.Context;
import java.math.BigInteger;

public final class Example
{
    public static void main (String[] args)
    {
        final Example main = new Example();
        main.demo();
    }

    private void demo ()
    {
        // Create a single-threaded stage.
        final Stage stage = Cascade.newStage(1);

        // Create the actors.
        final Actor<Integer, Integer> actor = stage
                .newActor()
                .withContextScript(new PrimeFinder())
                .create();

        final Actor<Integer, Integer> printer = stage
                .newActor()
                .withConsumerScript((Integer x) -> System.out.println(x))
                .create();

        // Connect the network: actor -> printer.
        actor.output().connect(printer.input());

        // Send messages through the network.
        actor.input().send(10);
        actor.input().send(20);
    }

    private static final class PrimeFinder
            implements Actor.ContextScript<Integer, Integer>
    {

        @Override
        public void execute (final Context<Integer, Integer> context,
                             final Integer input)
        {
            for (int i = 0; i < input; i++)
            {
                if (BigInteger.valueOf(i).isProbablePrime(100))
                {
                    context.sendFrom(i);
                }
            }
        }
    }
}
```

**Example Output***

```
2
3
5
7
2
3
5
7
11
13
17
19
```
