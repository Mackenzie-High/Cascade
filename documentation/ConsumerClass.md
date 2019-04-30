# Example - Class-based Context Script

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

        // Create the actor.
        final Actor<String, String> actor = stage
                .newActor()
                .withConsumerScript(new CustomScript())
                .create();

        // Send a message to the actor.
        actor.input().send("Mercury");
        actor.input().send("Venus");
        actor.input().send("Earth");
        actor.input().send("Mars");
    }

    /**
     * An instance of this script defines how the actor will behave.
     */
    private static final class CustomScript
            implements Actor.ConsumerScript<String>
    {
        @Override
        public void execute (final String input)
                throws Throwable
        {
            System.out.println("Hello " + input);
        }
    }
}
```

**Output:**

```
Hello Mercury
Hello Venus
Hello Earth
Hello Mars
```
