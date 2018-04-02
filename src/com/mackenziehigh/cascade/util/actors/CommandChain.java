package com.mackenziehigh.cascade.util.actors;

import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeToken;
import com.mackenziehigh.cascade.util.PrimitiveTypes;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 *
 * @param <T>
 */
public interface CommandChain<T extends CommandChain<T>>
{
    public T limit (long n);

    public T range (long initial,
                    long delta);

    public T range (long minimum,
                    long maximum,
                    long initial,
                    long delta);

    public T pushInstant ();

    public T pushEpoch (TimeUnit unit);

    public T pushElapsedDuration ();

    public T pushElapsed (TimeUnit unit);

    public T pushMonotonic (TimeUnit unit);

    public T pushTimeGapDuration ();

    public T pushTimeGap (TimeUnit unit);

    public T pushHertz ();

    public T pushRandom (PrimitiveTypes type);

    public T pushRandom (List<?> list);

    public T require (Predicate<CascadeStack> condition);

    public T requireStackHeight (int... options);

    public T requireStackDelta (int... options);

    public T requireStaticStackDelta (int delta);

    public T rateLimit (double permitsPerSecond);

    public T percentLimit (double percent);

    public T distinct (int limit);

    public <U> T cast (Class<U> type);

    public T convertTo (PrimitiveTypes type);

    public T convertToObject ();

    public T push (boolean value);

    public T push (char value);

    public T push (byte value);

    public T push (short value);

    public T push (int value);

    public T push (long value);

    public T push (float value);

    public T push (double value);

    public T push (Object value);

    public T pop ();

    public T pop (int n);

    public T dup ();

    public T dup (int n);

    public T swap ();

    public T swap (int n);

    public T repeat ();

    public T repeat (int n);

    public T replace (CascadeStack newStack);

    public T replaceIf (CascadeStack newStack,
                        Predicate<CascadeStack> condition);

    public T filter (Predicate<CascadeStack> condition);

    public T map (Function<CascadeStack, CascadeStack> functor);

    public T skipAfter (Predicate<CascadeStack> condition);

    public T skipBefore (Predicate<CascadeStack> condition);

    public T negate ();

    public T divide ();

    public T remainder ();

    public T multiply ();

    public T add ();

    public T subtract ();

    public T forEach (Consumer<CascadeStack> action);

    public T stash (String key,
                    int n);

    public T unstash (String key);

    public T demux ();

    public T send (CascadeToken event);

    public T sendIf (CascadeToken event,
                     Predicate<CascadeStack> condition);

    public T sendIfElse (CascadeToken eventTrue,
                         CascadeToken eventFalse,
                         Predicate<CascadeStack> condition);
}
