package com.mackenziehigh.cascade.old.internal;

import com.mackenziehigh.cascade.old.internal.ConcreteSchema;
import com.mackenziehigh.cascade.old.internal.Utils;
import com.mackenziehigh.cascade.old.internal.StandardLogger;
import com.mackenziehigh.cascade.old.internal.ConcreteReactor;
import com.mackenziehigh.cascade.old.internal.InflowQueue;
import com.mackenziehigh.cascade.old.internal.DevNullLogger;
import com.mackenziehigh.cascade.old.internal.ConcretePump;
import com.mackenziehigh.cascade.old.internal.ConcreteAllocator;
import com.mackenziehigh.cascade.old.internal.ArrayInflowQueue;
import com.mackenziehigh.cascade.old.internal.LinkedInflowQueue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mackenziehigh.cascade.old.Cascade;
import com.mackenziehigh.cascade.old.CascadeAllocator;
import com.mackenziehigh.cascade.old.CascadeAllocator.OperandStack;
import com.mackenziehigh.cascade.old.CascadeReactor;
import com.mackenziehigh.cascade.old.CascadeReactor.Core;
import com.mackenziehigh.cascade.old.CascadeReactor.CoreBuilder;
import com.mackenziehigh.cascade.old.CascadeSchema;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import static junit.framework.Assert.*;
import org.junit.Test;

/**
 * Unit Test.
 */
public final class ConcreteSchemaTest
{
    /**
     * Test: 20171230051439691158
     *
     * <p>
     * Case: Scoping
     * </p>
     */
    @Test
    public void test20171230051439691158 ()
    {
        System.out.println("Test: 20171230051439691158");

        final CascadeSchema cs = new ConcreteSchema("schema");
        cs.enter("places");

        // Always Required.
        cs.addDynamicPool("Italy").makeGlobalDefault();

        // name() => places.Canada
        cs.addDynamicPool("Canada");

        // name() => planets.Venus
        cs.addDynamicPool("planets.Venus");

        // name() => places.Australia
        cs.addFixedPool("Australia").withMinimumSize(0).withMaximumSize(100).withBufferCount(13);

        // name() => planets.Mars
        cs.addFixedPool("planets.Mars").withMinimumSize(101).withMaximumSize(200).withBufferCount(17);

        // name() => places.England
        // The fallback-pool and member-pool are referenced by their simple-names.
        cs.addCompositePool("England").withMemberPool("Australia").withFallbackPool("Canada");

        // name() => places.Scotland
        // The fallback-pool and member-pool are referenced by their full-names.
        cs.addCompositePool("Scotland").withMemberPool("places.Australia").withFallbackPool("places.Canada");

        // name() => places.America
        cs.addPump("America");

        // name() => planets.Jupiter
        cs.addPump("planets.Jupiter");

        // name() => places.Russia
        // This reactor uses the default logger and queue settings.
        // We have to explicitly set the pool and pump, since no "using" was specified above.
        cs.addReactor("Russia").withCore(Utils.nop()).usingPool("planets.Mars").usingPump("planets.Jupiter");

        // name() => planets.Neptune
        // This reactor uses the default logger and queue settings.
        // We have to explicitly set the pool and pump, since no "using" was specified above.
        cs.addReactor("planets.Neptune").withCore(Utils.nop()).usingPool("planets.Mars").usingPump("planets.Jupiter");

        // New Defaults.
        cs.usingArrayQueues(128);
        cs.usingPool("Canada");
        cs.usingPump("America");

        // This reactor uses the Canada pool, the America pump, and an array-queue of size (128).
        cs.addReactor("planets.Uranus").withCore(Utils.nop());

        final Cascade cas = cs.build();

        assertEquals("places.Italy", cas.allocator().defaultPool().name().name());
        assertTrue(cas.allocator().pools().get(CascadeToken.token("places.Canada")) instanceof ConcreteAllocator.DynamicAllocationPool);
        assertTrue(cas.allocator().pools().get(CascadeToken.token("planets.Venus")) instanceof ConcreteAllocator.DynamicAllocationPool);
        assertTrue(cas.allocator().pools().get(CascadeToken.token("places.Australia")) instanceof ConcreteAllocator.FixedAllocationPool);
        assertTrue(cas.allocator().pools().get(CascadeToken.token("planets.Mars")) instanceof ConcreteAllocator.FixedAllocationPool);
        assertTrue(cas.allocator().pools().get(CascadeToken.token("places.England")) instanceof ConcreteAllocator.CompositeAllocationPool);
        assertTrue(cas.allocator().pools().get(CascadeToken.token("places.Scotland")) instanceof ConcreteAllocator.CompositeAllocationPool);
        assertNotNull(cas.pumps().get(CascadeToken.token("places.America")));
        assertNotNull(cas.pumps().get(CascadeToken.token("planets.Jupiter")));
        assertNotNull(cas.reactors().get(CascadeToken.token("places.Russia")));
        assertNotNull(cas.reactors().get(CascadeToken.token("planets.Neptune")));
    }

    /**
     * Test: 20171231013350037150
     *
     *
     * <p>
     * Case: No Default Pool
     * </p>
     */
    @Test
    public void test20171231013350037150 ()
    {
        System.out.println("Test: 20171231013350037150");

        try
        {
            final CascadeSchema cs = new ConcreteSchema("schema");
            cs.enter("planets");
            cs.addDynamicPool("Mercury");
            cs.addPump("Venus");
            cs.addReactor("Earth").withCore(Utils.nop()).usingPool("Mercury").usingPump("Venus");
            cs.build();
            fail();
        }
        catch (RuntimeException ex)
        {
            assertTrue(ex.getMessage().toLowerCase().contains("no default allocation-pool was specified"));
        }
    }

    /**
     * Test: 20171231013350037249
     *
     * <p>
     * Case: The default-pool is a dynamic-pool.
     * </p>
     */
    @Test
    public void test20171231013350037249 ()
    {
        System.out.println("Test: 20171231013350037249");

        final CascadeSchema cs = new ConcreteSchema("schema");
        cs.enter("planets");
        cs.addDynamicPool("Mercury").withMinimumSize(100).withMaximumSize(200).makeGlobalDefault();
        cs.addPump("Venus");
        cs.addReactor("Earth").withCore(Utils.nop()).usingPool("Mercury").usingPump("Venus");

        final Cascade cas = cs.build();

        final CascadeAllocator.AllocationPool pool = cas.allocator().defaultPool();
        assertTrue(pool instanceof ConcreteAllocator.DynamicAllocationPool);
        assertFalse(pool.isFixed());
        assertEquals(100, pool.minimumAllocationSize());
        assertEquals(200, pool.maximumAllocationSize());
        assertFalse(pool.capacity().isPresent());
    }

    /**
     * Test: 20171231013350037279
     *
     *
     * <p>
     * Case: The default-pool is a fixed-pool.
     * </p>
     */
    @Test
    public void test20171231013350037279 ()
    {
        System.out.println("Test: 20171231013350037279");

        final CascadeSchema cs = new ConcreteSchema("schema");
        cs.enter("planets");
        cs.addFixedPool("Mercury").withMinimumSize(100).withMaximumSize(200).withBufferCount(13).makeGlobalDefault();
        cs.addPump("Venus");
        cs.addReactor("Earth").withCore(Utils.nop()).usingPool("Mercury").usingPump("Venus");

        final Cascade cas = cs.build();

        final CascadeAllocator.AllocationPool pool = cas.allocator().defaultPool();
        assertTrue(pool instanceof ConcreteAllocator.FixedAllocationPool);
        assertTrue(pool.isFixed());
        assertEquals(100, pool.minimumAllocationSize());
        assertEquals(200, pool.maximumAllocationSize());
        assertEquals(13, pool.capacity().getAsLong());
    }

    /**
     * Test: 20171231013350037305
     *
     * <p>
     * Case: The default-pool is a composite-pool.
     * </p>
     */
    @Test
    public void test20171231013350037305 ()
    {
        System.out.println("Test: 20171231013350037305");

        final CascadeSchema cs = new ConcreteSchema("schema");
        cs.enter("planets");
        cs.addFixedPool("Saturn").withMinimumSize(0).withMaximumSize(9).withBufferCount(10);
        cs.addFixedPool("Neptune").withMinimumSize(6).withMaximumSize(6).withBufferCount(10);
        cs.addFixedPool("Uranus").withMinimumSize(5).withMaximumSize(5).withBufferCount(10);
        cs.addCompositePool("Jovian").withMemberPool("Neptune").withMemberPool("Uranus").withFallbackPool("Saturn").makeGlobalDefault();
        cs.addPump("Venus");
        cs.addReactor("Earth").withCore(Utils.nop()).usingPool("Jovian").usingPump("Venus");

        final Cascade cas = cs.build();

        final CascadeAllocator.AllocationPool pool = cas.allocator().defaultPool();
        assertTrue(pool instanceof ConcreteAllocator.CompositeAllocationPool);

        final OperandStack stack = cas.allocator().newOperandStack();

        /**
         * Use the composite-pool to place an operand into the 'Saturn' pool.
         */
        assertEquals(0, cas.allocator().pools().get(CascadeToken.token("planets.Saturn")).size().getAsLong());
        pool.alloc(stack, "Rhea".getBytes(), 0, 4);
        assertEquals(1, cas.allocator().pools().get(CascadeToken.token("planets.Saturn")).size().getAsLong());

        /**
         * Use the composite-pool to place an operand into the 'Neptune' pool.
         */
        assertEquals(0, cas.allocator().pools().get(CascadeToken.token("planets.Neptune")).size().getAsLong());
        pool.alloc(stack, "Triton".getBytes(), 0, 6);
        assertEquals(1, cas.allocator().pools().get(CascadeToken.token("planets.Neptune")).size().getAsLong());

        /**
         * Use the composite-pool to place an operand into the 'Uranus' pool.
         */
        assertEquals(0, cas.allocator().pools().get(CascadeToken.token("planets.Uranus")).size().getAsLong());
        pool.alloc(stack, "Ariel".getBytes(), 0, 5);
        assertEquals(1, cas.allocator().pools().get(CascadeToken.token("planets.Uranus")).size().getAsLong());
    }

    /**
     * Test: 20171231013350037331
     *
     * <p>
     * Case: Default Min Size and Max Size of Dynamic-Pool.
     * </p>
     */
    @Test
    public void test20171231013350037331 ()
    {
        System.out.println("Test: 20171231013350037331");

        final CascadeSchema cs = new ConcreteSchema("schema");
        cs.enter("planets");
        cs.addDynamicPool("Mercury").makeGlobalDefault();
        cs.addPump("Venus");
        cs.addReactor("Earth").withCore(Utils.nop()).usingPool("Mercury").usingPump("Venus");

        final Cascade cas = cs.build();

        final CascadeAllocator.AllocationPool pool = cas.allocator().defaultPool();
        assertTrue(pool instanceof ConcreteAllocator.DynamicAllocationPool);
        assertFalse(pool.isFixed());
        assertFalse(pool.capacity().isPresent());

        /**
         * Values Under Test.
         */
        assertEquals(0, pool.minimumAllocationSize());
        assertEquals(Integer.MAX_VALUE, pool.maximumAllocationSize());
    }

    /**
     * Test: 20171231013350037353
     *
     * <p>
     * Case: Fixed Pool without Min Size.
     * </p>
     */
    @Test
    public void test20171231013350037353 ()
    {
        System.out.println("Test: 20171231013350037353");

        try
        {
            final CascadeSchema cs = new ConcreteSchema("schema");
            cs.enter("planets");
            cs.addFixedPool("Mercury").withMaximumSize(200).withBufferCount(13).makeGlobalDefault();
            cs.addPump("Venus");
            cs.addReactor("Earth").withCore(Utils.nop()).usingPool("Mercury").usingPump("Venus");
            cs.build();
            fail();
        }
        catch (RuntimeException ex)
        {
            assertTrue(ex.getMessage().toLowerCase().contains("unspecified minimum size"));
        }
    }

    /**
     * Test: 20171231013350037375
     *
     * <p>
     * Case: Fixed Pool without Max Size.
     * </p>
     */
    @Test
    public void test20171231013350037375 ()
    {
        System.out.println("Test: 20171231013350037375");

        try
        {
            final CascadeSchema cs = new ConcreteSchema("schema");
            cs.enter("planets");
            cs.addFixedPool("Mercury").withMinimumSize(200).withBufferCount(13).makeGlobalDefault();
            cs.addPump("Venus");
            cs.addReactor("Earth").withCore(Utils.nop()).usingPool("Mercury").usingPump("Venus");
            cs.build();
            fail();
        }
        catch (RuntimeException ex)
        {
            assertTrue(ex.getMessage().toLowerCase().contains("unspecified maximum size"));
        }
    }

    /**
     * Test: 20171231013350037399
     *
     * <p>
     * Case: Fixed Pool without Buffer Count.
     * </p>
     */
    @Test
    public void test20171231013350037399 ()
    {
        System.out.println("Test: 20171231013350037399");

        try
        {
            final CascadeSchema cs = new ConcreteSchema("schema");
            cs.enter("planets");
            cs.addFixedPool("Mercury").withMinimumSize(100).withMaximumSize(200).makeGlobalDefault();
            cs.addPump("Venus");
            cs.addReactor("Earth").withCore(Utils.nop()).usingPool("Mercury").usingPump("Venus");
            cs.build();
            fail();
        }
        catch (RuntimeException ex)
        {
            assertTrue(ex.getMessage().toLowerCase().contains("unspecified buffer count"));
        }
    }

    /**
     * Test: 20171231013350037449
     *
     * <p>
     * Case: Composite Pool - No Fallback
     * </p>
     */
    @Test
    public void test20171231013350037449 ()
    {
        System.out.println("Test: 20171231013350037449");

        final CascadeSchema cs = new ConcreteSchema("schema");
        cs.enter("planets");
        cs.addFixedPool("Neptune").withMinimumSize(6).withMaximumSize(9).withBufferCount(3);
        cs.addFixedPool("Uranus").withMinimumSize(0).withMaximumSize(5).withBufferCount(3);
        cs.addCompositePool("Jovian").withMemberPool("Neptune").withMemberPool("Uranus").makeGlobalDefault();
        cs.addPump("Venus");
        cs.addReactor("Earth").withCore(Utils.nop()).usingPool("Jovian").usingPump("Venus");

        final Cascade cas = cs.build();

        final CascadeAllocator.AllocationPool pool = cas.allocator().defaultPool();
        assertTrue(pool instanceof ConcreteAllocator.CompositeAllocationPool);

        final OperandStack stack = cas.allocator().newOperandStack();

        /**
         * Use the composite-pool to fill the 'Uranus' pool.
         */
        assertEquals(0, cas.allocator().pools().get(CascadeToken.token("planets.Uranus")).size().getAsLong());
        pool.alloc(stack, "Ariel".getBytes(), 0, 5);
        pool.alloc(stack, "Ariel".getBytes(), 0, 5);
        pool.alloc(stack, "Ariel".getBytes(), 0, 5);
        assertEquals(3, cas.allocator().pools().get(CascadeToken.token("planets.Uranus")).size().getAsLong());

        /**
         * This allocation request will fail, because the 'Uranus' pool is full and no fallback is present.
         */
        assertFalse(pool.tryAlloc(stack, "Ariel".getBytes(), 0, 5));
    }

    /**
     * Test: 20171231014357187392
     *
     * <p>
     * Case: Composite Pool - Only Fallback, No Members
     * </p>
     */
    @Test
    public void test20171231014357187392 ()
    {
        System.out.println("Test: 20171231014357187392");

        final CascadeSchema cs = new ConcreteSchema("schema");
        cs.enter("planets");
        cs.addFixedPool("Uranus").withMinimumSize(4).withMaximumSize(7).withBufferCount(3);
        cs.addCompositePool("Jovian").withFallbackPool("Uranus").makeGlobalDefault();
        cs.addPump("Venus");
        cs.addReactor("Earth").withCore(Utils.nop()).usingPool("Jovian").usingPump("Venus");

        final Cascade cas = cs.build();

        final CascadeAllocator.AllocationPool pool = cas.allocator().defaultPool();
        assertTrue(pool instanceof ConcreteAllocator.CompositeAllocationPool);
        assertEquals(4, pool.minimumAllocationSize());
        assertEquals(7, pool.maximumAllocationSize());

        final OperandStack stack = cas.allocator().newOperandStack();

        /**
         * Use the composite-pool to fill the 'Uranus' pool.
         */
        assertEquals(0, cas.allocator().pools().get(CascadeToken.token("planets.Uranus")).size().getAsLong());
        pool.alloc(stack, "Ariel".getBytes(), 0, 5);
        pool.alloc(stack, "Ariel".getBytes(), 0, 5);
        pool.alloc(stack, "Ariel".getBytes(), 0, 5);
        assertEquals(3, cas.allocator().pools().get(CascadeToken.token("planets.Uranus")).size().getAsLong());

        /**
         * This allocation request will fail, because the 'Uranus' pool is full and it is the fallback;
         * therefore, there does not exist another pool to fall farther back to.
         */
        assertFalse(pool.tryAlloc(stack, "Ariel".getBytes(), 0, 5));
    }

    /**
     * Test: 20171231014357187493
     *
     * <p>
     * Case: Composite Pool - No Fallback, No Members
     * </p>
     */
    @Test
    public void test20171231014357187493 ()
    {
        System.out.println("Test: 20171231014357187493");

        final CascadeSchema cs = new ConcreteSchema("schema");
        cs.enter("planets");
        cs.addCompositePool("Jovian").makeGlobalDefault();
        cs.addPump("Venus");
        cs.addReactor("Earth").withCore(Utils.nop()).usingPool("Jovian").usingPump("Venus");

        final Cascade cas = cs.build();

        final CascadeAllocator.AllocationPool pool = cas.allocator().defaultPool();
        assertTrue(pool instanceof ConcreteAllocator.CompositeAllocationPool);
        assertEquals(0, pool.minimumAllocationSize());
        assertEquals(0, pool.maximumAllocationSize());
    }

    /**
     * Test: 20171231020801872944
     *
     * <p>
     * Case: Composite Pool - No Such Member
     * </p>
     */
    @Test
    public void test20171231020801872944 ()
    {
        System.out.println("Test: 20171231020801872944");

        try
        {
            final CascadeSchema cs = new ConcreteSchema("schema");
            cs.enter("planets");
            cs.addCompositePool("Jovian").withMemberPool("Mercury").makeGlobalDefault();
            cs.addPump("Venus");
            cs.addReactor("Earth").withCore(Utils.nop()).usingPool("Jovian").usingPump("Venus");
            cs.build();
            fail();
        }
        catch (RuntimeException ex)
        {
            assertTrue(ex.getMessage().toLowerCase().contains("no such member"));
        }
    }

    /**
     * Test: 20171231020801873025
     *
     * <p>
     * Case: Composite Pool - No Such Fallback Pool
     * </p>
     */
    @Test
    public void test20171231020801873025 ()
    {
        System.out.println("Test: 20171231020801873025");

        try
        {
            final CascadeSchema cs = new ConcreteSchema("schema");
            cs.enter("planets");
            cs.addCompositePool("Jovian").withFallbackPool("Mercury").makeGlobalDefault();
            cs.addPump("Venus");
            cs.addReactor("Earth").withCore(Utils.nop()).usingPool("Jovian").usingPump("Venus");
            cs.build();
            fail();
        }
        catch (RuntimeException ex)
        {
            assertTrue(ex.getMessage().toLowerCase().contains("no such fallback"));
        }
    }

    /**
     * Test: 20171231040559816896
     *
     * <p>
     * Case: The Member Pool(s) cannot be other Composite Pool(s).
     * </p>
     */
    @Test
    public void test20171231040559816896 ()
    {
        System.out.println("Test: 20171231040559816896");

        try
        {
            final CascadeSchema cs = new ConcreteSchema("schema");
            cs.enter("planets");
            cs.addCompositePool("Terrestrial");
            cs.addCompositePool("Jovian").withMemberPool("Terrestrial").makeGlobalDefault();
            cs.addPump("Venus");
            cs.addReactor("Earth").withCore(Utils.nop()).usingPool("Jovian").usingPump("Venus");
            cs.build();
            fail();
        }
        catch (RuntimeException ex)
        {
            assertTrue(ex.getMessage().toLowerCase().contains("composite member pool"));
        }
    }

    /**
     * Test: 20171231040559816974
     *
     * <p>
     * Case: The Fallback Pool cannot be another Composite Pool.
     * </p>
     */
    @Test
    public void test20171231040559816974 ()
    {
        System.out.println("Test: 20171231040559816974");

        try
        {
            final CascadeSchema cs = new ConcreteSchema("schema");
            cs.enter("planets");
            cs.addCompositePool("Terrestrial");
            cs.addCompositePool("Jovian").withFallbackPool("Terrestrial").makeGlobalDefault();
            cs.addPump("Venus");
            cs.addReactor("Earth").withCore(Utils.nop()).usingPool("Jovian").usingPump("Venus");
            cs.build();
            fail();
        }
        catch (RuntimeException ex)
        {
            assertTrue(ex.getMessage().toLowerCase().contains("composite fallback pool"));
        }
    }

    /**
     * Test: 20171231014357187520
     *
     * <p>
     * Case: No Pumps
     * </p>
     */
    @Test
    public void test20171231014357187520 ()
    {
        System.out.println("Test: 20171231014357187520");

        try
        {
            final CascadeSchema cs = new ConcreteSchema("schema");
            cs.enter("planets");
            cs.addDynamicPool("Mercury").makeGlobalDefault();
            cs.addReactor("Earth").withCore(Utils.nop()).usingPool("Mercury").usingPump("Venus");
            cs.build();
            fail();
        }
        catch (RuntimeException ex)
        {
            assertTrue(ex.getMessage().toLowerCase().contains("no pumps were specified"));
        }
    }

    /**
     * Test: 20171231014357187546
     *
     * <p>
     * Case: Default Pump Settings
     * </p>
     */
    @Test
    public void test20171231014357187546 ()
    {
        System.out.println("Test: 20171231014357187546");

        final CascadeSchema cs = new ConcreteSchema("schema");
        cs.enter("planets");
        cs.addDynamicPool("Mercury").makeGlobalDefault();
        cs.addPump("Venus");
        cs.addReactor("Earth").withCore(Utils.nop()).usingPool("Mercury").usingPump("Venus");

        final Cascade cas = cs.build();

        final ConcretePump pump = (ConcretePump) cas.pumps().get(CascadeToken.token("planets.Venus"));
        assertEquals(1, pump.threads().size());
        final Thread thread = ImmutableList.copyOf(pump.threads()).get(0);
        assertFalse(thread.isDaemon());
        assertFalse(thread.isAlive());
    }

    /**
     * Test: 20171231014357187571
     *
     * <p>
     * Case: Customized Pump Settings
     * </p>
     */
    @Test
    public void test20171231014357187571 ()
    {
        System.out.println("Test: 20171231014357187571");

        final ThreadFactory factory = new ThreadFactoryBuilder().setDaemon(true).build();

        final CascadeSchema cs = new ConcreteSchema("schema");
        cs.enter("planets");
        cs.addDynamicPool("Mercury").makeGlobalDefault();
        cs.addPump("Venus").withThreadCount(3).usingThreadFactory(factory);
        cs.addReactor("Earth").withCore(Utils.nop()).usingPool("Mercury").usingPump("Venus");

        final Cascade cas = cs.build();

        final ConcretePump pump = (ConcretePump) cas.pumps().get(CascadeToken.token("planets.Venus"));
        assertEquals(3, pump.threads().size());
        final Thread thread = ImmutableList.copyOf(pump.threads()).get(0);
        assertTrue(thread.isDaemon()); // The default would be false, so the thread-factory was used.
        assertFalse(thread.isAlive());
    }

    /**
     * Test: 20171231014814504344
     *
     * <p>
     * Case: No Reactors, which is acceptable.
     * </p>
     */
    @Test
    public void test20171231014814504344 ()
    {
        System.out.println("Test: 20171231014814504344");

        final CascadeSchema cs = new ConcreteSchema("schema");
        cs.enter("planets");
        cs.addDynamicPool("Mercury").makeGlobalDefault();
        cs.addPump("Venus");

        final Cascade cas = cs.build();

        assertTrue(cas.reactors().isEmpty());
    }

    /**
     * Test: 20171231014814504425
     *
     * <p>
     * Case: Default Reactor Settings
     * </p>
     */
    @Test
    public void test20171231014814504425 ()
    {
        System.out.println("Test: 20171231014814504425");

        final CascadeSchema cs = new ConcreteSchema("schema");
        cs.enter("planets");
        cs.addDynamicPool("Mercury").makeGlobalDefault();
        cs.addPump("Venus");

        /**
         * Create the reactor. Some values (core, pool, pump) do not have defaults.
         */
        cs.addReactor("Earth")
                .withCore(Utils.nop())
                .usingPool("Mercury")
                .usingPump("Venus");

        final Cascade cas = cs.build();

        assertEquals(1, cas.reactors().size());
        final ConcreteReactor reactor = (ConcreteReactor) ImmutableList.copyOf(cas.reactors().values()).get(0);
        assertTrue(reactor.input() instanceof LinkedInflowQueue);
        final InflowQueue queue = reactor.input();
        assertEquals(Integer.MAX_VALUE, queue.capacity());
        assertEquals("planets.Mercury", reactor.pool().name().name());
        assertEquals("planets.Venus", reactor.pump().name().name());
        assertTrue(reactor.logger() instanceof StandardLogger);
        assertEquals("planets.Earth", reactor.logger().site().name());
    }

    /**
     * Test: 20171231014814504458
     *
     * <p>
     * Case: Customized Reactor Settings
     * </p>
     */
    @Test
    public void test20171231014814504458 ()
    {
        System.out.println("Test: 20171231014814504458");

        final CascadeSchema cs = new ConcreteSchema("schema");
        cs.enter("planets");
        cs.addDynamicPool("Mercury").makeGlobalDefault();
        cs.addPump("Venus");
        cs.addReactor("Earth")
                .withCore(Utils.nop())
                .withArrayQueue(117)
                .usingPool("Mercury")
                .usingPump("Venus")
                .usingLogger(x -> new DevNullLogger(x.append("X")))
                .subscribeTo("Eclipse")
                .subscribeTo("Solstice")
                .subscribeTo("MoonImpact");

        final Cascade cas = cs.build();

        assertEquals(1, cas.reactors().size());
        final ConcreteReactor reactor = (ConcreteReactor) ImmutableList.copyOf(cas.reactors().values()).get(0);
        assertTrue(reactor.input() instanceof ArrayInflowQueue);
        final InflowQueue queue = reactor.input();
        assertEquals(117, queue.capacity());
        assertEquals("planets.Mercury", reactor.pool().name().name());
        assertEquals("planets.Venus", reactor.pump().name().name());
        assertTrue(reactor.subscriptions().contains(CascadeToken.token("Eclipse")));
        assertTrue(reactor.subscriptions().contains(CascadeToken.token("Solstice")));
        assertTrue(reactor.subscriptions().contains(CascadeToken.token("MoonImpact")));

        /**
         * Since the name of the logger has an 'X' appended to it,
         * the custom logger-factory must have created it,
         * because the default logger-factory would not
         * have appended an 'X' onto the name.
         */
        assertEquals("planets.Earth.X", reactor.logger().site().name());
    }

    /**
     * Test: 20171231014814504491
     *
     * <p>
     * Case: Inherited Reactor Settings
     * </p>
     */
    @Test
    public void test20171231014814504491 ()
    {
        System.out.println("Test: 20171231014814504491");

        final CascadeSchema cs = new ConcreteSchema("schema");
        cs.enter("planets");
        cs.addDynamicPool("Mercury").makeGlobalDefault();
        cs.addDynamicPool("Mars").makeGlobalDefault();
        cs.addPump("Venus");
        cs.addPump("Neptune");

        /**
         * These are the settings that will be overridden.
         */
        cs.usingArrayQueues(117);
        cs.usingPool("Mars");
        cs.usingPump("Neptune");
        cs.usingLogger(x -> new DevNullLogger(x.append("X")));

        /**
         * Inherit the settings.
         */
        cs.addReactor("Earth").withCore(Utils.nop());

        final Cascade cas = cs.build();

        /**
         * Verify that the settings were in-fact inherited.
         */
        assertEquals(1, cas.reactors().size());
        final ConcreteReactor reactor = (ConcreteReactor) ImmutableList.copyOf(cas.reactors().values()).get(0);
        assertEquals("planets.Mars", reactor.pool().name().name());
        assertEquals("planets.Neptune", reactor.pump().name().name());
        assertEquals("planets.Earth", reactor.name().name());

        /**
         * By default, this would have been a linked-queue of int-max size.
         */
        assertTrue(reactor.input() instanceof ArrayInflowQueue);
        final InflowQueue queue = reactor.input();
        assertEquals(117, queue.capacity());

        /**
         * Since the name of the logger has an 'X' appended to it,
         * the custom logger-factory must have created it,
         * because the default logger-factory would not
         * have appended an 'X' onto the name.
         */
        assertEquals("planets.Earth.X", reactor.logger().site().name());
    }

    /**
     * Test: 20171231014814504524
     *
     * <p>
     * Case: Override Reactor Settings
     * </p>
     */
    @Test
    public void test20171231014814504524 ()
    {
        System.out.println("Test: 20171231014814504524");

        final CascadeSchema cs = new ConcreteSchema("schema");
        cs.enter("planets");
        cs.addDynamicPool("Mercury").makeGlobalDefault();
        cs.addDynamicPool("Mars").makeGlobalDefault();
        cs.addPump("Venus");
        cs.addPump("Neptune");

        /**
         * These are the settings that will be overridden.
         */
        cs.usingArrayQueues(117);
        cs.usingPool("Mars");
        cs.usingPump("Neptune");
        cs.usingLogger(x -> new DevNullLogger(x.append("X")));

        /**
         * Override the settings.
         */
        cs.addReactor("Earth")
                .withCore(Utils.nop())
                .withArrayQueue(798)
                .usingPool("Mercury")
                .usingPump("Venus")
                .usingLogger(x -> new DevNullLogger(x.append("Z")));

        final Cascade cas = cs.build();

        /**
         * Verify that the settings were in-fact overridden.
         */
        assertEquals(1, cas.reactors().size());
        final ConcreteReactor reactor = (ConcreteReactor) ImmutableList.copyOf(cas.reactors().values()).get(0);
        assertTrue(reactor.input() instanceof ArrayInflowQueue);
        final InflowQueue queue = reactor.input();
        assertEquals(798, queue.capacity()); // Notice the size versus the overridden one.
        assertEquals("planets.Mercury", reactor.pool().name().name());
        assertEquals("planets.Venus", reactor.pump().name().name());

        /**
         * Since the name of the logger has an 'Z' appended to it,
         * the overriding custom logger-factory must have created it,
         * because the default logger-factory would not have appended
         * an 'Z' onto the name and the custom "using" logger-factory
         * would have appended an 'X' instead.
         */
        assertEquals("planets.Earth.Z", reactor.logger().site().name());
    }

    /**
     * Test: 20171231030255026121
     *
     * <p>
     * Case: Initial Subscriptions
     * </p>
     */
    @Test
    public void test20171231030255026121 ()
    {
        System.out.println("Test: 20171231030255026121");

        final CoreBuilder builder = () -> new Core()
        {
            @Override
            public Set<CascadeToken> initialSubscriptions ()
            {
                return ImmutableSet.of(CascadeToken.token("AsteroidImpact"));
            }
        };

        final CascadeSchema cs = new ConcreteSchema("schema");
        cs.enter("planets");
        cs.addDynamicPool("Mercury").makeGlobalDefault();
        cs.addPump("Venus");
        cs.addReactor("Earth")
                .withCore(builder)
                .usingPool("Mercury")
                .usingPump("Venus")
                .subscribeTo("Eclipse")
                .subscribeTo("Solstice")
                .subscribeTo("MoonImpact");

        final Cascade cas = cs.build();

        assertEquals(1, cas.reactors().size());
        final ConcreteReactor reactor = (ConcreteReactor) ImmutableList.copyOf(cas.reactors().values()).get(0);
        assertEquals("planets.Mercury", reactor.pool().name().name());
        assertEquals("planets.Venus", reactor.pump().name().name());
        assertTrue(reactor.subscriptions().contains(CascadeToken.token("AsteroidImpact")));
        assertTrue(reactor.subscriptions().contains(CascadeToken.token("Eclipse")));
        assertTrue(reactor.subscriptions().contains(CascadeToken.token("Solstice")));
        assertTrue(reactor.subscriptions().contains(CascadeToken.token("MoonImpact")));

        reactor.subscribe(CascadeToken.token("RocketLaunch"));
        assertTrue(reactor.subscriptions().contains(CascadeToken.token("RocketLaunch")));
    }

    /**
     * Test: 20171231030325749249
     *
     * <p>
     * Case: Add Core via Builder.
     * </p>
     */
    @Test
    public void test20171231030325749249 ()
    {
        System.out.println("Test: 20171231030325749249");

        final CascadeReactor.CoreBuilder builder = () -> new CascadeReactor.Core()
        {
            // Pass
        };

        final CascadeSchema cs = new ConcreteSchema("schema");
        cs.enter("planets");
        cs.addDynamicPool("Mercury").makeGlobalDefault();
        cs.addPump("Venus");
        cs.addReactor("Earth").withCore(builder).usingPool("Mercury").usingPump("Venus");

        final Cascade cas = cs.build();

        assertEquals(1, cas.reactors().size());
        final ConcreteReactor reactor = (ConcreteReactor) ImmutableList.copyOf(cas.reactors().values()).get(0);

        /**
         * Since the builder (above) creates an instance of an anonymous class,
         * then we know that the Class object is quite unique; therefore,
         * we can use it to verify that the builder was actually used.
         */
        assertEquals(builder.build().getClass(), reactor.core().getClass());
    }

    /**
     * Test: 20171231042414682082
     *
     * <p>
     * Case: Different Queue Types (Inherited)
     * </p>
     */
    @Test
    public void test20171231042414682082 ()
    {
        System.out.println("Test: 20171231042414682082");

        final CascadeSchema cs = new ConcreteSchema("schema");
        cs.enter("planets");
        cs.addDynamicPool("Mercury").makeGlobalDefault();
        cs.addPump("Venus");

        /**
         * Create a reactor with an array-based queue.
         */
        cs.usingArrayQueues(203);
        cs.addReactor("Earth")
                .withCore(Utils.nop())
                .usingPool("Mercury")
                .usingPump("Venus");

        /**
         * Create a reactor with a linked-based queue.
         */
        cs.usingLinkedQueues(307);
        cs.addReactor("Mars")
                .withCore(Utils.nop())
                .usingPool("Mercury")
                .usingPump("Venus");

        final Cascade cas = cs.build();

        assertEquals(2, cas.reactors().size());
        final ConcreteReactor earth = (ConcreteReactor) cas.reactors().get(CascadeToken.token("planets.Earth"));
        final ConcreteReactor mars = (ConcreteReactor) cas.reactors().get(CascadeToken.token("planets.Mars"));

        assertTrue(earth.input() instanceof ArrayInflowQueue);
        assertTrue(mars.input() instanceof LinkedInflowQueue);

        assertEquals(203, earth.queueCapacity());
        assertEquals(307, mars.queueCapacity());
    }

    /**
     * Test: 20171231042414682162
     *
     * <p>
     * Case: Different Queue Types (Explicit)
     * </p>
     */
    @Test
    public void test20171231042414682162 ()
    {
        System.out.println("Test: 20171231042414682162");

        final CascadeSchema cs = new ConcreteSchema("schema");
        cs.enter("planets");
        cs.addDynamicPool("Mercury").makeGlobalDefault();
        cs.addPump("Venus");

        /**
         * Create a reactor with an array-based queue.
         */
        cs.addReactor("Earth")
                .withCore(Utils.nop())
                .usingPool("Mercury")
                .usingPump("Venus")
                .withArrayQueue(517);

        /**
         * Create a reactor with a linked-based queue.
         */
        cs.addReactor("Mars")
                .withCore(Utils.nop())
                .usingPool("Mercury")
                .usingPump("Venus")
                .withLinkedQueue(623);

        final Cascade cas = cs.build();

        assertEquals(2, cas.reactors().size());
        final ConcreteReactor earth = (ConcreteReactor) cas.reactors().get(CascadeToken.token("planets.Earth"));
        final ConcreteReactor mars = (ConcreteReactor) cas.reactors().get(CascadeToken.token("planets.Mars"));

        assertTrue(earth.input() instanceof ArrayInflowQueue);
        assertTrue(mars.input() instanceof LinkedInflowQueue);

        assertEquals(517, earth.queueCapacity());
        assertEquals(623, mars.queueCapacity());
    }

    /**
     * Test: 20171231060142521411
     *
     * <p>
     * Case: build() was already called.
     * </p>
     */
    @Test
    public void test20171231060142521411 ()
    {
        System.out.println("Test: 20171231060142521411");

        try
        {
            final CascadeSchema cs = new ConcreteSchema("schema");
            cs.enter("planets");
            cs.addDynamicPool("Mercury").makeGlobalDefault();
            cs.addPump("Venus");
            cs.addReactor("Earth").withCore(Utils.nop()).usingPool("Mercury").usingPump("Venus");
            cs.build();
            cs.build();
            fail();
        }
        catch (RuntimeException ex)
        {
            assertTrue(ex.getMessage().toLowerCase().contains("already built"));
        }
    }
}
