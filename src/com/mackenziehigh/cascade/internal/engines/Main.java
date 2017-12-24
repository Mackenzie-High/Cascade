package com.mackenziehigh.cascade.internal.engines;

import com.google.common.collect.Maps;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import com.mackenziehigh.cascade.CascadeToken;
import com.mackenziehigh.cascade.internal.messages.ConcreteAllocator;
import com.mackenziehigh.cascade.internal.routing.EventDispatcher;
import com.mackenziehigh.cascade.internal.routing.EventDispatcher.ConcurrentEventSender;
import java.util.Map;

/**
 *
 */
public final class Main
{
    public static void main (String[] args)
            throws Throwable
    {
        final EventConsumer cons1 = (CascadeToken event, OperandStack message) ->
        {
            System.out.println("I love " + message.asString() + " very very much!");
        };

        final ConcreteAllocator allocator = new ConcreteAllocator();
        allocator.addDynamicPool("default", 0, 1024);

        final Map<CascadeToken, Connection> map = Maps.newConcurrentMap();
        map.put(CascadeToken.create("cons1"), new DedicatedEngineWithArrayQueue(allocator, 128, cons1));

        final EventDispatcher dispatcher = new EventDispatcher(map);
        map.values().forEach(x -> ((Engine) x).start());

        dispatcher.register(CascadeToken.create("cons1"), CascadeToken.create("hello"));

        final ConcurrentEventSender mainSender = dispatcher.lookup(CascadeToken.create("main"));
        final OperandStack msg = allocator.newOperandStack();

        mainSender.broadcast(CascadeToken.create("hello"), msg.push("Chicky"));
    }
}
