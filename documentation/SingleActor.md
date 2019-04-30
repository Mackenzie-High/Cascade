# Example - Single Actor

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
        final Stage stage = Cascade.newStage(1);

        // Create the actors.
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
