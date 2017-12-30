package com.mackenziehigh.cascade.internal;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeSchema;
import com.mackenziehigh.cascade.CascadeToken;
import static junit.framework.Assert.*;
import org.junit.Test;

/**
 * Unit Test.
 */
public final class ConcreteSchemaTest
{

    @Test
    public void test ()
    {
        CascadeSchema cs;
        Cascade cas;

        final CascadeToken Pool1 = CascadeToken.create("pool1");
        final CascadeToken Pool2 = CascadeToken.create("pool2");

        /**
         * Case: Dynamic Allocator.
         */
        cs = new ConcreteSchema("schema1");
        cs.addDynamicPool("pool1").withMinimumSize(100).withMaximumSize(300);
        cs.addDynamicPool("pool2").withMinimumSize(200).withMaximumSize(400);
        cas = cs.build();
        assertEquals(Pool1, cas.allocator().pools().get(Pool1).name());
        assertEquals(Pool2, cas.allocator().pools().get(Pool2).name());
        assertFalse(cas.allocator().pools().get(Pool1).isFixed());
        assertFalse(cas.allocator().pools().get(Pool2).isFixed());
        assertFalse(cas.allocator().pools().get(Pool1).size().isPresent());
        assertFalse(cas.allocator().pools().get(Pool2).size().isPresent());
        assertFalse(cas.allocator().pools().get(Pool1).capacity().isPresent());
        assertFalse(cas.allocator().pools().get(Pool2).capacity().isPresent());
        assertEquals(100, cas.allocator().pools().get(Pool1).minimumAllocationSize());
        assertEquals(300, cas.allocator().pools().get(Pool1).maximumAllocationSize());
        assertEquals(200, cas.allocator().pools().get(Pool2).minimumAllocationSize());
        assertEquals(400, cas.allocator().pools().get(Pool2).maximumAllocationSize());

        /**
         * Case: Fixed Allocator.
         */
        cs = new ConcreteSchema("schema1");
        cs.addFixedPool("pool1").withMinimumSize(100).withMaximumSize(300).withBufferCount(33);
        cs.addFixedPool("pool2").withMinimumSize(200).withMaximumSize(400).withBufferCount(47);
        cas = cs.build();
        assertEquals(Pool1, cas.allocator().pools().get(Pool1).name());
        assertEquals(Pool2, cas.allocator().pools().get(Pool2).name());
        assertTrue(cas.allocator().pools().get(Pool1).isFixed());
        assertTrue(cas.allocator().pools().get(Pool2).isFixed());
        assertTrue(cas.allocator().pools().get(Pool1).size().isPresent());
        assertTrue(cas.allocator().pools().get(Pool2).size().isPresent());
        assertTrue(cas.allocator().pools().get(Pool1).capacity().isPresent());
        assertTrue(cas.allocator().pools().get(Pool2).capacity().isPresent());
        assertEquals(100, cas.allocator().pools().get(Pool1).minimumAllocationSize());
        assertEquals(300, cas.allocator().pools().get(Pool1).maximumAllocationSize());
        assertEquals(200, cas.allocator().pools().get(Pool2).minimumAllocationSize());
        assertEquals(400, cas.allocator().pools().get(Pool2).maximumAllocationSize());
        assertEquals(33, cas.allocator().pools().get(Pool1).capacity().getAsLong());
        assertEquals(47, cas.allocator().pools().get(Pool2).capacity().getAsLong());

        /**
         * Case: Pump without any reactors.
         */
        cs = new ConcreteSchema("schema1");
        cs.addPump("pump1").withThreadCount(13);
        cas = cs.build();
        assertEquals("pump1", cas.pumps().get(CascadeToken.create("pump1")).name().toString());
        assertEquals(13, cas.pumps().get(CascadeToken.create("pump1")).threads().size());
        assertTrue(cas.pumps().get(CascadeToken.create("pump1")).reactors().isEmpty());

        /**
         * Case: Pump with reactors.
         */
        cs = new ConcreteSchema("schema1");
        cs.addDynamicPool("pool1").withMinimumSize(0).withMaximumSize(128);
        cs.addPump("pump1").withThreadCount(17);
        cs.usingPool("pool1");
        cs.usingPump("pump1");
        cs.addReactor("reactor1").withArrayQueue(8).withCore(Utils.nop());
        cs.addReactor("reactor2").withArrayQueue(8).withCore(Utils.nop());
        cas = cs.build();
        assertEquals("pump1", cas.pumps().get(CascadeToken.create("pump1")).name().toString());
        assertEquals(1, cas.pumps().size());
        assertEquals(17, cas.pumps().get(CascadeToken.create("pump1")).threads().size());
        assertEquals(2, cas.pumps().get(CascadeToken.create("pump1")).reactors().size());
        assertTrue(cas.reactors().containsKey(CascadeToken.create("reactor1")));
        assertTrue(cas.reactors().containsKey(CascadeToken.create("reactor2")));

        /**
         * Case: Pump with non-default thread-factory.
         */
        cs = new ConcreteSchema("schema1");
        cs.addPump("pump1").withThreadCount(1).usingThreadFactory(new ThreadFactoryBuilder().setNameFormat("Vulcan").build());
        cas = cs.build();
        assertEquals(1, cas.pumps().size());
        assertEquals(1, cas.pumps().get(CascadeToken.create("pump1")).threads().size());
        assertEquals("Vulcan", Lists.newArrayList(cas.pumps().get(CascadeToken.create("pump1")).threads()).get(0).getName());

        /**
         * Case: Reactors.
         */
        cs = new ConcreteSchema("schema1");
        cs.addDynamicPool("pool1").withMinimumSize(0).withMaximumSize(128);
        cs.addPump("pump1").withThreadCount(1);
        cs.addPump("pump2").withThreadCount(1);
        cs.usingPool("pool1");
        cs.addReactor("reactor1").withArrayQueue(8).usingPump("pump1").withCore(Utils.nop());
        cs.addReactor("reactor2").withArrayQueue(8).usingPump("pump2").withCore(Utils.nop());
        cas = cs.build();
        assertEquals(2, cas.reactors().size());
        assertEquals("pump1", cas.reactors().get(CascadeToken.create("reactor1")).pump().name().toString());
        assertEquals("pump2", cas.reactors().get(CascadeToken.create("reactor2")).pump().name().toString());
    }
}
