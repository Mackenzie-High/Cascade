package com.mackenziehigh.loader.internal;

import com.google.common.base.Verify;
import com.mackenziehigh.sexpr.SList;
import com.mackenziehigh.sexpr.SexprSchema;
import com.mackenziehigh.sexpr.annotations.Before;
import com.mackenziehigh.sexpr.annotations.Pass;
import java.io.IOException;

/**
 * This class is used to create and configure the controller,
 * message-processor(s), message-queue(s), and modules.
 */
final class Configurator
{
    private final StandardController controller = new StandardController();

    @Pass ("ECHO_PASS")
    @Before ("echo")
    public void visit_echo (final SList node)
    {
        System.out.println(node.get(1).toAtom().content());
    }

    public StandardController load (final SList config)
            throws IOException
    {
        /**
         * Load the schema.
         */
        final SexprSchema grammar = SexprSchema.fromResource("/com/mackenziehigh/loader/internal/Grammar.txt")
                .pass("ECHO_PASS")
                .defineViaAnnotations(this)
                .build();

        /**
         * Execute all of the actions specified by the configuration.
         */
        final boolean success = grammar.match(config);
        Verify.verify(success);

        return controller;
    }
}
