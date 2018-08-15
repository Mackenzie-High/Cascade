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
import com.mackenziehigh.cascade.Reactor.Input.OverflowPolicy;
import com.mackenziehigh.cascade.Reactor.Output;
import com.mackenziehigh.cascade.Reactors;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit Test.
 */
public final class InternalInputTest
{
    private final Reactor reactor = Reactors.newReactor();

    private final InternalInput<String> input = new InternalInput<>(reactor, String.class);

    /**
     * Test: 20180812140830546583
     *
     * <p>
     * Method: <code>capacity</code>
     * </p>
     */
    @Test
    public void test20180812140830546583 ()
    {
        /**
         * Default Capacity.
         */
        assertEquals(Integer.MAX_VALUE, input.capacity());

        /**
         * Non-Default Capacity.
         */
        input.useArrayInflowDeque(5);
        assertEquals(5, input.capacity());
    }

    /**
     * Test: 20180812140830546671
     *
     * <p>
     * Method: <code>clear</code>
     * </p>
     */
    @Test
    public void test20180812140830546671 ()
    {
        /**
         * Clear when empty.
         */
        assertEquals(0, input.size());
        input.clear();
        assertEquals(0, input.size());

        /**
         * Clear when non-empty.
         */
        input.send("A").send("B").send("C");
        assertEquals(3, input.size());
        input.clear();
        assertEquals(0, input.size());
    }

    /**
     * Test: 20180812140830546700
     *
     * <p>
     * Method: <code>connect</code>
     * </p>
     *
     * <p>
     * Case: Not Yet Connected.
     * </p>
     */
    @Test
    public void test20180812140830546700 ()
    {
        final Output<String> output = reactor.newOutput(String.class);

        assertFalse(input.connection().isPresent());
        assertFalse(output.connection().isPresent());

        input.connect(output);

        assertEquals(output, input.connection().get());
        assertEquals(input, output.connection().get());
    }

    /**
     * Test: 20180812142658798664
     *
     * <p>
     * Method: <code>connect</code>
     * </p>
     *
     * <p>
     * Case: Already connected to the same output.
     * </p>
     */
    @Test
    public void test20180812142658798664 ()
    {
        final Output<String> output = reactor.newOutput(String.class);

        /**
         * Setup the initial connection.
         */
        assertFalse(input.connection().isPresent());
        assertFalse(output.connection().isPresent());
        input.connect(output);
        assertEquals(output, input.connection().get());
        assertEquals(input, output.connection().get());

        /**
         * Perform Actual Test.
         */
        input.connect(output);
        assertEquals(output, input.connection().get());
        assertEquals(input, output.connection().get());
    }

    /**
     * Test: 20180812142658798745
     *
     * <p>
     * Method: <code>connect</code>
     * </p>
     *
     * <p>
     * Case: Already connected to a different output.
     * </p>
     */
    @Test (expected = IllegalStateException.class)
    public void test20180812142658798745 ()
    {
        final Output<String> output1 = reactor.newOutput(String.class);
        final Output<String> output2 = reactor.newOutput(String.class);

        /**
         * Setup the initial connection.
         */
        assertFalse(input.connection().isPresent());
        assertFalse(output1.connection().isPresent());
        input.connect(output1);
        assertEquals(output1, input.connection().get());
        assertEquals(input, output1.connection().get());
        assertFalse(output2.connection().isPresent());

        /**
         * Perform Actual Test.
         */
        input.connect(output2);
    }

    /**
     * Test: 20180812140830546727
     *
     * <p>
     * Method: <code>connection</code>
     * </p>
     */
    @Test
    public void test20180812140830546727 ()
    {
        final Output<String> output = reactor.newOutput(String.class);

        assertFalse(input.connection().isPresent());
        input.connect(output);
        assertEquals(output, input.connection().get());
    }

    /**
     * Test: 20180812140830546752
     *
     * <p>
     * Method: <code>disconnect</code>
     * </p>
     *
     * <p>
     * Case: Already connected.
     * </p>
     */
    @Test
    public void test20180812140830546752 ()
    {
        final Output<String> output = reactor.newOutput(String.class);

        /**
         * Setup the connection.
         */
        assertFalse(input.connection().isPresent());
        assertFalse(output.connection().isPresent());
        input.connect(output);
        assertEquals(output, input.connection().get());
        assertEquals(input, output.connection().get());

        /**
         * Perform Actual Test.
         */
        input.disconnect();
        assertFalse(input.connection().isPresent());
        assertFalse(output.connection().isPresent());
    }

    /**
     * Test: 20180812144601728764
     *
     * <p>
     * Method: <code>disconnect</code>
     * </p>
     *
     * <p>
     * Case: Already disconnected.
     * </p>
     */
    @Test
    public void test20180812144601728764 ()
    {
        final Output<String> output = reactor.newOutput(String.class);

        assertFalse(input.connection().isPresent());
        assertFalse(output.connection().isPresent());
        input.disconnect();
        assertFalse(input.connection().isPresent());
        assertFalse(output.connection().isPresent());
    }

    /**
     * Test: 20180812140830546773
     *
     * <p>
     * Method: <code>forEach</code>
     * </p>
     *
     * <p>
     * Case:
     * </p>
     */
    @Test
    public void test20180812140830546773 ()
    {
        input.send("A").send("B").send("C");

        final List<String> actual = new ArrayList<>();
        input.forEach(x -> actual.add(x));

        assertEquals("A", actual.get(0));
        assertEquals("B", actual.get(1));
        assertEquals("C", actual.get(2));
    }

    /**
     * Test: 20180812140830546795
     *
     * <p>
     * Method: <code>isFull</code>
     * </p>
     *
     * <p>
     * Case:
     * </p>
     */
    @Test
    public void test20180812140830546795 ()
    {
        assertFalse(input.isFull());
        input.useArrayInflowDeque(3);
        assertFalse(input.isFull());

        input.send("A");
        assertFalse(input.size() == input.capacity());
        assertFalse(input.isFull());

        input.send("B");
        assertFalse(input.size() == input.capacity());
        assertFalse(input.isFull());

        input.send("C");
        assertTrue(input.size() == input.capacity());
        assertTrue(input.isFull());

        input.poll();
        assertFalse(input.size() == input.capacity());
        assertFalse(input.isFull());

        input.poll();
        assertFalse(input.size() == input.capacity());
        assertFalse(input.isFull());

        input.send("D");
        assertFalse(input.size() == input.capacity());
        assertFalse(input.isFull());

        input.send("E");
        assertTrue(input.size() == input.capacity());
        assertTrue(input.isFull());
    }

    /**
     * Test: 20180812140830546818
     *
     * <p>
     * Method: <code>name</code>
     * </p>
     */
    @Test
    public void test20180812140830546818 ()
    {
        /**
         * Default Name.
         */
        assertEquals(input.uuid().toString(), input.name());

        /**
         * Non-Default Name.
         */
        input.named("Potomac");
        assertEquals("Potomac", input.name());
    }

    /**
     * Test: 20180812140830546840
     *
     * <p>
     * Method: <code>named</code>
     * </p>
     *
     * <p>
     * Case:
     * </p>
     */
    @Test
    public void test20180812140830546840 ()
    {
        input.named("Potomac");
        assertEquals("Potomac", input.name());

        input.named("Shenandoah");
        assertEquals("Shenandoah", input.name());
    }

    /**
     * Test: 20180812140830546864
     *
     * <p>
     * Method: <code>overflowPolicy</code>
     * </p>
     *
     * <p>
     * Case:
     * </p>
     */
    @Test
    public void test20180812140830546864 ()
    {
        /**
         * Default.
         */
        assertEquals(OverflowPolicy.DROP_INCOMING, input.overflowPolicy());

        /**
         * Non-Default.
         */
        input.useArrayInflowDeque(3, OverflowPolicy.THROW);
        assertEquals(OverflowPolicy.THROW, input.overflowPolicy());
    }

    /**
     * Test: 20180812141218449134
     *
     * <p>
     * Method: <code>peek</code>
     * </p>
     *
     * <p>
     * Case:
     * </p>
     */
    @Test
    public void test20180812141218449134 ()
    {
        assertFalse(input.peek().isPresent());

        input.send("A").send("B").send("C");

        assertEquals("A", input.peek().get());
        assertEquals("A", input.peek().get());
        input.poll();

        assertEquals("B", input.peek().get());
        assertEquals("B", input.peek().get());
        input.poll();

        assertEquals("C", input.peek().get());
        assertEquals("C", input.peek().get());
        input.poll();

        assertFalse(input.peek().isPresent());
    }

    /**
     * Test: 20180812141218449205
     *
     * <p>
     * Method: <code>peekOrDefault</code>
     * </p>
     *
     * <p>
     * Case: Non-Null Default-Value.
     * </p>
     */
    @Test
    public void test20180812141218449205 ()
    {
        assertEquals("X", input.peekOrDefault("X"));

        input.send("A").send("B").send("C");

        assertEquals("A", input.peekOrDefault("X"));
        assertEquals("A", input.peekOrDefault("X"));
        input.poll();

        assertEquals("B", input.peekOrDefault("X"));
        assertEquals("B", input.peekOrDefault("X"));
        input.poll();

        assertEquals("C", input.peekOrDefault("X"));
        assertEquals("C", input.peekOrDefault("X"));
        input.poll();

        assertEquals("X", input.peekOrDefault("X"));
    }

    /**
     * Test: 20180812150446220894
     *
     * <p>
     * Method: <code>peekOrDefault</code>
     * </p>
     *
     * <p>
     * Case: Null Default-Value.
     * </p>
     */
    @Test
    public void test20180812150446220894 ()
    {
        assertEquals(null, input.peekOrDefault(null));

        input.send("A").send("B").send("C");

        assertEquals("A", input.peekOrDefault(null));
        assertEquals("A", input.peekOrDefault(null));
        input.poll();

        assertEquals("B", input.peekOrDefault(null));
        assertEquals("B", input.peekOrDefault(null));
        input.poll();

        assertEquals("C", input.peekOrDefault(null));
        assertEquals("C", input.peekOrDefault(null));
        input.poll();

        assertEquals(null, input.peekOrDefault(null));
    }

    /**
     * Test: 20180812141218449233
     *
     * <p>
     * Method: <code>peekOrNull</code>
     * </p>
     */
    @Test
    public void test20180812141218449233 ()
    {
        assertEquals(null, input.peekOrNull());

        input.send("A").send("B").send("C");

        assertEquals(3, input.size());
        assertEquals("A", input.peekOrNull());
        assertEquals("A", input.peekOrNull());
        input.poll();

        assertEquals(2, input.size());
        assertEquals("B", input.peekOrNull());
        assertEquals("B", input.peekOrNull());
        input.poll();

        assertEquals(1, input.size());
        assertEquals("C", input.peekOrNull());
        assertEquals("C", input.peekOrNull());
        input.poll();

        assertEquals(0, input.size());
        assertEquals(null, input.peekOrNull());
    }

    /**
     * Test: 20180812141218449258
     *
     * <p>
     * Method: <code>poll</code>
     * </p>
     */
    @Test
    public void test20180812141218449258 ()
    {
        assertEquals(0, input.size());
        assertFalse(input.poll().isPresent());

        input.send("A").send("B").send("C");

        assertEquals(3, input.size());
        assertEquals("A", input.poll().get());

        assertEquals(2, input.size());
        assertEquals("B", input.poll().get());

        assertEquals(1, input.size());
        assertEquals("C", input.poll().get());

        assertEquals(0, input.size());
        assertFalse(input.poll().isPresent());
    }

    /**
     * Test: 20180812141218449283
     *
     * <p>
     * Method: <code>pollOrDefault</code>
     * </p>
     *
     * <p>
     * Case: Non-Null Default-Value.
     * </p>
     */
    @Test
    public void test20180812141218449283 ()
    {
        assertEquals(0, input.size());
        assertEquals("X", input.pollOrDefault("X"));

        input.send("A").send("B").send("C");

        assertEquals(3, input.size());
        assertEquals("A", input.pollOrDefault("X"));

        assertEquals(2, input.size());
        assertEquals("B", input.pollOrDefault("X"));

        assertEquals(1, input.size());
        assertEquals("C", input.pollOrDefault("X"));

        assertEquals(0, input.size());
        assertEquals("X", input.pollOrDefault("X"));
    }

    /**
     * Test: 20180812164611923365
     *
     * <p>
     * Method: <code>pollOrDefault</code>
     * </p>
     *
     * <p>
     * Case: Null Default-Value.
     * </p>
     */
    @Test
    public void test20180812164611923365 ()
    {
        assertEquals(0, input.size());
        assertEquals(null, input.pollOrDefault(null));

        input.send("A").send("B").send("C");

        assertEquals(3, input.size());
        assertEquals("A", input.pollOrDefault(null));

        assertEquals(2, input.size());
        assertEquals("B", input.pollOrDefault(null));

        assertEquals(1, input.size());
        assertEquals("C", input.pollOrDefault(null));

        assertEquals(0, input.size());
        assertEquals(null, input.pollOrDefault(null));
    }

    /**
     * Test: 20180812141401783424
     *
     * <p>
     * Method: <code>pollOrNull</code>
     * </p>
     */
    @Test
    public void test20180812141401783424 ()
    {
        assertEquals(0, input.size());
        assertEquals(null, input.pollOrNull());

        input.send("A").send("B").send("C");

        assertEquals(3, input.size());
        assertEquals("A", input.pollOrNull());

        assertEquals(2, input.size());
        assertEquals("B", input.pollOrNull());

        assertEquals(1, input.size());
        assertEquals("C", input.pollOrNull());

        assertEquals(0, input.size());
        assertEquals(null, input.pollOrNull());
    }

    /**
     * Test: 20180812141401783502
     *
     * <p>
     * Method: <code>reactor</code>
     * </p>
     */
    @Test
    public void test20180812141401783502 ()
    {
        // Identity Equality
        assertTrue(reactor == input.reactor());
    }

    /**
     * Test: 20180812141401783532
     *
     * <p>
     * Method: <code>remainingCapacity</code>
     * </p>
     */
    @Test
    public void test20180812141401783532 ()
    {
        /**
         * Case: Default.
         */
        assertEquals(Integer.MAX_VALUE, input.remainingCapacity());

        /**
         * Case: Non-Default.
         */
        input.useArrayInflowDeque(3);

        assertEquals(3, input.remainingCapacity());
        assertEquals(null, input.pollOrNull());

        input.send("A").send("B").send("C");

        assertEquals(0, input.remainingCapacity());
        assertEquals("A", input.pollOrNull());

        assertEquals(1, input.remainingCapacity());
        assertEquals("B", input.pollOrNull());

        assertEquals(2, input.remainingCapacity());
        assertEquals("C", input.pollOrNull());

        assertEquals(3, input.remainingCapacity());
        assertEquals(null, input.pollOrNull());
    }

    /**
     * Test: 20180812141401783558
     *
     * <p>
     * Method: <code>send</code>
     * </p>
     *
     * <p>
     * Case: Basic Functionality.
     * </p>
     */
    @Test
    public void test20180812141401783558 ()
    {
        assertEquals(input, input.send("A"));
        assertEquals(input, input.send("B"));
        assertEquals(input, input.send("C"));
        assertEquals("A", input.pollOrNull());
        assertEquals("B", input.pollOrNull());
        assertEquals("C", input.pollOrNull());
    }

    /**
     * Test: 20180525230353278173
     *
     * <p>
     * Method: <code>send</code>
     * </p>
     *
     * <p>
     * Case: Null Argument.
     * </p>
     */
    @Test (expected = NullPointerException.class)
    public void test20180525230353278173 ()
    {
        input.send(null);
    }

    /**
     * Test: 20180525230353278223
     *
     * <p>
     * Method: <code>send</code>
     * </p>
     *
     * <p>
     * Case: Overflow Policy - Drop Oldest.
     * </p>
     */
    @Test
    public void test20180525230353278223 ()
    {
        /**
         * Build the input and fill it up to capacity.
         */
        input.useLinkedInflowDeque(3, OverflowPolicy.DROP_OLDEST);
        input.send("A");
        input.send("B");
        input.send("C");
        assertEquals(3, input.size());
        assertTrue(input.isFull());
        assertEquals(OverflowPolicy.DROP_OLDEST, input.overflowPolicy());

        /**
         * Try to insert another element, which will overflow the capacity.
         */
        input.send("X");

        /**
         * Verify the the overflow-policy properly resolved the overflow.
         */
        assertEquals(3, input.size());
        assertEquals("B", input.poll().get());
        assertEquals("C", input.poll().get());
        assertEquals("X", input.poll().get());
        assertTrue(input.isEmpty());
    }

    /**
     * Test: 20180525230353278248
     *
     * <p>
     * Method: <code>send</code>
     * </p>
     *
     * <p>
     * Case: Overflow Policy - Drop Newest
     * </p>
     */
    @Test
    public void test20180525230353278248 ()
    {
        /**
         * Build the input and fill it up to capacity.
         */
        input.useLinkedInflowDeque(3, OverflowPolicy.DROP_NEWEST);
        input.send("A");
        input.send("B");
        input.send("C");
        assertEquals(3, input.size());
        assertTrue(input.isFull());
        assertEquals(OverflowPolicy.DROP_NEWEST, input.overflowPolicy());

        /**
         * Try to insert another element, which will overflow the capacity.
         */
        input.send("X");

        /**
         * Verify the the overflow-policy properly resolved the overflow.
         */
        assertEquals(3, input.size());
        assertEquals("A", input.poll().get());
        assertEquals("B", input.poll().get());
        assertEquals("X", input.poll().get());
        assertTrue(input.isEmpty());
    }

    /**
     * Test: 20180525230353278273
     *
     * <p>
     * Method: <code>send</code>
     * </p>
     *
     * <p>
     * Case: Overflow Policy - Drop Pending
     * </p>
     */
    @Test
    public void test20180525230353278273 ()
    {
        /**
         * Build the input and fill it up to capacity.
         */
        input.useLinkedInflowDeque(3, OverflowPolicy.DROP_PENDING);
        input.send("A");
        input.send("B");
        input.send("C");
        assertEquals(3, input.size());
        assertTrue(input.isFull());
        assertEquals(OverflowPolicy.DROP_PENDING, input.overflowPolicy());

        /**
         * Try to insert another element, which will overflow the capacity.
         */
        input.send("X");

        /**
         * Verify the the overflow-policy properly resolved the overflow.
         */
        assertEquals(1, input.size());
        assertEquals("X", input.poll().get());
        assertTrue(input.isEmpty());
    }

    /**
     * Test: 20180525230353278297
     *
     * <p>
     * Method: <code>send</code>
     * </p>
     *
     * <p>
     * Case: Overflow Policy - Drop Incoming
     * </p>
     */
    @Test
    public void test20180525230353278297 ()
    {
        /**
         * Build the input and fill it up to capacity.
         */
        input.useLinkedInflowDeque(3, OverflowPolicy.DROP_INCOMING);
        input.send("A");
        input.send("B");
        input.send("C");
        assertEquals(3, input.size());
        assertTrue(input.isFull());
        assertEquals(OverflowPolicy.DROP_INCOMING, input.overflowPolicy());

        /**
         * Try to insert another element, which will overflow the capacity.
         */
        input.send("X");

        /**
         * Verify the the overflow-policy properly resolved the overflow.
         */
        assertEquals(3, input.size());
        assertEquals("A", input.poll().get());
        assertEquals("B", input.poll().get());
        assertEquals("C", input.poll().get());
        assertTrue(input.isEmpty());
    }

    /**
     * Test: 20180525230353278317
     *
     * <p>
     * Method: <code>send</code>
     * </p>
     *
     * <p>
     * Case: Overflow Policy - Drop All
     * </p>
     */
    @Test
    public void test20180525230353278317 ()
    {
        /**
         * Build the input and fill it up to capacity.
         */
        input.useLinkedInflowDeque(3, OverflowPolicy.DROP_ALL);
        input.send("A");
        input.send("B");
        input.send("C");
        assertEquals(3, input.size());
        assertTrue(input.isFull());
        assertEquals(OverflowPolicy.DROP_ALL, input.overflowPolicy());

        /**
         * Try to insert another element, which will overflow the capacity.
         */
        input.send("X");

        /**
         * Verify the the overflow-policy properly resolved the overflow.
         */
        assertTrue(input.isEmpty());
    }

    /**
     * Test: 20180812141401783583
     *
     * <p>
     * Method: <code>size</code>
     * </p>
     */
    @Test
    public void test20180812141401783583 ()
    {
        assertEquals(0, input.size());
        assertEquals(input, input.send("A"));
        assertEquals(1, input.size());
        assertEquals(input, input.send("B"));
        assertEquals(2, input.size());
        assertEquals(input, input.send("C"));
        assertEquals(3, input.size());
        input.poll();
        assertEquals(2, input.size());
        input.poll();
        assertEquals(1, input.size());
        assertEquals(input, input.send("X"));
        assertEquals(2, input.size());
        assertEquals(input, input.send("Y"));
        assertEquals(3, input.size());
        assertEquals(input, input.send("Z"));
        assertEquals(4, input.size());
    }

    /**
     * Test: 20180812141601593456
     *
     * <p>
     * Method: <code>toString</code>
     * </p>
     */
    @Test
    public void test20180812141601593456 ()
    {
        /**
         * Default.
         */
        assertEquals(input.uuid().toString(), input.toString());

        /**
         * Non-Default.
         */
        input.named("Alerts");
        assertEquals("Alerts", input.toString());
        assertEquals("Alerts", input.name());
    }

    /**
     * Test: 20180812141601593547
     *
     * <p>
     * Method: <code>type</code>
     * </p>
     */
    @Test
    public void test20180812141601593547 ()
    {
        assertEquals(String.class, new InternalInput<>(reactor, String.class).type());
        assertEquals(Double.class, new InternalInput<>(reactor, Double.class).type());
        assertEquals(Integer.class, new InternalInput<>(reactor, Integer.class).type());
    }

    /**
     * Test: 20180812141601593576
     *
     * <p>
     * Method: <code>useArrayInflowDeque(int)</code>
     * </p>
     *
     * <p>
     * Case:
     * </p>
     */
    @Test
    public void test20180812141601593576 ()
    {
        System.out.println("Test: 20180812141601593576");
        fail();
    }

    /**
     * Test: 20180812141721691382
     *
     * <p>
     * Method: <code>useArrayInflowDeque(int, OverflowPolicy)</code>
     * </p>
     *
     * <p>
     * Case:
     * </p>
     */
    @Test
    public void test20180812141721691382 ()
    {
        System.out.println("Test: 20180812141721691382");
        fail();
    }

    /**
     * Test: 20180812141721691460
     *
     * <p>
     * Method: <code>useInflowDeque</code>
     * </p>
     *
     * <p>
     * Case:
     * </p>
     */
    @Test
    public void test20180812141721691460 ()
    {
        System.out.println("Test: 20180812141721691460");
        fail();
    }

    /**
     * Test: 20180812141721691488
     *
     * <p>
     * Method: <code>useLinkedInflowDeque()</code>
     * </p>
     *
     * <p>
     * Case:
     * </p>
     */
    @Test
    public void test20180812141721691488 ()
    {
        System.out.println("Test: 20180812141721691488");
        fail();
    }

    /**
     * Test: 20180812141721691514
     *
     * <p>
     * Method: <code>useLinkedInflowDeque(int)</code>
     * </p>
     *
     * <p>
     * Case:
     * </p>
     */
    @Test
    public void test20180812141721691514 ()
    {
        System.out.println("Test: 20180812141721691514");
        fail();
    }

    /**
     * Test: 20180812141721691541
     *
     * <p>
     * Method: <code>useLinkedInflowDeque(int, OverflowPolicy)</code>
     * </p>
     *
     * <p>
     * Case:
     * </p>
     */
    @Test
    public void test20180812141721691541 ()
    {
        System.out.println("Test: 20180812141721691541");
        fail();
    }

    /**
     * Test: 20180812142031056551
     *
     * <p>
     * Method: <code>uuid</code>
     * </p>
     *
     * <p>
     * Case:
     * </p>
     */
    @Test
    public void test20180812142031056551 ()
    {
        /**
         * Reactors do not get their UUIDs from the enclosing reactors.
         */
        assertNotEquals(reactor.uuid(), input.uuid());

        /**
         * Distinct inputs have distinct UUIDs.
         */
        assertNotEquals(input.uuid(), new InternalInput<>(reactor, String.class).type());

        /**
         * The same input always has the same UUID.
         */
        assertEquals(input.uuid(), input.uuid());
    }

    /**
     * Test: 20180812142031056645
     *
     * <p>
     * Method: <code>verify</code>
     * </p>
     *
     * <p>
     * Case: Order of Verifications.
     * </p>
     */
    @Test
    public void test20180812142031056645 ()
    {
        final List<String> list = new ArrayList<>();

        input.verify(x -> list.add("A = " + x));
        input.verify(x -> list.add("B = " + x));
        input.verify(x -> list.add("C = " + x));

        input.send("X");
        input.send("Y");
        input.send("Z");

        assertEquals(9, list.size());
        assertEquals("A = X", list.get(0));
        assertEquals("B = X", list.get(1));
        assertEquals("C = X", list.get(2));
        assertEquals("A = Y", list.get(3));
        assertEquals("B = Y", list.get(4));
        assertEquals("C = Y", list.get(5));
        assertEquals("A = Z", list.get(6));
        assertEquals("B = Z", list.get(7));
        assertEquals("C = Z", list.get(8));
    }

    /**
     * Test: 20180814220932721419
     *
     * <p>
     * Method: <code>verify</code>
     * </p>
     *
     * <p>
     * Case: Violation of Condition.
     * </p>
     */
    @Test (expected = IllegalArgumentException.class)
    public void test20180814220932721419 ()
    {
        input.verify(x -> x.contains("A")).send("X");
    }

    /**
     * Test: 20180814225854120362
     *
     * <p>
     * Method: <code>isConnected</code>
     * </p>
     */
    @Test
    public void test20180814225854120362 ()
    {
        final Output<String> output = reactor.newOutput(String.class);

        assertFalse(input.isConnected());
        input.connect(output);
        assertTrue(input.isConnected());
    }
}
