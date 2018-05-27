package dev.util.functions;

/**
 * A function of arity (3), which can throw checked-exceptions.
 */
@FunctionalInterface
public interface Function3<R, A, B, C>
{
    public R call (A arg1,
                   B arg2,
                   C arg3)
            throws Throwable;
}
