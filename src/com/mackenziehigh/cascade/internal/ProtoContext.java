package com.mackenziehigh.cascade.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.CascadeAllocator.AllocationPool;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import com.mackenziehigh.cascade.CascadeEdge;
import com.mackenziehigh.cascade.CascadeLogger;
import com.mackenziehigh.cascade.CascadeNode;
import com.mackenziehigh.cascade.CascadeNode.SendFailureException;
import com.mackenziehigh.cascade.CascadePump;
import com.mackenziehigh.cascade.internal.pumps3.OrderlyAtomicSender;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public final class ProtoContext
        implements CascadeNode.Context
{
    private final String name;

    private final CascadeNode node;

    private final Kernel kernel;

    private final LazyRef<OrderlyAtomicSender> transmitter;

    private final LazyRef<CascadeLogger> logger;

    private final LazyRef<CascadeAllocator> allocator;

    private final LazyRef<AllocationPool> pool;

    private final LazyRef<CascadePump> pump;

    private final LazyRef<List<CascadeEdge>> inputs;

    private final LazyRef<List<CascadeEdge>> outputs;

    public ProtoContext (final String name,
                         final CascadeNode node,
                         final Kernel kernel)
    {
        this.name = Objects.requireNonNull(name);
        this.node = Objects.requireNonNull(node);
        this.kernel = Objects.requireNonNull(kernel);
        this.transmitter = LazyRef.create(() -> new OrderlyAtomicSender(kernel.actorsToOutputConnections.get(node)));
        this.logger = LazyRef.create(() -> kernel.actorsToLoggers.get(node));
        this.allocator = LazyRef.create(() -> kernel.allocator);
        this.pool = LazyRef.create(() -> kernel.actorsToPools.get(node));
        this.pump = LazyRef.create(() -> kernel.actorsToPumps.get(node));
        this.inputs = LazyRef.create(() -> resolveInputs());
        this.outputs = LazyRef.create(() -> resolveOutputs());
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
    public CascadeLogger logger ()
    {
        return logger.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeAllocator allocator ()
    {
        return allocator.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeAllocator.AllocationPool pool ()
    {
        return pool.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadePump pump ()
    {
        return pump.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CascadeNode node ()
    {
        return node;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name ()
    {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CascadeEdge> inputs ()
    {
        return inputs.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CascadeEdge> outputs ()
    {
        return outputs.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OperandStack message ()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Throwable exception ()
    {
        return null;
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
    public boolean sync (final OperandStack message,
                         final long timeout,
                         final TimeUnit timeoutUnits)
    {
        try
        {
            return transmitter.get().sendSync(message, timeout, timeoutUnits);
        }
        catch (InterruptedException ex)
        {
            return false; // TODO: throw????
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

        throw new SendFailureException(node());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int broadcast (final OperandStack message)
    {
        return transmitter.get().broadcast(message);
    }

    private List<CascadeEdge> resolveInputs ()
    {
        final SortedMap<String, CascadeEdge> map = Maps.newTreeMap(String.CASE_INSENSITIVE_ORDER);

        for (CascadeEdge x : kernel.actorsToInputs.get(node()))
        {
            map.put(x.supplier().name(), x);
        }

        final List<CascadeEdge> edges = ImmutableList.copyOf(map.values());

        return edges;
    }

    private List<CascadeEdge> resolveOutputs ()
    {
        final SortedMap<String, CascadeEdge> map = Maps.newTreeMap(String.CASE_INSENSITIVE_ORDER);

        for (CascadeEdge x : kernel.actorsToOutputs.get(node()))
        {
            map.put(x.supplier().name(), x);
        }

        final List<CascadeEdge> edges = ImmutableList.copyOf(map.values());

        return edges;
    }

}
