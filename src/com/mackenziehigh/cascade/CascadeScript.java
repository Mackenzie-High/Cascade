package com.mackenziehigh.cascade;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Scripts define how actors behave in-response to events.
 */
public final class CascadeScript
{

    /**
     * Lambda function whose signature is the same as the onSetup() event-handler.
     */
    @FunctionalInterface
    public interface SetupFunction
    {
        public void accept (CascadeContext ctx)
                throws Throwable;
    }

    /**
     * Lambda function whose signature is the same as the onMessage() event-handler.
     */
    @FunctionalInterface
    public interface MessageFunction
    {
        public void accept (CascadeContext ctx,
                            CascadeToken event,
                            CascadeStack stack)
                throws Throwable;
    }

    /**
     * Lambda function whose signature is the same as the onUnhandledException() event-handler.
     */
    @FunctionalInterface
    public interface ExceptionFunction
    {
        public void accept (CascadeContext ctx,
                            Throwable cause)
                throws Throwable;
    }

    /**
     * Lambda function whose signature is the same as the onClose() event-handler.
     */
    @FunctionalInterface
    public interface CloseFunction
    {
        public void accept (CascadeContext ctx)
                throws Throwable;
    }

    private final AtomicBoolean started = new AtomicBoolean();

    private final AtomicBoolean stopped = new AtomicBoolean();

    private volatile List<SetupFunction> setupFunctions = ImmutableList.of();

    private final Map<CascadeToken, List<MessageFunction>> messageFunctions = new ConcurrentHashMap<>(8);

    private volatile List<ExceptionFunction> exceptionFunctions = ImmutableList.of();

    private volatile List<CloseFunction> closeFunctions = ImmutableList.of();

    private final Object eventHandlerLock = new Object();

    /**
     * This lock is used to prevent concurrent modifications of the data-structures herein.
     *
     * <p>
     * This lock is separate from the event-handler lock, because long-running event-handlers
     * shall not prevent the data-structures from being modified concurrently.
     * </p>
     */
    private final Object dataStructureLock = new Object();

    /**
     * This event-handler will be executed when the enclosing actor is setup.
     *
     * @param ctx provides access to the actor itself, etc.
     * @throws Throwable if something horrible happens.
     */
    public void onSetup (final CascadeContext ctx)
            throws Throwable
    {
        if (started.compareAndSet(false, true) == false)
        {
            return; // Already Starting or Started.
        }

        synchronized (eventHandlerLock)
        {
            try
            {
                subscribe(ctx);

                final List<SetupFunction> array = setupFunctions;

                for (int i = 0; i < array.size(); i++)
                {
                    array.get(i).accept(ctx);
                }
            }
            catch (Throwable ex)
            {
                onUnhandledException(ctx, ex);
            }
        }
    }

    /**
     * This event-handler will be executed whenever the enclosing actor
     * dequeues an event-message for processing herein.
     *
     * @param ctx provides access to the actor itself, etc.
     * @param event identifies the event that created the message.
     * @param stack contains the content of the message.
     * @throws Throwable if something horrible happens.
     */
    public void onMessage (final CascadeContext ctx,
                           final CascadeToken event,
                           final CascadeStack stack)
            throws Throwable
    {
        synchronized (eventHandlerLock)
        {
            try
            {
                final List<MessageFunction> array = messageFunctions.getOrDefault(event, Collections.EMPTY_LIST);

                for (int i = 0; i < array.size(); i++)
                {
                    array.get(i).accept(ctx, event, stack);
                }
            }
            catch (Throwable ex)
            {
                onUnhandledException(ctx, ex);
            }
        }
    }

    /**
     * This event-handler will be executed whenever the enclosing
     * actor throws and unhandled exception of any kind.
     *
     * <p>
     * This method will be invoked given an unhandled exception
     * that is thrown by any of the other event-handlers,
     * but not those thrown by itself.
     * </p>
     *
     * @param ctx provides access to the actor itself, etc.
     * @param cause is the unhandled exception that was thrown.
     * @throws Throwable if something horrible happens.
     */
    private void onException (final CascadeContext ctx,
                              final Throwable cause)
            throws Throwable
    {
        synchronized (eventHandlerLock)
        {
            final List<ExceptionFunction> array = exceptionFunctions;

            for (int i = 0; i < array.size(); i++)
            {
                array.get(i).accept(ctx, cause);
            }
        }
    }

    /**
     * This event-handler will be executed when the enclosing actor is closed.
     *
     * @param ctx provides access to the actor itself, etc.
     * @throws Throwable if something horrible happens.
     */
    public void onClose (final CascadeContext ctx)
            throws Throwable
    {
        if (stopped.compareAndSet(false, true) == false)
        {
            return; // Already Stopping or Stopped.
        }

        synchronized (eventHandlerLock)
        {
            try
            {
                final List<CloseFunction> array = closeFunctions;

                for (int i = 0; i < array.size(); i++)
                {
                    array.get(i).accept(ctx);
                }
            }
            catch (Throwable ex1)
            {
                onUnhandledException(ctx, ex1);
            }
            finally
            {
                try
                {
                    unsubscribe(ctx);
                }
                catch (Throwable ex2)
                {
                    onUnhandledException(ctx, ex2);
                }
            }
        }
    }

    /**
     * Prepend the given function onto the list of actions
     * to perform in order to setup the enclosing actor.
     *
     * @param functor is an action.
     * @return this.
     */
    public CascadeScript prependToOnSetup (final SetupFunction functor)
    {
        synchronized (dataStructureLock)
        {
            Preconditions.checkNotNull(functor, "functor");
            final ImmutableList.Builder<SetupFunction> builder = ImmutableList.builder();
            builder.add(functor);
            builder.addAll(setupFunctions);
            setupFunctions = builder.build();
            return this;
        }
    }

    /**
     * Append the given function onto the list of actions
     * to perform in order to setup the enclosing actor.
     *
     * @param functor is an action.
     * @return this.
     */
    public CascadeScript appendToOnSetup (final SetupFunction functor)
    {
        synchronized (dataStructureLock)
        {
            Preconditions.checkNotNull(functor, "functor");
            final ImmutableList.Builder<SetupFunction> builder = ImmutableList.builder();
            builder.addAll(setupFunctions);
            builder.add(functor);
            setupFunctions = builder.build();
            return this;
        }
    }

    /**
     * Remove an element from the list of setup actions, if present.
     *
     * @param functor will be removed from the list.
     * @return this.
     */
    public CascadeScript removeOnSetup (final SetupFunction functor)
    {
        synchronized (dataStructureLock)
        {
            Preconditions.checkNotNull(functor, "functor");
            final List<SetupFunction> builder = Lists.newArrayList(setupFunctions);
            setupFunctions.remove(functor);
            setupFunctions = ImmutableList.copyOf(builder);
            return this;
        }
    }

    /**
     * Prepend the given function onto the list of actions to perform
     * in order to handle a message caused by the given event.
     *
     * @param event identifies an event-channel.
     * @param functor is an action.
     * @return this.
     */
    public CascadeScript prependToOnMessage (final CascadeToken event,
                                             final MessageFunction functor)
    {
        synchronized (dataStructureLock)
        {
            final List<MessageFunction> original = messageFunctions.getOrDefault(event, Collections.EMPTY_LIST);
            final List<MessageFunction> builder = Lists.newArrayListWithCapacity(original.size() + 1);
            builder.add(0, functor);
            builder.addAll(original);
            final List<MessageFunction> modified = ImmutableList.copyOf(builder);
            messageFunctions.put(event, modified);
            return this;
        }
    }

    /**
     * Append the given function onto the list of actions to perform
     * in order to handle a message caused by the given event.
     *
     * @param event identifies an event-channel.
     * @param functor is an action.
     * @return this.
     */
    public CascadeScript appendToOnMessage (final CascadeToken event,
                                            final MessageFunction functor)
    {
        synchronized (dataStructureLock)
        {
            final List<MessageFunction> original = messageFunctions.getOrDefault(event, Collections.EMPTY_LIST);
            final List<MessageFunction> builder = Lists.newArrayListWithCapacity(original.size() + 1);
            builder.addAll(original);
            builder.add(0, functor);
            final List<MessageFunction> modified = ImmutableList.copyOf(builder);
            messageFunctions.put(event, modified);
            return this;
        }
    }

    /**
     * Remove an element from the list of message-handler actions, if present.
     *
     * @param event identifies the relevant event-channel.
     * @param functor will be removed from the list.
     * @return this.
     */
    public CascadeScript removeOnMessage (final CascadeToken event,
                                          final MessageFunction functor)
    {
        synchronized (dataStructureLock)
        {
            final List<MessageFunction> original = messageFunctions.getOrDefault(event, Collections.EMPTY_LIST);
            final List<MessageFunction> builder = Lists.newArrayList(original);
            builder.remove(functor);
            final List<MessageFunction> modified = ImmutableList.copyOf(builder);
            messageFunctions.put(event, modified);
            return this;
        }
    }

    /**
     * Prepend the given function onto the list of actions
     * to perform in order to handle unhandled exceptions.
     *
     * @param functor is an action.
     * @return this.
     */
    public CascadeScript prependToOnException (final ExceptionFunction functor)
    {
        synchronized (dataStructureLock)
        {
            Preconditions.checkNotNull(functor, "functor");
            final ImmutableList.Builder<ExceptionFunction> builder = ImmutableList.builder();
            builder.add(functor);
            builder.addAll(exceptionFunctions);
            exceptionFunctions = builder.build();
            return this;
        }
    }

    /**
     * Append the given function onto the list of actions
     * to perform in order to handle unhandled exceptions.
     *
     * @param functor is an action.
     * @return this.
     */
    public CascadeScript appendToOnException (final ExceptionFunction functor)
    {
        synchronized (dataStructureLock)
        {
            Preconditions.checkNotNull(functor, "functor");
            final ImmutableList.Builder<ExceptionFunction> builder = ImmutableList.builder();
            builder.addAll(exceptionFunctions);
            builder.add(functor);
            exceptionFunctions = builder.build();
            return this;
        }
    }

    /**
     * Remove an element from the list of unhandled-exception handlers, if present.
     *
     * @param functor will be removed from the list.
     * @return this.
     */
    public CascadeScript removeOnException (final ExceptionFunction functor)
    {
        synchronized (dataStructureLock)
        {
            Preconditions.checkNotNull(functor, "functor");
            final List<ExceptionFunction> builder = Lists.newArrayList(exceptionFunctions);
            exceptionFunctions.remove(functor);
            exceptionFunctions = ImmutableList.copyOf(builder);
            return this;
        }
    }

    /**
     * Prepend the given function onto the list of actions
     * to perform in order to close the enclosing actor.
     *
     * @param functor is an action.
     * @return this.
     */
    public CascadeScript prependToOnClose (final CloseFunction functor)
    {
        synchronized (dataStructureLock)
        {
            Preconditions.checkNotNull(functor, "functor");
            final ImmutableList.Builder<CloseFunction> builder = ImmutableList.builder();
            builder.add(functor);
            builder.addAll(closeFunctions);
            closeFunctions = builder.build();
            return this;
        }
    }

    /**
     * Append the given function onto the list of actions
     * to perform in order to close the enclosing actor.
     *
     * @param functor is an action.
     * @return this.
     */
    public CascadeScript appendToOnClose (final CloseFunction functor)
    {
        synchronized (dataStructureLock)
        {
            Preconditions.checkNotNull(functor, "functor");
            final ImmutableList.Builder<CloseFunction> builder = ImmutableList.builder();
            builder.addAll(closeFunctions);
            builder.add(functor);
            closeFunctions = builder.build();
            return this;
        }
    }

    /**
     * Remove an element from the list of close actions, if present.
     *
     * @param functor will be removed from the list.
     * @return this.
     */
    public CascadeScript removeOnClose (final CloseFunction functor)
    {
        synchronized (dataStructureLock)
        {
            Preconditions.checkNotNull(functor, "functor");
            final List<CloseFunction> builder = Lists.newArrayList(closeFunctions);
            closeFunctions.remove(functor);
            closeFunctions = ImmutableList.copyOf(builder);
            return this;
        }
    }

    /**
     * Getter.
     *
     * @return the sequence of actions required to setup the enclosing actor.
     */
    public List<SetupFunction> setupScript ()
    {
        return ImmutableList.copyOf(setupFunctions);
    }

    /**
     * Getter.
     *
     * @return the sequence of actions required to process an event-message.
     */
    public Map<CascadeToken, List<MessageFunction>> messageScript ()
    {
        final ImmutableMap.Builder<CascadeToken, List<MessageFunction>> map = ImmutableMap.builder();

        for (CascadeToken key : messageFunctions.keySet())
        {
            map.put(key, messageFunctions.getOrDefault(key, Collections.EMPTY_LIST));
        }

        return map.build();
    }

    /**
     * Getter.
     *
     * @return the sequence of actions required to handle an unhandled-exception.
     */
    public List<ExceptionFunction> exceptionScript ()
    {
        return ImmutableList.copyOf(exceptionFunctions);
    }

    /**
     * Getter.
     *
     * @return the sequence of actions required to close the enclosing actor.
     */
    public List<CloseFunction> closeScript ()
    {
        return ImmutableList.copyOf(closeFunctions);
    }

    private void onUnhandledException (final CascadeContext ctx,
                                       final Throwable cause)
    {
        try
        {
            reinterruptIfNeeded(cause);
            onException(ctx, cause);
        }
        catch (Throwable ex1)
        {
            try
            {
                reinterruptIfNeeded(cause);
                onException(ctx, ex1);
            }
            catch (Throwable ex2)
            {
                try
                {
                    reinterruptIfNeeded(cause);
                    ex1.printStackTrace(System.err);
                }
                catch (Throwable ex3)
                {
                    // Pass. Oh my, you are in some deep trouble.
                }
            }
        }
    }

    private void reinterruptIfNeeded (final Throwable cause)
    {
        if (cause instanceof InterruptedException)
        {
            Thread.currentThread().interrupt();
        }
    }

    private void subscribe (final CascadeContext ctx)
    {
        messageFunctions.keySet().forEach(evt -> ctx.actor().subscribe(evt));
    }

    private void unsubscribe (final CascadeContext ctx)
    {
        messageFunctions.keySet().forEach(evt -> ctx.actor().unsubscribe(evt));
    }
}
