package dev.util.functions;

/**
 * A function of arity (2), which can throw checked-exceptions.
 */
@FunctionalInterface
public interface Function2<R, A, B>
{
    public R call (A arg1,
                   B arg2)
            throws Throwable;
}
