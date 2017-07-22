package com.mackenziehigh.loader.internal;

import com.mackenziehigh.sexpr.SList;
import com.mackenziehigh.sexpr.Sexpr;
import com.mackenziehigh.sexpr.SexprSchema;
import java.io.IOException;

/**
 *
 */
public final class MainDev
{
    public static void main (String[] args)
            throws IOException
    {
        final SexprSchema grammar = SexprSchema.fromResource("/com/mackenziehigh/loader/symbolic/Grammar.txt")
                .setFailureHandler(x -> System.out.println(x.get().location().toString()))
                .build();
        final Sexpr input = SList.parseResource("/com/mackenziehigh/loader/symbolic/Input.txt");
        final boolean matched = grammar.match(input);
        System.out.println(matched);
    }
}
