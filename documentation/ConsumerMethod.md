# Consumer Method

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
        final Actor<String, String> root = stage
                .newActor()
                .withConsumerScript(this::script)
                .create();

        // Send a message to the actor.
        root.input().send("Mercury");
        root.input().send("Venus");
        root.input().send("Earth");
        root.input().send("Mars");
    }

    private void script (final String input)
            throws Throwable
    {
        System.out.println("Hello " + input);
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
