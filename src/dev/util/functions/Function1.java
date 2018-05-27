package dev.util.functions;

/**
 * A function of arity (1), which can throw checked-exceptions.
 */
@FunctionalInterface
public interface Function1<R, A>
{
    public R call (A arg1)
            throws Throwable;
}
