package com.mackenziehigh.cascade.builder;

import com.mackenziehigh.cascade.Output;
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
