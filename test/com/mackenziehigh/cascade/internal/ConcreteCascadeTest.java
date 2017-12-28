package com.mackenziehigh.cascade.internal;

import com.mackenziehigh.cascade.CascadeReactor;
import com.mackenziehigh.cascade.CascadeReactor.Core;
import org.junit.Test;

/**
 * Unit Test.
 */
public final class ConcreteCascadeTest
{
    @Test
    public void testLifeCycle ()
    {
        final Core chernobyl = new CascadeReactor.Core()
        {
            @Override
            public void onException (CascadeReactor.Context context)
                    throws Throwable
            {
                Core.super.onException(context);
            }

            @Override
            public void onDestroy (CascadeReactor.Context context)
                    throws Throwable
            {
                Core.super.onDestroy(context);
            }

            @Override
            public boolean isDestroyable ()
                    throws Throwable
            {
                return Core.super.isDestroyable();
            }

            @Override
            public void onStop (CascadeReactor.Context context)
                    throws Throwable
            {
                Core.super.onStop(context);
            }

            @Override
            public void onMessage (CascadeReactor.Context context)
                    throws Throwable
            {
                Core.super.onMessage(context);
            }

            @Override
            public void onStart (CascadeReactor.Context context)
                    throws Throwable
            {
                Core.super.onStart(context);
            }

            @Override
            public void onSetup (CascadeReactor.Context context)
                    throws Throwable
            {
                Core.super.onSetup(context);
            }

        };

    }
}
