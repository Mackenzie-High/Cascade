## Hello World Example

**Description**

In this example, there are four actor objects arranged into a cirular data processing pipeline. The actors manipulate String messages. 

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
        final Stage stage = Cascade.newStage(2);

        // Create the actors.
        final Actor<String, String> actor1 = stage.newActor().withFunctionScript(this::actor1).create();
        final Actor<String, String> actor2 = stage.newActor().withFunctionScript(this::actor2).create();
        final Actor<String, String> actor3 = stage.newActor().withFunctionScript(this::actor3).create();
        final Actor<String, String> actor4 = stage.newActor().withFunctionScript(this::actor4).create();

        // Connect Network: actor1 -> actor2 -> actor3 -> actor4 -> actor1
        actor1.output().connect(actor2.input());
        actor2.output().connect(actor3.input());
        actor3.output().connect(actor4.input());
        actor4.output().connect(actor1.input());

        // Send a messages through the pipeline and cause a cascade.
        actor1.input().send("Command 1");
    }

    private String actor1 (final String message)
    {
        System.out.printf("(Actor 1) received (%s).\n", message);
        return message;
    }

    private String actor2 (final String message)
    {
        System.out.printf("(Actor 2) received (%s).\n", message);
        return message;
    }

    private String actor3 (final String message)
    {
        System.out.printf("(Actor 3) received (%s).\n", message);
        return message;
    }

    private String actor4 (final String message)
    {
        System.out.printf("(Actor 4) received (%s).\n\n", message);
        String next = null;
        next = "Command 1".equals(message) ? "Command 2" : next;
        next = "Command 2".equals(message) ? "Command 3" : next;
        next = "Command 3".equals(message) ? "Complete" : next;
        return next;
    }
}
```

**Standard Output**
```
(Actor 1) received (Command 1).
(Actor 2) received (Command 1).
(Actor 3) received (Command 1).
(Actor 4) received (Command 1).

(Actor 1) received (Command 2).
(Actor 2) received (Command 2).
(Actor 3) received (Command 2).
(Actor 4) received (Command 2).

(Actor 1) received (Command 3).
(Actor 2) received (Command 3).
(Actor 3) received (Command 3).
(Actor 4) received (Command 3).

(Actor 1) received (Complete).
(Actor 2) received (Complete).
(Actor 3) received (Complete).
(Actor 4) received (Complete).
```
