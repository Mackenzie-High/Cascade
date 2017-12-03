package com.mackenziehigh.cascade.internal;

import com.google.common.collect.ImmutableList;
import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import com.mackenziehigh.cascade.CascadeEdge;
import com.mackenziehigh.cascade.CascadeNode;
import com.mackenziehigh.cascade.CascadeNode.SendFailureException;
import com.mackenziehigh.cascade.internal.pumps3.Connector.Connection;
import com.mackenziehigh.cascade.internal.pumps3.OrderlyAtomicSender;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public final class ConcreteEdge
        implements CascadeEdge
{
    private final Kernel kernel;

    private final LazyRef<CascadeNode> supplier;

    private final LazyRef<CascadeNode> consumer;

    private final LazyRef<Connection> connection;

    private final LazyRef<OrderlyAtomicSender> transmitter;

    public ConcreteEdge (final Kernel kernel,
                         final String supplierName,
                         final String consumerName)
    {
        this.kernel = Objects.requireNonNull(kernel);
        this.supplier = LazyRef.create(() -> kernel.namesToNodes.get(supplierName));
        this.consumer = LazyRef.create(() -> kernel.namesToNodes.get(consumerName));
        this.connection = LazyRef.create(() -> kernel.nodesToConnections.get(this));
        this.transmitter = LazyRef.create(() -> new OrderlyAtomicSender(ImmutableList.of(connection.get())));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cascade cascade ()
    {
        return Objects.requireNonNull(kernel.cascade);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeNode supplier ()
    {
        return supplier.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeNode consumer ()
    {
        return consumer.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int backlogSize ()
    {
        return connection.get().parent().globalSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int backlogCapacity ()
    {
        return connection.get().parent().globalCapacity();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int queueSize ()
    {
        return connection.get().localSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int queueCapacity ()
    {
        return connection.get().localCapacity();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean async (final OperandStack message)
    {
        return transmitter.get().sendAsync(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean sync (final CascadeAllocator.OperandStack message,
                         final long timeout,
                         final TimeUnit timeoutUnits)
    {
        try
        {
            return transmitter.get().sendSync(message, timeout, timeoutUnits);
        }
        catch (InterruptedException ex)
        {
            return false; // TODO: throw instead?
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send (final OperandStack message)
            throws SendFailureException
    {
        while (kernel.stop.get() == false)
        {
            if (sync(message, 1, TimeUnit.SECONDS))
            {
                return;
            }
        }

        throw new SendFailureException(supplier());
    }
}
