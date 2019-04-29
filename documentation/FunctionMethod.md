## Function Method 

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
        final Actor<Integer, Integer> actor = stage
                .newActor()
                .withFunctionScript(this::actor)
                .create();

        final Actor<Integer, Integer> printer = stage
                .newActor()
                .withConsumerScript((Integer x) -> System.out.println(x))
                .create();

        // Connect the network: actor -> printer
        actor.output().connect(printer.input());

        // Send messages through the network.
        actor.input().send(2);
        actor.input().send(3);
        actor.input().send(5);
    }

    private Integer actor (final Integer message)
    {
        return 2 * message;
    }
}
```

**Example Output***

```
4
6
10
```
