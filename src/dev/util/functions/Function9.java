package dev.util.functions;

/**
 * A function of arity (9), which can throw checked-exceptions.
 */
@FunctionalInterface
public interface Function9<R, A, B, C, D, E, F, G, H, I>
{
    public R call (A arg1,
                   B arg2,
                   C arg3,
                   D arg4,
                   E arg5,
                   F arg6,
                   G arg7,
                   H arg8,
                   I arg9)
            throws Throwable;
}
