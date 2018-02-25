package com.mackenziehigh.cascade.redo2.scripts;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is a marker interface, for annotation-based cores,
 * which indicates that a method will be invoked whenever
 * an unhandled exception occurs in an event-handler.
 */
@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.METHOD)
public @interface OnException
{
    // Pass
}
