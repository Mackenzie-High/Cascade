package com.mackenziehigh.cascade.cores.annotated;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * @author mackenzie
 */
@Retention (RetentionPolicy.RUNTIME)
public @interface OnMessage
{
    public String[] value ();
}
