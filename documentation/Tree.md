# Example - Network of Actors - Tree

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

        // Create the actors.
        final Actor<Integer, Integer> root = stage
                .newActor()
                .withFunctionScript(this::script1)
                .create();

        final Actor<Integer, Integer> square = stage
                .newActor()
                .withFunctionScript(this::script2)
                .create();

        final Actor<Integer, Integer> cube = stage
                .newActor()
                .withFunctionScript(this::script3)
                .create();

        final Actor<Integer, Integer> squareSquare = stage
                .newActor()
                .withConsumerScript(this::script4)
                .create();

        final Actor<Integer, Integer> cubeSquare = stage
                .newActor()
                .withConsumerScript(this::script5)
                .create();

        final Actor<Integer, Integer> squareCube = stage
                .newActor()
                .withConsumerScript(this::script6)
                .create();

        final Actor<Integer, Integer> cubeCube = stage
                .newActor()
                .withConsumerScript(this::script7)
                .create();

        // Connect the actors to form a tree.
        root.output().connect(square.input());
        root.output().connect(cube.input());
        square.output().connect(squareSquare.input());
        square.output().connect(cubeSquare.input());
        cube.output().connect(squareCube.input());
        cube.output().connect(cubeCube.input());

        // Send a message through the tree.
        root.input().send(2);
    }

    private Integer script1 (final Integer message)
    {
        System.out.printf("script1: analyze(%s)\n", message);
        return message; // To Next Actor
    }

    private Integer script2 (final Integer message)
    {
        final int result = message * message;
        System.out.printf("script2: square(%s) = %d\n", message, result);
        return result; // To Next Actor
    }

    private Integer script3 (final Integer message)
    {
        final int result = message * message * message;
        System.out.printf("script3: cube(%s) = %d\n", message, result);
        return result; // To Next Actor
    }

    private void script4 (final Integer message)
    {
        System.out.printf("script4: square(%d) = %d\n", message, message * message);
    }

    private void script5 (final Integer message)
    {
        System.out.printf("script5: cube(%d) = %d\n", message, message * message * message);
    }

    private void script6 (final Integer message)
    {
        System.out.printf("script6: square(%d) = %d\n", message, message * message);
    }

    private void script7 (final Integer message)
    {
        System.out.printf("script7: cube(%d) = %d\n", message, message * message * message);
    }

}
```

**Output:**
```
script1: analyze(2)
script2: square(2) = 4
script3: cube(2) = 8
script4: square(4) = 16
script5: cube(4) = 64
script6: square(8) = 64
script7: cube(8) = 512
```
