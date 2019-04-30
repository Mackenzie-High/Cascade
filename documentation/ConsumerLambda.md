Example - Lambda based Consumer Script

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
                .withConsumerScript((String msg) -> System.out.println("Hello " + msg))
                .create();

        // Send a message to the actor.
        actor.input().send("Mercury");
        actor.input().send("Venus");
        actor.input().send("Earth");
        actor.input().send("Mars");
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
