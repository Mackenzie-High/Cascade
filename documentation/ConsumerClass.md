## Example Consumer Class 

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
                .withConsumerScript(new CustomScript())
                .create();

        // Send messages through the network.
        actor.input().send(2);
        actor.input().send(3);
        actor.input().send(5);
        actor.input().send(7);
    }

    private static final class CustomScript
            implements Actor.ConsumerScript<Integer>
    {
        long sum = 0;

        @Override
        public void execute (final Integer input)
        {
            sum += input;
            System.out.println("sum = " + sum);
        }
    }
}
```

**Example Output***

```
sum = 2
sum = 5
sum = 10
sum = 17
```
