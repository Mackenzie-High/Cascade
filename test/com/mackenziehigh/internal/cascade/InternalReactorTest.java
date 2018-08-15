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
import com.mackenziehigh.cascade.Reactor.Input.OverflowPolicy;
import com.mackenziehigh.cascade.Reactor.Output;
import com.mackenziehigh.cascade.Reactor.Reaction;
import com.mackenziehigh.cascade.Reactors;
import com.mackenziehigh.internal.cascade.powerplants.NopPowerplant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit Test.
 */
public final class InternalReactorTest
{
    private final InternalReactor reactor = (InternalReactor) Reactors.newReactor();

    /**
     * Test: 20180815002620561299
     *
     * <p>
     * Method: <code>newInput</code>
     * </p>
     */
    @Test
    public void test20180815002620561299 ()
    {
        final Input<String> input = reactor.newInput(String.class);
        final InternalInput raw = (InternalInput) input;

        assertTrue(raw.inflowQueue() instanceof LinkedInflowDeque);
        assertEquals(0, input.size());
        assertEquals(Integer.MAX_VALUE, input.capacity());
        assertEquals(OverflowPolicy.DROP_INCOMING, input.overflowPolicy());
        assertEquals(String.class, input.type());
        assertEquals(reactor, input.reactor());
        assertFalse(input.isConnected());
    }

    /**
     * Test: 20180815002620561387
     *
     * <p>
     * Method: <code>newOutput</code>
     * </p>
     */
    @Test
    public void test20180815002620561387 ()
    {
        final Output<String> output = reactor.newOutput(String.class);

        assertEquals(0, output.size());
        assertEquals(0, output.capacity());
        assertEquals(reactor, output.reactor());
        assertFalse(output.isConnected());
    }

    /**
     * Test: 20180815002620561417
     *
     * <p>
     * Method: <code>newReaction</code>
     * </p>
     */
    @Test
    public void test20180815002620561417 ()
    {
        final Reaction reaction = reactor.newReaction();

        assertTrue(reaction instanceof InternalReaction);
    }

    /**
     * Test: 20180815002620561454
     *
     * <p>
     * Method: <code>name</code>
     * </p>
     */
    @Test
    public void test20180815002620561454 ()
    {
        /**
         * Default.
         */
        assertEquals(reactor.uuid().toString(), reactor.name());

        /**
         * Non Default.
         */
        reactor.named("Anna");
        assertEquals("Anna", reactor.name());
    }

    /**
     * Test: 20180815002620561482
     *
     * <p>
     * Method: <code>uuid</code>
     * </p>
     */
    @Test
    public void test20180815002620561482 ()
    {
        /**
         * The same reactor always has the same UUID.
         */
        assertEquals(reactor.uuid(), reactor.uuid());

        /**
         * Different reactors have different UUIDs.
         */
        assertNotEquals(reactor.uuid(), Reactors.newReactor().uuid());
    }

    /**
     * Test: 20180815002620561506
     *
     * <p>
     * Method: <code>inputs</code>
     * </p>
     */
    @Test
    public void test20180815002620561506 ()
    {
        final Set<Input<?>> expected = new HashSet<>();
        expected.add(reactor.newInput(String.class));
        expected.add(reactor.newInput(Integer.class));
        expected.add(reactor.newInput(Double.class));

        assertEquals(expected, reactor.inputs());
    }

    /**
     * Test: 20180815002620561532
     *
     * <p>
     * Method: <code>outputs</code>
     * </p>
     */
    @Test
    public void test20180815002620561532 ()
    {
        final Set<Output<?>> expected = new HashSet<>();
        expected.add(reactor.newOutput(String.class));
        expected.add(reactor.newOutput(Integer.class));
        expected.add(reactor.newOutput(Double.class));

        assertEquals(expected, reactor.outputs());
    }

    /**
     * Test: 20180815002620561558
     *
     * <p>
     * Method: <code>reactions</code>
     * </p>
     */
    @Test
    public void test20180815002620561558 ()
    {
        final List<Reaction> expected = new ArrayList<>();
        expected.add(reactor.newReaction());
        expected.add(reactor.newReaction());
        expected.add(reactor.newReaction());

        assertEquals(expected, reactor.reactions());
    }

    /**
     * Test: 20180815002620561583
     *
     * <p>
     * Method: <code>disconnect</code>
     * </p>
     */
    @Test
    public void test20180815002620561583 ()
    {
        final Reactor peer = Reactors.newReactor();

        final Input<String> peerIn1 = peer.newInput(String.class);
        final Input<String> peerIn2 = peer.newInput(String.class);
        final Output<String> peerOut1 = peer.newOutput(String.class);
        final Output<String> peerOut2 = peer.newOutput(String.class);

        final Input<String> selfIn1 = reactor.newInput(String.class);
        final Input<String> selfIn2 = reactor.newInput(String.class);
        final Output<String> selfOut1 = reactor.newOutput(String.class);
        final Output<String> selfOut2 = reactor.newOutput(String.class);

        peerIn1.connect(selfOut1);
        peerIn2.connect(selfOut2);
        peerOut1.connect(selfIn1);
        peerOut2.connect(selfIn2);

        assertTrue(selfIn1.isConnected());
        assertTrue(selfIn2.isConnected());
        assertTrue(selfOut1.isConnected());
        assertTrue(selfOut2.isConnected());

        /**
         * Method Under Test.
         */
        reactor.disconnect();

        assertFalse(selfIn1.isConnected());
        assertFalse(selfIn2.isConnected());
        assertFalse(selfOut1.isConnected());
        assertFalse(selfOut2.isConnected());
    }

    /**
     * Test: 20180815002620561607
     *
     * <p>
     * Method: <code>isReacting</code>
     * </p>
     */
    @Test
    public void test20180815002620561607 ()
    {
        final List<Boolean> list = new ArrayList();

        assertFalse(reactor.isReacting());

        reactor.newReaction().onMatch(() -> list.add(reactor.isReacting()));
        reactor.crank();

        assertFalse(reactor.isReacting());

        assertEquals(1, list.size());
        assertTrue(list.get(0));
    }

    /**
     * Test: 20180815002955070440
     *
     * <p>
     * Method: <code>powerplant</code>
     * </p>
     */
    @Test
    public void test20180815002955070440 ()
    {
        assertTrue(reactor.powerplant() instanceof NopPowerplant);
    }

    /**
     * Test: 20180815002955070518
     *
     * <p>
     * Method: <code>poweredBy</code>
     * </p>
     */
    @Test
    public void test20180815002955070518 ()
    {
        fail();
    }

    /**
     * Test: 20180815002955070546
     *
     * <p>
     * Method: <code>signal</code>
     * </p>
     *
     * <p>
     * Case:
     * </p>
     */
    @Test
    public void test20180815002955070546 ()
    {
        System.out.println("Test: 20180815002955070546");
        fail();
    }

    /**
     * Test: 20180815002955070572
     *
     * <p>
     * Method: <code>crank</code>
     * </p>
     *
     * <p>
     * Case:
     * </p>
     */
    @Test
    public void test20180815002955070572 ()
    {
        System.out.println("Test: 20180815002955070572");
        fail();
    }

    /**
     * Test: 20180815002955070600
     *
     * <p>
     * Method: <code>toString</code>
     * </p>
     */
    @Test
    public void test20180815002955070600 ()
    {
        /**
         * Default.
         */
        assertEquals(reactor.uuid().toString(), reactor.toString());

        /**
         * Non Default.
         */
        reactor.named("Anna");
        assertEquals("Anna", reactor.toString());
    }

}
