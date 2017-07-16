package com.mackenziehigh.loader.internal.parser;

import com.mackenziehigh.loader.internal.Configuration;
import high.mackenzie.snowflake.ParserOutput;

/**
 * Configuration Parser.
 */
public final class Parser
{
    public Configuration parse (final String input)
    {
        final GeneratedParser parser = new GeneratedParser();

        final ParserOutput output = parser.parse(input);

        if (output.success() == false)
        {
            throw new IllegalArgumentException();
        }

        final Visitor visitor = new Visitor();

        visitor.visit(output.parseTree());

        final Configuration result = visitor.output;

        return result;
    }
}
