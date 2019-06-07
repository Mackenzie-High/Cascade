# Example - Custom Stage

**Stage Class:**

```java
package examples;

import com.mackenziehigh.cascade.Cascade;

/**
 * This class demonstrates how easy one can create a custom Stage.
 * However, in reality, creating a thread per message would usually
 * be quite inefficient due to excessive thread-spawning, etc.
 */
public final class HeavyStage
        extends Cascade.AbstractStage
{
    @Override
    protected void onRunnable (final DefaultActor<?, ?> state)
    {
        // The onSubmit() method is only called when
        // the ActorTask is safe to execute; therefore,
        // we can just spawn a thread immediately.
        // No need to worry about synchronization at all.
        final Thread thread = new Thread(state);
        thread.start();
    }

    @Override
    protected void onClose ()
    {
        // In this implementation, this method is unneeded.
        // In other implementations, such as those that have
        // a shared ExecutorService, this is where you would
        // place the logic needed to shut the service down.
    }
}
```

**Main Class:**

```java
package examples;

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
        // Create a custom stage.
        final Stage stage = new HeavyStage();

        // Create an actor.
        final Actor<String, String> actor = stage
                .newActor()
                .withConsumerScript((String msg) -> System.out.println("Welcome to " + msg + "!"))
                .create();

        // Send messages to the actor.
        actor.input().send("Mercury");
        actor.input().send("Venus");
        actor.input().send("Earth");
        actor.input().send("Mars");
    }
}
```

**Example Output:**
```
Welcome to Mercury!
Welcome to Venus!
Welcome to Earth!
Welcome to Mars!
```
