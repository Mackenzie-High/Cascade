package dev.util.functions;

/**
 * A function of arity (6), which can throw checked-exceptions.
 */
@FunctionalInterface
public interface Function6<R, A, B, C, D, E, F>
{
    public R call (A arg1,
                   B arg2,
                   C arg3,
                   D arg4,
                   E arg5,
                   F arg6)
            throws Throwable;
}
