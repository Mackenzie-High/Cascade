package dev.util.functions;

/**
 * A function of arity (4), which can throw checked-exceptions.
 */
@FunctionalInterface
public interface Function4<R, A, B, C, D>
{
    public R call (A arg1,
                   B arg2,
                   C arg3,
                   D arg4)
            throws Throwable;
}
