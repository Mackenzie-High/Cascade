# Example - Class based Function Script

**Code:**

```java
package examples;

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
        final Stage stage = Cascade.newStage();

        // Create the actor that is being demonstrated.
        final Actor<Integer, Double> actor = stage
                .newActor()
                .withFunctionScript(new CustomScript())
                .create();

        // Create an actor that merely prints the messages
        // that the previous actor produced.
        final Actor<Double, Double> printer = stage
                .newActor()
                .withConsumerScript((Double msg) -> System.out.println("sqrt = " + msg))
                .create();

        // Connect the actors to form a pipeline.
        actor.output().connect(printer.input());

        // Send a message through the pipeline.
        actor.input().send(17);
    }

    /**
     * An instance of this script defines how the actor will behave.
     */
    private static final class CustomScript
            implements Actor.FunctionScript<Integer, Double>
    {
        @Override
        public Double onInput (final Integer input)
                throws Throwable
        {
            return Math.sqrt(input);
        }
    }
}
```

**Output:**

```
sqrt = 4.123105625617661
```
