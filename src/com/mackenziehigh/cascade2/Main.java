package com.mackenziehigh.cascade2;

import com.google.common.collect.Maps;
import com.mackenziehigh.cascade.CascadeAllocator.OperandStack;
import com.mackenziehigh.cascade.internal.messages.ConcreteAllocator;
import com.mackenziehigh.cascade2.EventDispatcher.ConcurrentEventSender;
import java.util.Map;

/**
 *
 */
public final class Main
{
    public static void main (String[] args)
            throws Throwable
    {
        final EventConsumer cons1 = (Token event, OperandStack message) ->
        {
            System.out.println("I love " + message.asString() + " very very much!");
        };

        final ConcreteAllocator allocator = new ConcreteAllocator();
        allocator.addDynamicPool("default", 0, 1024);

        final Map<Token, Connection> map = Maps.newConcurrentMap();
        map.put(Token.create("cons1"), new DedicatedConnection(allocator, 128, cons1));

        final EventDispatcher dispatcher = new EventDispatcher(map);
        map.values().forEach(x -> x.start());

        dispatcher.register(Token.create("cons1"), Token.create("hello"));

        final ConcurrentEventSender mainSender = dispatcher.lookup(Token.create("main"));
        final OperandStack msg = allocator.newOperandStack();

        mainSender.broadcast(Token.create("hello"), msg.push("Chicky"));
    }
}
