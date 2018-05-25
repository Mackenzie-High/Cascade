package com.mackenziehigh.cascade.reactor.builder;

import com.mackenziehigh.cascade.reactor.Output;
import java.util.function.Predicate;

/**
 *
 * @author mackenzie
 */
public interface OutputBuilder<T>
{
    public OutputBuilder<T> named (String name);

    public OutputBuilder<T> verify (Predicate<T> condition);

    public Output<T> build ();
}
