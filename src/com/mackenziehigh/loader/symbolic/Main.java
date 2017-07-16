package com.mackenziehigh.loader.symbolic;

import com.mackenziehigh.sexpr.SList;
import com.mackenziehigh.sexpr.Sexpr;
import com.mackenziehigh.sexpr.SexprSchema;
import java.io.IOException;

/**
 *
 */
public final class Main
{
    public static void main (String[] args)
            throws IOException
    {
        final SexprSchema grammar = SexprSchema.fromResource("/com/mackenziehigh/loader/symbolic/Grammar.txt").build();
        final Sexpr input = SList.parseResource("/com/mackenziehigh/loader/symbolic/Input.txt");
        grammar.match(input);
    }
}
