## Function Class

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
        final Actor<String, String> actor = stage
                .newActor()
                .withFunctionScript(new FibonacciScript())
                .create();

        final Actor<String, String> printer = stage
                .newActor()
                .withConsumerScript((String x) -> System.out.println(x))
                .create();

        // Connect the network: actor -> printer.
        actor.output().connect(printer.input());

        // Send messages through the network.
        actor.input().send("A");
        actor.input().send("B");
        actor.input().send("C");
        actor.input().send("E");
        actor.input().send("F");
        actor.input().send("G");
        actor.input().send("H");
    }

    private static final class FibonacciScript
            implements Actor.FunctionScript<String, String>
    {
        int position = 0;

        int minus1 = 1;

        int minus2 = 1;

        @Override
        public String execute (final String input)
        {
            ++position;

            if (position == 1)
            {
                return input + " = 1";
            }
            else if (position == 2)
            {
                return input + " = 1";
            }
            else
            {
                final int sum = minus1 + minus2;
                minus2 = minus1;
                minus1 = sum;
                return input + " = " + sum;
            }
        }
    }
}
```

**Example Output***

```
A = 1
B = 1
C = 2
E = 3
F = 5
G = 8
H = 13
```
