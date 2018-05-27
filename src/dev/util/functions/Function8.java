package dev.util.functions;

/**
 * A function of arity (8), which can throw checked-exceptions.
 */
@FunctionalInterface
public interface Function8<R, A, B, C, D, E, F, G, H>
{
    public R call (A arg1,
                   B arg2,
                   C arg3,
                   D arg4,
                   E arg5,
                   F arg6,
                   G arg7,
                   H arg8)
            throws Throwable;
}
