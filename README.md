# Cascade

Cascade is an embeddable actor framework contained within a single Java source-file. 

**Latest JavaDoc**: https://tinyurl.com/y7pxnwv5

**Source Code**: https://tinyurl.com/y9h36mnb

**Detailed Code Coverage**: https://tinyurl.com/y8s5gbmy

```java
package com.mackenziehigh.dev;

import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.Cascade.Stage;
import com.mackenziehigh.cascade.Cascade.Stage.Actor;

public final class Example01
{
    public static void main (String[] args)
    {
        final Example01 main = new Example01();
        main.demo();
    }

    private void demo ()
    {
        // Create a single-threaded stage.
        final Stage stage = Cascade.newStage(1);

        // Create the actors.
        final Actor<String, String> actor1 = stage.newActor().withScript(this::actor1).create();
        final Actor<String, String> actor2 = stage.newActor().withScript(this::actor2).create();
        final Actor<String, String> actor3 = stage.newActor().withScript(this::actor3).create();
        final Actor<String, String> actor4 = stage.newActor().withScript(this::actor4).create();

        // Connect Pipeline: actor1 -> actor2 -> actor3 -> actor4
        actor1.output().connect(actor2.input());
        actor2.output().connect(actor3.input());
        actor3.output().connect(actor4.input());

        // Send Messages Through Pipeline.
        actor1.input().send("A");
        actor1.input().send("B");
        actor1.input().send("C");
    }

    private String actor1 (final String message)
    {
        return String.format("(X = %s)", message);
    }

    private String actor2 (final String message)
    {
        return String.format("(Y = %s)", message);
    }

    private String actor3 (final String message)
    {
        return String.format("(Z = %s)", message);
    }

    private void actor4 (final String message)
    {
        System.out.println(message);
    }
}
```
