package com.mackenziehigh.cascade.old.internal;

import com.mackenziehigh.cascade.old.CascadeReactor;
import com.mackenziehigh.cascade.old.CascadeReactor.Context;
import com.mackenziehigh.cascade.old.CascadeReactor.Core;
import static junit.framework.Assert.fail;
import org.junit.Test;

/**
 * Unit Test.
 */
public final class ConcreteCascadeTest
{
    @Test
    public void testLifeCycle ()
    {
        final StringBuffer buffer = new StringBuffer();

        final Core core1 = new CascadeReactor.Core()
        {
            @Override
            public void onSetup (final Context context)
                    throws Throwable
            {
                buffer.append('A');
            }

            @Override
            public void onStart (final Context context)
                    throws Throwable
            {
                buffer.append('B');
            }

            @Override
            public void onMessage (final Context context)
                    throws Throwable
            {
                buffer.append('C');
            }

            @Override
            public void onStop (final Context context)
                    throws Throwable
            {
                buffer.append('D');
            }

            @Override
            public boolean isDestroyable ()
                    throws Throwable
            {
                buffer.append('E');
                return true;
            }

            @Override
            public void onDestroy (final Context context)
                    throws Throwable
            {
                buffer.append('F');
            }

            @Override
            public void onException (final Context context)
                    throws Throwable
            {
                buffer.append('G');
            }
        };

        final Core core2 = new CascadeReactor.Core()
        {
            @Override
            public void onSetup (final Context context)
                    throws Throwable
            {
                buffer.append('a');
            }

            @Override
            public void onStart (final Context context)
                    throws Throwable
            {
                buffer.append('b');
            }

            @Override
            public void onMessage (final Context context)
                    throws Throwable
            {
                buffer.append('c');
            }

            @Override
            public void onStop (final Context context)
                    throws Throwable
            {
                buffer.append('d');
            }

            @Override
            public boolean isDestroyable ()
                    throws Throwable
            {
                buffer.append('e');
                return true;
            }

            @Override
            public void onDestroy (final Context context)
                    throws Throwable
            {
                buffer.append('f');
            }

            @Override
            public void onException (final Context context)
                    throws Throwable
            {
                buffer.append('g');
            }
        };

//        final CascadeSchema schema = CascadeSchema.createSimple();
//        schema.addReactor().named("reactor1").withCore(core1).withLinkedQueue(16);
//        final Cascade cascade = schema.build();
        fail();
    }
}
