package dev.util.functions;

/**
 * A function of arity (0), which can throw checked-exceptions.
 */
@FunctionalInterface
public interface Function0<R>
{
    public R call ()
            throws Throwable;
}
