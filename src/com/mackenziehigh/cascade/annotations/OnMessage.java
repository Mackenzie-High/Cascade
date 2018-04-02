package com.mackenziehigh.cascade.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is a marker interface, for annotation-based cores,
 * which indicates that a method will be invoked whenever
 * messages are received from a named event-channel.
 */
@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.METHOD)
public @interface OnMessage
{
    public String value () default "";
}
