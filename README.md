# Cascade

Cascade is an embeddable actor framework contained within a single Java source-file. 

**Latest JavaDoc**: https://tinyurl.com/y7pxnwv5

**Source Code**: https://tinyurl.com/y9h36mnb

**Detailed Code Coverage**: https://tinyurl.com/y8s5gbmy

## Actors

An actor is an object that has: (1) an input connector, (2) an input queue, (3) an output connector, (4) a function that transforms input messages into output messages.

Actors can be connected to one another by connecting their input/output connectors to one another. Each actor can be connected to an arbitrary number of inputs and/or outputs. 

## Example - Simple Pipeline

**Description**

In this example, there are four actor objects arranged into a data processing pipeline. The actors manipulate String messages. 

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

## Six Ways to Define an Actor

### Consumer Lambda

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

### Function Lambda 

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
        final Actor<Integer, Integer> actor = stage
                .newActor()
                .withFunctionScript(this::actor)
                .create();

        final Actor<Integer, Integer> printer = stage
                .newActor()
                .withConsumerScript((Integer x) -> System.out.println(x))
                .create();

        // Connect the network: actor -> printer
        actor.output().connect(printer.input());

        // Send messages through the network.
        actor.input().send(2);
        actor.input().send(3);
        actor.input().send(5);
    }

    private Integer actor (final Integer message)
    {
        return 2 * message;
    }
}
```

**Example Output***

```
4
6
10
```

### General Lambda

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
        final Actor<Integer, String> actor = stage
                .newActor()
                .withContextScript(this::actor)
                .create();

        final Actor<String, String> printer = stage
                .newActor()
                .withConsumerScript((String x) -> System.out.println(x))
                .create();

        // Connect the network: actor -> printer
        actor.output().connect(printer.input());

        // Send messages through the network.
        actor.input().send(2);
    }

    private void actor (final Actor.Context<Integer, String> context,
                        final Integer message)
    {
        context.sendFrom(String.format("square(%d) = %d", message, message * message));
        context.sendFrom(String.format("cube(%d) = %d", message, message * message * message));
    }
}
```

**Example Output***

```
square(2) = 4
cube(2) = 8
```

### Consumer Class

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
        final Actor<Integer, Integer> actor = stage
                .newActor()
                .withConsumerScript(new CustomScript())
                .create();

        // Send messages through the network.
        actor.input().send(2);
        actor.input().send(3);
        actor.input().send(5);
        actor.input().send(7);
    }

    private static final class CustomScript
            implements Actor.ConsumerScript<Integer>
    {
        long sum = 0;

        @Override
        public void execute (final Integer input)
        {
            sum += input;
            System.out.println("sum = " + sum);
        }
    }
}
```

**Example Output***

```
sum = 2
sum = 5
sum = 10
sum = 17
```

### Function Class

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

### General Class

**Code**

```java
package com.mackenziehigh.dev;

import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.Cascade.Stage;
import com.mackenziehigh.cascade.Cascade.Stage.Actor;
import com.mackenziehigh.cascade.Cascade.Stage.Actor.Context;
import java.math.BigInteger;

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
        final Actor<Integer, Integer> actor = stage
                .newActor()
                .withContextScript(new PrimeFinder())
                .create();

        final Actor<Integer, Integer> printer = stage
                .newActor()
                .withConsumerScript((Integer x) -> System.out.println(x))
                .create();

        // Connect the network: actor -> printer.
        actor.output().connect(printer.input());

        // Send messages through the network.
        actor.input().send(10);
        actor.input().send(20);
    }

    private static final class PrimeFinder
            implements Actor.ContextScript<Integer, Integer>
    {

        @Override
        public void execute (final Context<Integer, Integer> context,
                             final Integer input)
        {
            for (int i = 0; i < input; i++)
            {
                if (BigInteger.valueOf(i).isProbablePrime(100))
                {
                    context.sendFrom(i);
                }
            }
        }
    }
}
```

**Example Output***

```
2
3
5
7
2
3
5
7
11
13
17
19
```
