package com.mackenziehigh.internal.cascade;

import java.util.UUID;
import static junit.framework.Assert.*;
import org.junit.Test;

/**
 *
 * TODO: Test pings
 */
public final class InternalOutputTest
{
    private final MockReactor reactor = new MockReactor();

    private final InternalInput<String> input = new InternalInput<>(reactor, String.class).withCapacity(8).build();

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
     * Test: 20180527124836248172
     *
     * <p>
     * Method: <code>name</code>
     * </p>
     *
     * <p>
     * Case: After build(), No Name Assignment.
     * </p>
     */
    @Test
    public void test20180527124836248172 ()
    {
        output.build();
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
     * Case: After build(), Name Assigned.
     * </p>
     */
    @Test
    public void test20180527124836248253 ()
    {
        final String expected = "Vulcan";
        output.named(expected);
        output.build();
        final String actual = output.name();
        assertEquals(expected, actual);
    }

    /**
     * Test: 20180527130450509924
     *
     * <p>
     * Method: <code>name</code>
     * </p>
     *
     * <p>
     * Case: Before build(), After Name Assignment.
     * </p>
     */
    @Test
    public void test20180527130450509924 ()
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
     * Test: 20180527123317387752
     *
     * <p>
     * Method: <code>build</code>
     * </p>
     */
    @Test
    public void test20180527123317387752 ()
    {
        final InternalOutput<String> result = output.build();
        assertTrue(result == output); // identity
    }

    /**
     * Test: 20180527153327623371
     *
     * <p>
     * Method: <code>build</code>
     * </p>
     *
     * <p>
     * Case: Duplicate Invocation.
     * </p>
     */
    @Test (expected = IllegalStateException.class)
    public void test20180527153327623371 ()
    {
        output.build();
        output.build();
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
        System.out.println("Test: 20180527123317387776");

        final UUID uuid1 = output.uuid();
        output.build();
        final UUID uuid2 = output.uuid();

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
        /**
         * Before build().
         */
        assertEquals(String.class, output.type());

        /**
         * After build().
         */
        output.build();
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
     * Case:
     * </p>
     */
    @Test
    public void test20180527123535256225 ()
    {
        System.out.println("Test: 20180527123535256225");
        fail();
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
        output.build();
        output.connect(input);
        output.connect(input);
    }

    /**
     * Test: 20180527123535256251
     *
     * <p>
     * Method: <code>connect</code>
     * </p>
     *
     * <p>
     * Case: Before build().
     * </p>
     */
    @Test
    public void test20180527123535256251 ()
    {
        /**
         * Before connect().
         */
        assertFalse(output.connection().isPresent());
        assertFalse(output.isFull());

        /**
         * After connect().
         */
        output.connect(input);
        assertTrue(output.connection().isPresent());
        assertEquals(input, output.connection().get());
        assertFalse(output.isFull());
    }

    /**
     * Test: 20180527132754300335
     *
     * <p>
     * Method: <code>connect</code>
     * </p>
     *
     * <p>
     * Case: After build().
     * </p>
     */
    @Test
    public void test20180527132754300335 ()
    {
        output.build();

        /**
         * Before connect().
         */
        assertFalse(output.connection().isPresent());
        assertFalse(output.isFull());

        /**
         * After connect().
         */
        output.connect(input);
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
     * Case: Before build().
     * </p>
     */
    @Test
    public void test20180527123535256278 ()
    {
        /**
         * Before connect().
         */
        assertFalse(output.connection().isPresent());
        assertFalse(output.isFull());

        /**
         * After connect().
         */
        assertEquals(output, output.connect(input));
        assertTrue(output.connection().isPresent());
        assertEquals(input, output.connection().get());
        assertFalse(output.isFull());

        /**
         * After disconnect().
         */
        assertEquals(output, output.disconnect());
        assertFalse(output.connection().isPresent());
        assertFalse(output.isFull());
    }

    /**
     * Test: 20180527133641601967
     *
     * <p>
     * Method: <code>disconnect</code>
     * </p>
     *
     * <p>
     * Case: After build().
     * </p>
     */
    @Test
    public void test20180527133641601967 ()
    {
        output.build();

        /**
         * Before connect().
         */
        assertFalse(output.connection().isPresent());
        assertFalse(output.isFull());

        /**
         * After connect().
         */
        assertEquals(output, output.connect(input));
        assertTrue(output.connection().isPresent());
        assertEquals(input, output.connection().get());
        assertFalse(output.isFull());

        /**
         * After disconnect().
         */
        assertEquals(output, output.disconnect());
        assertFalse(output.connection().isPresent());
        assertFalse(output.isFull());
    }

    /**
     * Test: 20180527123535256340
     *
     * <p>
     * Method: <code>isFull</code>
     * </p>
     *
     * <p>
     * Case: Before build() of Output.
     * </p>
     */
    @Test
    public void test20180527123535256340 ()
    {
        final InternalInput<String> arrayInput = new InternalInput<>(reactor, String.class)
                .withCapacity(3)
                .build();
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
         * The input is now full, but the output is not full,
         * because the output is not built yet.
         */
        assertTrue(arrayInput.isFull());
        assertFalse(underTest.isFull());
    }

    /**
     * Test: 20180527134525242539
     *
     * <p>
     * Method: <code>isFull</code>
     * </p>
     *
     * <p>
     * Case: After build() of Output.
     * </p>
     */
    @Test
    public void test20180527134525242539 ()
    {
        final InternalInput<String> arrayInput = new InternalInput<>(reactor, String.class)
                .withCapacity(3)
                .build();
        final InternalOutput<String> underTest = new InternalOutput<>(reactor, String.class);
        underTest.connect(arrayInput);
        underTest.build();

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
     * Case: Before build().
     * </p>
     */
    @Test
    public void test20180527153828660370 ()
    {
        assertTrue(input.isEmpty());
        output.send("A");
        output.send("B");
        output.send("C");
        assertTrue(input.isEmpty());
    }

    /**
     * Test: 20180527155049600947
     *
     * <p>
     * Method: <code>send</code>
     * </p>
     *
     * <p>
     * Case: After build() and connected.
     * </p>
     */
    @Test
    public void test20180527155049600947 ()
    {
        output.build();
        output.connect(input);

        assertTrue(input.isEmpty());
        output.send("A");
        output.send("B");
        output.send("C");
        assertFalse(input.isEmpty());
        assertEquals(3, input.size());

        assertEquals("A", input.pollOrDefault(null));
        assertEquals("B", input.pollOrDefault(null));
        assertEquals("C", input.pollOrDefault(null));
        assertTrue(input.isEmpty());
    }

    /**
     * Test: 20180527160250712357
     *
     * <p>
     * Method: <code>send</code>
     * </p>
     *
     * <p>
     * Case: After build(), but not connected.
     * </p>
     */
    @Test
    public void test20180527160250712357 ()
    {
        output.build();

        assertTrue(input.isEmpty());
        output.send("A");
        output.send("B");
        output.send("C");
        assertTrue(input.isEmpty());
    }

    /**
     * Test: 20180527161128070338
     *
     * <p>
     * Method: <code>send</code>
     * </p>
     *
     * <p>
     * Case: Before build(), Before connect().
     * </p>
     */
    @Test
    public void test20180527161128070338 ()
    {
        assertTrue(input.isEmpty());
        output.send("A");
        output.send("B");
        output.send("C");
        assertTrue(input.isEmpty());
    }
}
