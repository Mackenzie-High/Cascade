package com.mackenziehigh.cascade.builder;

import java.util.function.Predicate;
import com.mackenziehigh.cascade.PrivateInput;

/**
 *
 */
public interface InputBuilder<E>

{

    public InputBuilder<E> named (String name);

    public InputBuilder<E> verify (Predicate<E> condition);

    public PrivateInput<E> build ();
}
