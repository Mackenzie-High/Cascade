# Example - Network of Actors - Pipeline

**Code:**

```java
package examples;

import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.Cascade.Stage;
import com.mackenziehigh.cascade.Cascade.Stage.Actor;
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
        // Create a custom stage.
        final Stage stage = Cascade.newStage();

        // Create the actors.
        final Actor<Integer, Integer> printSquare = stage
                .newActor()
                .withFunctionScript(this::script1)
                .create();

        final Actor<Integer, Integer> printCube = stage
                .newActor()
                .withFunctionScript(this::script2)
                .create();

        final Actor<Integer, Integer> printPrimality = stage
                .newActor()
                .withConsumerScript(this::script3)
                .create();

        // Connect the actors to form a pipeline.
        printSquare.output().connect(printCube.input());
        printCube.output().connect(printPrimality.input());

        // Send a message through the pipeline.
        printSquare.input().send(3);
    }

    private Integer script1 (final Integer message)
    {
        System.out.printf("square(%s) = %d\n", message, message * message);
        return message; // To Next Actor
    }

    private Integer script2 (final Integer message)
    {
        System.out.printf("cube(%s) = %d\n", message, message * message * message);
        return message; // To Next Actor
    }

    private void script3 (final Integer message)
    {
        final boolean primality = BigInteger.valueOf(message).isProbablePrime(100);
        System.out.printf("prime(%d) = %s\n", message, primality);
    }

}
```

**Output:**
```
square(3) = 9
cube(3) = 27
prime(3) = true
```
