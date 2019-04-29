## Consumer Method

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
                .withConsumerScript(this::actor)
                .create();

        actor.input().send("Hello");
        actor.input().send("Goodbye");
    }

    private void actor (final String message)
    {
        System.out.println("Say " + message);
    }
}
```

**Example Output**

```
Say Hello
Say Goodbye
```
