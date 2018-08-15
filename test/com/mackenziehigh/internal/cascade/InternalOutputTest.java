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
import com.mackenziehigh.cascade.Reactors;
import java.util.UUID;
import static junit.framework.Assert.*;
import org.junit.Test;

/**
 * Unit Test.
 */
public final class InternalOutputTest
{
    private final Reactor reactor = Reactors.newReactor();

    private final Input<String> input = reactor.newInput(String.class);

    private final InternalOutput<String> output = new InternalOutput<>(reactor, String.class);

    /**
     * Test: 20180527123317387638
     *
     * <p>
     * Method: <code>name</code>
     * </p>
     *
     * <p>
     * Case: Before build().
     * </p>
     */
    @Test
    public void test20180527123317387638 ()
    {
        final String expected = output.uuid().toString();
        final String actual = output.name();
        assertEquals(expected, actual);
    }

    /**
     * Test: 20180527124836248253
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
    public void test20180527124836248253 ()
    {
        final String expected = "Vulcan";
        output.named(expected);
        final String actual = output.name();
        assertEquals(expected, actual);
    }

    /**
     * Test: 20180527123317387726
     *
     * <p>
     * Method: <code>verify</code>
     * </p>
     *
     * <p>
     * Case:
     * </p>
     */
    @Test
    public void test20180527123317387726 ()
    {
        System.out.println("Test: 20180527123317387726");
        fail();
    }

    /**
     * Test: 20180527123317387776
     *
     * <p>
     * Method: <code>uuid</code>
     */
    @Test
    public void test20180527123317387776 ()
    {
        final UUID uuid1 = output.uuid();
        final UUID uuid2 = output.uuid();

        assertNotNull(uuid1);
        assertEquals(uuid1, uuid2);
    }

    /**
     * Test: 20180527123535256154
     *
     * <p>
     * Method: <code>type</code>
     * </p>
     */
    @Test
    public void test20180527123535256154 ()
    {
        assertEquals(String.class, output.type());
    }

    /**
     * Test: 20180527123535256225
     *
     * <p>
     * Method: <code>reactor</code>
     * </p>
     *
     * <p>
     * Case: Normal.
     * </p>
     */
    @Test
    public void test20180527123535256225 ()
    {
        assertEquals(reactor, output.reactor());
    }

    /**
     * Test: 20180527161624136215
     *
     * <p>
     * Method: <code>connect</code>
     * </p>
     *
     * <p>
     * Case: Already Connected.
     * </p>
     */
    @Test (expected = IllegalStateException.class)
    public void test20180527161624136215 ()
    {
        final Input<String> otherInput = reactor.newInput(String.class);
        output.connect(input);
        output.connect(otherInput);
    }

    /**
     * Test: 20180708011838173344
     *
     * <p>
     * Method: <code>connect</code>
     * </p>
     *
     * <p>
     * Case: Same Input.
     * </p>
     */
    @Test
    public void test20180708011838173344 ()
    {
        assertFalse(output.connection().isPresent());
        output.connect(input);
        assertEquals(output.connection().get(), input);
        output.connect(input);
        assertEquals(output.connection().get(), input);
    }

    /**
     * Test: 20180527132754300335
     *
     * <p>
     * Method: <code>connect</code>
     * </p>
     *
     * <p>
     * Case: Normal
     * </p>
     */
    @Test
    public void test20180527132754300335 ()
    {
        /**
         * Preconditions.
         */
        assertFalse(output.connection().isPresent());
        assertFalse(output.isFull());

        /**
         * Method Under Test.
         */
        output.connect(input);

        /**
         * Postconditions.
         */
        assertTrue(output.connection().isPresent());
        assertEquals(input, output.connection().get());
        assertFalse(output.isFull());
    }

    /**
     * Test: 20180527123535256278
     *
     * <p>
     * Method: <code>disconnect</code>
     * </p>
     *
     * <p>
     * Case: Normal.
     * </p>
     */
    @Test
    public void test20180527123535256278 ()
    {
        /**
         * Connect.
         */
        assertEquals(output, output.connect(input));

        /**
         * Preconditions.
         */
        assertTrue(output.connection().isPresent());
        assertEquals(input, output.connection().get());
        assertFalse(output.isFull());

        /**
         * Method Under Test.
         */
        assertEquals(output, output.disconnect());

        /**
         * Postconditions.
         */
        assertFalse(output.connection().isPresent());
        assertFalse(output.isFull());
    }

    /**
     * Test: 20180527134525242539
     *
     * <p>
     * Method: <code>isFull</code>
     * </p>
     *
     * <p>
     * Case: Normal.
     * </p>
     */
    @Test
    public void test20180527134525242539 ()
    {
        final Input<String> arrayInput = reactor.newInput(String.class).useLinkedInflowDeque(3);
        final InternalOutput<String> underTest = new InternalOutput<>(reactor, String.class);
        underTest.connect(arrayInput);

        /**
         * The input is not full yet.
         */
        assertFalse(arrayInput.isFull());
        assertFalse(underTest.isFull());
        arrayInput.send("A");
        assertFalse(arrayInput.isFull());
        assertFalse(underTest.isFull());
        arrayInput.send("B");
        assertFalse(arrayInput.isFull());
        assertFalse(underTest.isFull());
        arrayInput.send("C");

        /**
         * The input is now full, which means the output must be full too.
         * since the output is fully built.
         */
        assertTrue(arrayInput.isFull());
        assertTrue(underTest.isFull());
    }

    /**
     * Test: 20180527153828660370
     *
     * <p>
     * Method: <code>send</code>
     * </p>
     *
     * <p>
     * Case: Before Connection Established.
     * </p>
     */
    @Test
    public void test20180527153828660370 ()
    {
        assertTrue(input.isEmpty());
        assertTrue(output.isEmpty());
        output.send("A");
        output.send("B");
        output.send("C");
        assertTrue(input.isEmpty());
        assertTrue(output.isEmpty());
    }

    /**
     * Test: 20180527155049600947
     *
     * <p>
     * Method: <code>send</code>
     * </p>
     *
     * <p>
     * Case: After Connection Established.
     * </p>
     */
    @Test
    public void test20180527155049600947 ()
    {
        output.connect(input);

        assertTrue(input.isEmpty());
        assertTrue(output.isEmpty());
        output.send("A");
        output.send("B");
        output.send("C");
        assertFalse(input.isEmpty());
        assertEquals(3, input.size());
        assertEquals(3, output.size());

        assertEquals("A", input.pollOrDefault(null));
        assertEquals("B", input.pollOrDefault(null));
        assertEquals("C", input.pollOrDefault(null));
        assertTrue(input.isEmpty());
        assertTrue(output.isEmpty());
    }
}
