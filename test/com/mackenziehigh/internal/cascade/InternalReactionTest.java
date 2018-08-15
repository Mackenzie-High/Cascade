/*
 * Copyright 2018 Michael Mackenzie High
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mackenziehigh.internal.cascade;

import com.mackenziehigh.cascade.Reactor;
import com.mackenziehigh.cascade.Reactor.Input;
import com.mackenziehigh.cascade.Reactor.Output;
import com.mackenziehigh.cascade.Reactors;
import java.util.ArrayList;
import java.util.List;
import static junit.framework.Assert.*;
import org.junit.Test;

/**
 * Unit Test.
 */
public final class InternalReactionTest
{
    private final Reactor reactor = Reactors.newReactor();

    private final InternalReaction reaction = new InternalReaction(reactor);

    private final Input<String> input = reactor.newInput(String.class);

    private final Output<String> output = reactor.newOutput(String.class);

    /**
     * Test: 20180529201504798481
     *
     * <p>
     * Method: <code>name</code>
     * </p>
     *
     * <p>
     * Case: Default.
     * </p>
     */
    @Test
    public void test20180529201504798481 ()
    {
        assertEquals(reaction.uuid().toString(), reaction.name());
    }

    /**
     * Test: 20180529201504798589
     *
     * <p>
     * Method: <code>name</code>
     * </p>
     *
     * <p>
     * Case: After Assignment.
     * </p>
     */
    @Test
    public void test20180529201504798589 ()
    {
        reaction.named("Combustion");
        assertEquals("Combustion", reaction.name());
    }

    /**
     * Test: 20180529201504798616
     *
     * <p>
     * Method: <code>uuid</code>
     * </p>
     *
     * <p>
     * Case: Normal.
     * </p>
     */
    @Test
    public void test20180529201504798616 ()
    {
        assertNotNull(reaction.uuid());
        assertTrue(reaction.uuid() == reaction.uuid()); // Same Identity
    }

    /**
     * Test: 20180529201504798643
     *
     * <p>
     * Method: <code>require(Input)</code>
     * </p>
     *
     * <p>
     * Case: Empty Input.
     * </p>
     */
    @Test
    public void test20180529201504798643 ()
    {
        reaction.require(input);

        assertTrue(input.isEmpty());
        assertFalse(reaction.isReady());
    }

    /**
     * Test: 20180529201504798668
     *
     * <p>
     * Method: <code>require(Input)</code>
     * </p>
     *
     * <p>
     * Case: Non-Empty Input.
     * </p>
     */
    @Test
    public void test20180529201504798668 ()
    {
        reaction.require(input);

        input.send("X");
        assertFalse(input.isEmpty());

        assertTrue(reaction.isReady());
    }

    /**
     * Test: 20180814230817048326
     *
     * <p>
     * Method: <code>require(Input, int)</code>
     * </p>
     *
     * <p>
     * Case: Insufficient Inputs.
     * </p>
     */
    @Test
    public void test20180814230817048326 ()
    {
        reaction.require(input, 2);

        input.send("X"); // Only one, but two required.

        assertFalse(reaction.isReady());
    }

    /**
     * Test: 20180814230817048405
     *
     * <p>
     * Method: <code>require(Input, int)</code>
     * </p>
     *
     * <p>
     * Case: Sufficient Inputs.
     * </p>
     */
    @Test
    public void test20180814230817048405 ()
    {
        reaction.require(input, 2);

        input.send("A").send("B");

        assertTrue(reaction.isReady());
    }

    /**
     * Test: 20180814230817048433
     *
     * <p>
     * Method: <code>require(Input, Predicate)</code>
     * </p>
     *
     * <p>
     * Case: Insufficient Inputs.
     * </p>
     */
    @Test
    public void test20180814230817048433 ()
    {
        reaction.require(input, x -> x.contains("X"));

        assertFalse(reaction.isReady());
    }

    /**
     * Test: 20180814231132121290
     *
     * <p>
     * Method: <code>require(Input, Predicate)</code>
     * </p>
     *
     * <p>
     * Case: Input does not match.
     * </p>
     */
    @Test
    public void test20180814231132121290 ()
    {
        reaction.require(input, x -> x.contains("X"));

        input.send("Y");

        assertFalse(reaction.isReady());
    }

    /**
     * Test: 20180814231132121362
     *
     * <p>
     * Method: <code>require(Input, Predicate)</code>
     * </p>
     *
     * <p>
     * Case: Input Matches.
     * </p>
     */
    @Test
    public void test20180814231132121362 ()
    {
        reaction.require(input, x -> x.contains("X"));

        input.send("X");

        assertTrue(reaction.isReady());
    }

    /**
     * Test: 20180814231227117940
     *
     * <p>
     * Method: <code>require(Output)</code>
     * </p>
     *
     * <p>
     * Case: Insufficient Capacity.
     * </p>
     */
    @Test
    public void test20180814231227117940 ()
    {
        reaction.require(output);

        input.useArrayInflowDeque(3).send("A").send("B").send("C");
        output.connect(input);
        assertTrue(output.isFull());

        assertFalse(reaction.isReady());
    }

    /**
     * Test: 20180814231227118017
     *
     * <p>
     * Method: <code>require(Output)</code>
     * </p>
     *
     * <p>
     * Case: Sufficient Capacity.
     * </p>
     */
    @Test
    public void test20180814231227118017 ()
    {
        reaction.require(output);

        input.useArrayInflowDeque(3).send("A").send("B");
        output.connect(input);
        assertFalse(output.isFull());

        assertTrue(reaction.isReady());
    }

    /**
     * Test: 20180814231312359176
     *
     * <p>
     * Method: <code>require(Output)</code>
     * </p>
     *
     * <p>
     * Case: No Connection.
     * </p>
     */
    @Test
    public void test20180814231312359176 ()
    {
        reaction.require(output);

        output.disconnect();

        assertTrue(reaction.isReady());
    }

    /**
     * Test: 20180814231409634684
     *
     * <p>
     * Method: <code>require(Output, int)</code>
     * </p>
     *
     * <p>
     * Case: Insufficient Capacity.
     * </p>
     */
    @Test
    public void test20180814231409634684 ()
    {
        reaction.require(output, 2);

        input.useArrayInflowDeque(3).send("A").send("B");
        output.connect(input);
        assertFalse(output.isFull());
        assertEquals(1, output.remainingCapacity()); // Need (2) or Greater.

        assertFalse(reaction.isReady());
    }

    /**
     * Test: 20180814231409634786
     *
     * <p>
     * Method: <code>require(Output, int)</code>
     * </p>
     *
     * <p>
     * Case: Sufficient Capacity.
     * </p>
     */
    @Test
    public void test20180814231409634786 ()
    {
        reaction.require(output, 2);

        input.useArrayInflowDeque(3).send("A");
        output.connect(input);
        assertFalse(output.isFull());
        assertEquals(2, output.remainingCapacity()); // Need (2) or Greater.

        assertTrue(reaction.isReady());
    }

    /**
     * Test: 20180814231409634816
     *
     * <p>
     * Method: <code>require(Output, int)</code>
     * </p>
     *
     * <p>
     * Case: No Connection.
     * </p>
     */
    @Test
    public void test20180814231409634816 ()
    {
        reaction.require(output, 2);

        output.disconnect();

        assertTrue(reaction.isReady());
    }

    /**
     * Test: 20180814231647123733
     *
     * <p>
     * Method: <code>require(BooleanSupplier)</code>
     * </p>
     *
     * <p>
     * Case: True.
     * </p>
     */
    @Test
    public void test20180814231647123733 ()
    {
        reaction.require(() -> true);

        assertTrue(reaction.isReady());
    }

    /**
     * Test: 20180814231725706948
     *
     * <p>
     * Method: <code>require(BooleanSupplier)</code>
     * </p>
     *
     * <p>
     * Case: False.
     * </p>
     */
    @Test
    public void test20180814231725706948 ()
    {
        reaction.require(() -> false);

        assertFalse(reaction.isReady());
    }

    /**
     * Test: 20180814231725707013
     *
     * <p>
     * Method: <code>require(BooleanSupplier)</code>
     * </p>
     *
     * <p>
     * Case: Exception in Condition.
     * </p>
     */
    @Test
    public void test20180814231725707013 ()
    {
        final List<Throwable> list = new ArrayList<>();

        final OutOfMemoryError error = new OutOfMemoryError();

        reaction.require(() ->
        {
            throw error;
        });

        reaction.onMatch(() -> list.add(new RuntimeException()));
        reaction.onError(x -> list.add(x));

        assertFalse(reaction.isReady());

        assertEquals(1, list.size());
        assertEquals(error, list.get(0));
    }

    /**
     * Test: 20180814231847032606
     *
     * <p>
     * Case: Chain of Actions.
     * </p>
     */
    @Test
    public void test20180814231847032606 ()
    {
        final List<String> list = new ArrayList<>();

        reaction.onMatch(() -> list.add("A"));
        reaction.onMatch(() -> list.add("B"));
        reaction.onMatch(() -> list.add("C"));

        assertTrue(reaction.isReady());

        reaction.crank();

        assertEquals(3, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
    }

    /**
     * Test: 20180815000927074358
     *
     * <p>
     * Case: Exception in Chain of Actions.
     * </p>
     */
    @Test
    public void test20180815000927074358 ()
    {
        final List<Object> list = new ArrayList<>();

        reaction.onMatch(() -> list.add("A"));
        reaction.onMatch(() -> list.add(1 / 0 == 0));
        reaction.onMatch(() -> list.add("B"));

        reaction.onError(x -> list.add("X"));
        reaction.onError(x -> list.add("Y"));

        reaction.crank();

        assertEquals(3, list.size());
        assertEquals("A", list.get(0));
        assertEquals("X", list.get(1));
        assertEquals("Y", list.get(2));
    }

    /**
     * Test: 20180814231847032679
     *
     * <p>
     * Case: Chain of Error Handlers.
     * </p>
     */
    @Test
    public void test20180814231847032679 ()
    {
        final List<Throwable> list = new ArrayList<>();

        final OutOfMemoryError error = new OutOfMemoryError();

        reaction.onMatch(() ->
        {
            throw error;
        });

        reaction.onError(x -> list.add(new RuntimeException(x)));
        reaction.onError(x -> list.add(new IllegalArgumentException(x)));
        reaction.onError(x -> list.add(new IllegalStateException(x)));

        assertTrue(reaction.isReady());
        reaction.crank();

        assertEquals(3, list.size());
        assertTrue(list.get(0) instanceof RuntimeException);
        assertTrue(list.get(1) instanceof IllegalArgumentException);
        assertTrue(list.get(2) instanceof IllegalStateException);
        assertEquals(error, list.get(0).getCause());
        assertEquals(error, list.get(1).getCause());
        assertEquals(error, list.get(2).getCause());
    }

    /**
     * Test: 20180814235529650300
     *
     * <p>
     * Method: <code>toString</code>
     * </p>
     */
    @Test
    public void test20180814235529650300 ()
    {
        /**
         * Default.
         */
        assertEquals(reaction.name(), reaction.toString());

        /**
         * Non-Default.
         */
        reaction.named("H20 -> H2 + O");
        assertEquals(reaction.name(), reaction.toString());
    }

    /**
     * Test: 20180815001817842657
     *
     * <p>
     * Case: Exception in Error Handler.
     * </p>
     */
    @Test
    public void test20180815001817842657 ()
    {
        final List<Object> list = new ArrayList<>();

        reaction.onMatch(() -> list.add("A"));
        reaction.onMatch(() -> list.add(1 / 0 == 0));
        reaction.onMatch(() -> list.add("B"));

        reaction.onError(x -> list.add("X"));
        reaction.onError(x -> list.add(1 / 0 == 0));
        reaction.onError(x -> list.add("Y"));

        reaction.crank();

        assertEquals(2, list.size());
        assertEquals("A", list.get(0));
        assertEquals("X", list.get(1));
    }
}
