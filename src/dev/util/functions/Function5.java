package dev.util.functions;

/**
 * A function of arity (5), which can throw checked-exceptions.
 */
@FunctionalInterface
public interface Function5<R, A, B, C, D, E>
{
    public R call (A arg1,
                   B arg2,
                   C arg3,
                   D arg4,
                   E arg5)
            throws Throwable;
}
