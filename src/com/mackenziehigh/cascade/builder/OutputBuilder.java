package com.mackenziehigh.cascade.builder;

import com.mackenziehigh.cascade.PrivateOutput;
import java.util.function.Predicate;

/**
 *
 * @author mackenzie
 */
public interface OutputBuilder<T>
{
    public OutputBuilder<T> named (String name);

    public OutputBuilder<T> verify (Predicate<T> condition);

    public PrivateOutput<T> build ();
}
