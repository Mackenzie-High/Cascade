package com.mackenziehigh.cascade.internal.schema;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.CascadeAllocator;
import com.mackenziehigh.cascade.CascadeLogger;
import com.mackenziehigh.cascade.CascadePump;
import com.mackenziehigh.cascade.CascadeReactor;
import com.mackenziehigh.cascade.CascadeSchema;
import com.mackenziehigh.cascade.CascadeToken;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ThreadFactory;

/**
 *
 */
public final class ConcreteSchema
        implements CascadeSchema
{
    private CascadeToken name;

    private Scope scope = new Scope();

    private final Set<DynamicPoolSchemaImp> dynamicPools = Sets.newHashSet();

    private final Set<FixedPoolSchemaImp> fixedPools = Sets.newHashSet();

    private final Set<CompositePoolSchemaImp> compositePools = Sets.newHashSet();

    private final Set<PumpSchemaImp> pumps = Sets.newHashSet();

    private final Set<ReactorSchemaImp> reactors = Sets.newHashSet();

    @Override
    public CascadeSchema named (final String name)
    {
        Preconditions.checkState(name == null, "Already Named");
        this.name = CascadeToken.create(name);
        return this;
    }

    @Override
    public CascadeSchema begin (final String name)
    {
        final CascadeToken token = CascadeToken.create(name);
        this.scope = new Scope();
        this.scope.below = new Scope();
        this.scope.namespace = token;

        return this;
    }

    @Override
    public CascadeSchema end ()
    {
        if (scope.below == null)
        {
            throw new IllegalStateException("At Bottom Scope");
        }
        else
        {
            scope = scope.below;
            return this;
        }
    }

    @Override
    public CascadeSchema usingLogger (final CascadeLogger.Factory factory)
    {
        scope.loggerFactory = factory;
        scope.logger = null;
        return this;
    }

    @Override
    public CascadeSchema usingLogger (final CascadeLogger logger)
    {
        scope.loggerFactory = null;
        scope.logger = logger;
        return this;
    }

    @Override
    public CascadeSchema usingPool (final String name)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CascadeSchema usingPump (String name)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DynamicPoolSchema addDynamicPool ()
    {
        final DynamicPoolSchemaImp result = new DynamicPoolSchemaImp();
        dynamicPools.add(result);
        return result;
    }

    @Override
    public FixedPoolSchema addFixedPool ()
    {
        final FixedPoolSchemaImp result = new FixedPoolSchemaImp();
        fixedPools.add(result);
        return result;
    }

    @Override
    public CompositePoolSchema addCompositePool ()
    {
        final CompositePoolSchemaImp result = new CompositePoolSchemaImp();
        compositePools.add(result);
        return result;
    }

    @Override
    public PumpSchema addPump ()
    {
        final PumpSchemaImp result = new PumpSchemaImp();
        pumps.add(result);
        return result;
    }

    @Override
    public ReactorSchema addReactor ()
    {
        final ReactorSchemaImp result = new ReactorSchemaImp();
        reactors.add(result);
        return result;
    }

    @Override
    public Cascade build ()
    {
        /**
         * Declare.
         */
        dynamicPools.forEach(x -> declare(x));
        fixedPools.forEach(x -> declare(x));
        compositePools.forEach(x -> declare(x));
        pumps.forEach(x -> declare(x));
        reactors.forEach(x -> declare(x));

        /**
         * Validate.
         */
        dynamicPools.forEach(x -> validate(x));
        fixedPools.forEach(x -> validate(x));
        compositePools.forEach(x -> validate(x));
        pumps.forEach(x -> validate(x));
        reactors.forEach(x -> validate(x));

        /**
         * Compile.
         */
        dynamicPools.forEach(x -> compile(x));
        fixedPools.forEach(x -> compile(x));
        compositePools.forEach(x -> compile(x));
        pumps.forEach(x -> compile(x));
        reactors.forEach(x -> compile(x));

        return null;
    }

    private void declare (final DynamicPoolSchemaImp object)
    {

    }

    private void declare (final FixedPoolSchemaImp object)
    {

    }

    private void declare (final CompositePoolSchemaImp object)
    {

    }

    private void declare (final PumpSchemaImp object)
    {

    }

    private void declare (final ReactorSchemaImp object)
    {

    }

    private void validate (final DynamicPoolSchemaImp object)
    {

    }

    private void validate (final FixedPoolSchemaImp object)
    {

    }

    private void validate (final CompositePoolSchemaImp object)
    {

    }

    private void validate (final PumpSchemaImp object)
    {

    }

    private void validate (final ReactorSchemaImp object)
    {

    }

    private void compile (final DynamicPoolSchemaImp object)
    {

    }

    private void compile (final FixedPoolSchemaImp object)
    {

    }

    private void compile (final CompositePoolSchemaImp object)
    {

    }

    private void compile (final PumpSchemaImp object)
    {

    }

    private void compile (final ReactorSchemaImp object)
    {

    }

    private CascadeToken convertName (final String name)
    {
        return CascadeToken.create(name);
    }

    private void preventChange (String entity,
                                Object original,
                                Object value)
    {
        if (original != null)
        {
            throw new IllegalStateException(String.format("Redefinition of %s (%s, %s)",
                                                          entity,
                                                          String.valueOf(original),
                                                          String.valueOf(value)));
        }
    }

    private final class DynamicPoolSchemaImp
            implements DynamicPoolSchema
    {
        private CascadeToken name;

        private Integer minimumSize;

        private Integer maximumSize;

        @Override
        public DynamicPoolSchema named (final String name)
        {
            preventChange("Pool Name", this.name, name);
            this.name = convertName(name);
            return this;
        }

        @Override
        public DynamicPoolSchema withMinimumSize (final int bound)
        {
            preventChange("Minimum Size", this.minimumSize, bound);
            this.minimumSize = bound;
            return this;
        }

        @Override
        public DynamicPoolSchema withMaximumSize (int bound)
        {
            preventChange("Maximum Size", this.maximumSize, bound);
            this.maximumSize = bound;
            return this;
        }
    };

    private final class FixedPoolSchemaImp
            implements FixedPoolSchema
    {
        private CascadeToken name;

        private Integer minimumSize;

        private Integer maximumSize;

        private Integer bufferCount;

        @Override
        public FixedPoolSchema named (final String name)
        {
            preventChange("Pool Name", this.name, name);
            this.name = convertName(name);
            return this;
        }

        @Override
        public FixedPoolSchema withMinimumSize (final int bound)
        {
            preventChange("Minimum Size", this.minimumSize, bound);
            this.minimumSize = bound;
            return this;
        }

        @Override
        public FixedPoolSchema withMaximumSize (final int bound)
        {
            preventChange("Maximum Size", this.maximumSize, bound);
            this.maximumSize = bound;
            return this;
        }

        @Override
        public FixedPoolSchema withBufferCount (final int count)
        {
            preventChange("Buffer Count", this.bufferCount, count);
            this.bufferCount = count;
            return this;
        }

    };

    private final class CompositePoolSchemaImp
            implements CompositePoolSchema
    {
        private CascadeToken name;

        private CascadeToken fallback;

        private final Set<CascadeToken> members = Sets.newHashSet();

        @Override
        public CompositePoolSchema named (final String name)
        {
            preventChange("Pool Name", this.name, name);
            this.name = convertName(name);
            return this;
        }

        @Override
        public CompositePoolSchema withFallbackPool (final String name)
        {
            final CascadeToken token = convertName(name);
            preventChange("Fallback Pool", this.fallback, token);
            return this;
        }

        @Override
        public CompositePoolSchema withMemberPool (final String name)
        {
            final CascadeToken member = convertName(name);
            members.add(member);
            return this;
        }

    };

    private final class PumpSchemaImp
            implements PumpSchema
    {

        private CascadeToken name;

        private ThreadFactory threadFactory;

        private Integer threadCount;

        @Override
        public PumpSchema named (final String name)
        {
            preventChange("Pump Name", this.name, name);
            this.name = convertName(name);
            return this;
        }

        @Override
        public PumpSchema usingThreadFactory (final ThreadFactory factory)
        {
            preventChange("Thread Factory", this.threadFactory, threadFactory);
            this.threadFactory = factory;
            return this;
        }

        @Override
        public PumpSchema withThreadCount (final int count)
        {
            preventChange("Buffer Count", this.threadCount, count);
            this.threadCount = count;
            return this;
        }

    };

    private final class ReactorSchemaImp
            implements ReactorSchema
    {
        private CascadeToken name;

        private final SortedSet<CascadeToken> subscriptions = Sets.newTreeSet();

        @Override
        public ReactorSchema named (final String name)
        {
            preventChange("Reactor Name", this.name, name);
            this.name = convertName(name);
            return this;
        }

        @Override
        public ReactorSchema withCore (final CascadeReactor.Core core)
        {
            subscriptions.addAll(core.initialSubscriptions());
            return this;
        }

        @Override
        public ReactorSchema usingLogger (final CascadeLogger.Factory factory)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ReactorSchema usingLogger (final CascadeLogger logger)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ReactorSchema usingPool (final String name)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ReactorSchema usingPump (final String name)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ReactorSchema withLinearSharedQueue (final String group,
                                                    final int queueCapacity,
                                                    final int backlogCapacity)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ReactorSchema withLinearArrayQueue (final int queueCapacity)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ReactorSchema withLinearLinkedQueue (final int queueCapacity)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ReactorSchema withCircularArrayQueue (final int queueCapacity)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ReactorSchema withCircularLinkedQueue (final int queueCapacity)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ReactorSchema subscribeTo (final String event)
        {
            final CascadeToken token = CascadeToken.create(event);
            subscriptions.add(token);
            return this;
        }

    };

    private final class Scope
    {
        public CascadeToken namespace;

        public CascadeLogger.Factory loggerFactory;

        public CascadeLogger logger;

        public CascadeAllocator.AllocationPool pool;

        public CascadePump pump;

        public CascadeReactor reactor;

        public Scope below;
    }

    public static void main (String[] args)
    {
        final CascadeSchema cs = new ConcreteSchema();

        cs.addDynamicPool().named("pool1").withMinimumSize(128).withMaximumSize(256);
        cs.addFixedPool().named("pool2").withMinimumSize(512).withMaximumSize(768).withBufferCount(10);
        cs.addCompositePool().named("pool3").withMemberPool("pool1").withMemberPool("pool2");

        cs.addPump().named("pump1").withThreadCount(2);

        cs.addReactor().named("clock1").withCore(null).subscribeTo("toggle");
    }
}
