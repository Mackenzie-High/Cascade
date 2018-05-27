package dev.util.functions;

/**
 * A function of arity (7), which can throw checked-exceptions.
 */
@FunctionalInterface
public interface Function7<R, A, B, C, D, E, F, G>
{
    public R call (A arg1,
                   B arg2,
                   C arg3,
                   D arg4,
                   E arg5,
                   F arg6,
                   G arg7)
            throws Throwable;
}
