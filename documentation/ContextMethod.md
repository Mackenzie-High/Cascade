## Context Lambda

**Code**

```java
package com.mackenziehigh.dev;

import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.Cascade.Stage;
import com.mackenziehigh.cascade.Cascade.Stage.Actor;

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
        final Actor<Integer, String> actor = stage
                .newActor()
                .withContextScript(this::actor)
                .create();

        final Actor<String, String> printer = stage
                .newActor()
                .withConsumerScript((String x) -> System.out.println(x))
                .create();

        // Connect the network: actor -> printer
        actor.output().connect(printer.input());

        // Send messages through the network.
        actor.input().send(2);
    }

    private void actor (final Actor.Context<Integer, String> context,
                        final Integer message)
    {
        context.sendFrom(String.format("square(%d) = %d", message, message * message));
        context.sendFrom(String.format("cube(%d) = %d", message, message * message * message));
    }
}
```

**Example Output***

```
square(2) = 4
cube(2) = 8
```
