package com.mackenziehigh.loader.internal.parser;

import high.mackenzie.snowflake.Grammar;
import high.mackenzie.snowflake.GrammarBuilder;
import high.mackenzie.snowflake.IParser;
import high.mackenzie.snowflake.ParserOutput;

/**
 * This class was auto-generated using the Snowflake parser-generator.
 *
 * <p>
 * Generated On: Sat Jun 10 17:04:15 EDT 2017</p>
 */
public final class GeneratedParser
        implements IParser
{

    private static final Grammar grammar = grammar();

    /**
     * This method constructs the grammar object.
     */
    private static Grammar grammar ()
    {
        final GrammarBuilder g = new GrammarBuilder();

        // Character Classes
        g.range("@class0", (char) 58, (char) 58);
        g.range("@class1", (char) 64, (char) 64);
        g.range("@class2", (char) 91, (char) 91);
        g.range("@class3", (char) 44, (char) 44);
        g.range("@class4", (char) 93, (char) 93);
        g.range("@class5", (char) 123, (char) 123);
        g.range("@class6", (char) 125, (char) 125);
        g.range("@class7", (char) 59, (char) 59);
        g.range("@class8", (char) 65, (char) 90);
        g.range("@class9", (char) 97, (char) 122);
        g.range("@class10", (char) 95, (char) 95);
        g.range("@class11", (char) 36, (char) 36);
        g.combine("@class12", "@class8", "@class9", "@class10", "@class11");
        g.range("@class13", (char) 65, (char) 90);
        g.range("@class14", (char) 97, (char) 122);
        g.range("@class15", (char) 95, (char) 95);
        g.range("@class16", (char) 36, (char) 36);
        g.range("@class17", (char) 48, (char) 57);
        g.combine("@class18", "@class13", "@class14", "@class15", "@class16", "@class17");
        g.range("@class19", (char) 46, (char) 46);
        g.range("@class20", (char) 45, (char) 45);
        g.range("@class21", (char) 45, (char) 45);
        g.range("@class22", (char) 46, (char) 46);
        g.range("@class23", (char) 95, (char) 95);
        g.range("@class24", (char) 48, (char) 57);
        g.range("@class25", (char) 101, (char) 101);
        g.range("@class26", (char) 69, (char) 69);
        g.combine("@class27", "@class25", "@class26");
        g.range("@class28", (char) 45, (char) 45);
        g.range("@class29", (char) 43, (char) 43);
        g.combine("@class30", "@class28", "@class29");
        g.range("@class31", (char) 34, (char) 34);
        g.range("@class32", (char) 34, (char) 34);
        g.combine("@class33", "@class32");
        g.negate("@class34", "@class33");
        g.range("@class35", (char) 39, (char) 39);
        g.combine("@class36", "@class35");
        g.negate("@class37", "@class36");
        g.range("@class38", (char) 92, (char) 92);
        g.range("@class39", (char) 116, (char) 116);
        g.range("@class40", (char) 98, (char) 98);
        g.range("@class41", (char) 110, (char) 110);
        g.range("@class42", (char) 114, (char) 114);
        g.range("@class43", (char) 102, (char) 102);
        g.range("@class44", (char) 34, (char) 34);
        g.range("@class45", (char) 92, (char) 92);
        g.range("@class46", (char) 35, (char) 35);
        g.range("@class47", (char) 10, (char) 10);
        g.range("@class48", (char) 13, (char) 13);
        g.combine("@class49", "@class47", "@class48");
        g.negate("@class50", "@class49");
        g.range("@class51", (char) 10, (char) 10);
        g.range("@class52", (char) 13, (char) 13);
        g.combine("@class53", "@class51", "@class52");
        g.range("@class54", (char) 0, (char) 65535);
        g.combine("@class55", "@class54");
        g.range("@class56", (char) 10, (char) 10);
        g.range("@class57", (char) 13, (char) 13);
        g.combine("@class58", "@class56", "@class57");
        g.range("@class59", (char) 10, (char) 10);
        g.range("@class60", (char) 13, (char) 13);
        g.combine("@class61", "@class59", "@class60");
        g.negate("@class62", "@class61");
        g.range("@class63", (char) 32, (char) 32);
        g.range("@class64", (char) 9, (char) 9);
        g.range("@class65", (char) 10, (char) 10);
        g.range("@class66", (char) 11, (char) 11);
        g.range("@class67", (char) 12, (char) 12);
        g.range("@class68", (char) 13, (char) 13);
        g.combine("@class69", "@class63", "@class64", "@class65", "@class66", "@class67", "@class68");

        // Grammar Rules
        g.choose("@36", "DIGIT", "@35");
        g.choose("@57", "SP", "COMMENT");
        g.choose("@60", "@58", "@59");
        g.choose("COMMENT", "MULTI_LINE_COMMENT", "SINGLE_LINE_COMMENT");
        g.choose("ESCAPE_CHAR", "@48", "@49", "@50", "@51", "@52", "@53", "@54", "@55", "@56");
        g.choose("NON_QUOTE", "ESCAPE_SEQUENCE", "@41");
        g.choose("NON_TRIPLE_QUOTE", "ESCAPE_SEQUENCE", "@45", "@46");
        g.choose("STRING_LITERAL", "STRING_LITERAL_STYLE1", "STRING_LITERAL_STYLE2");
        g.choose("boolean", "true", "false");
        g.choose("program_member", "threads_directive", "module_directive");
        g.choose("string", "verbatim_string", "regular_string");
        g.choose("value", "boolean", "float", "integer", "string", "class", "list", "map");
        g.chr("@10", "@class2");
        g.chr("@11", "@class3");
        g.chr("@16", "@class4");
        g.chr("@17", "@class5");
        g.chr("@19", "@class6");
        g.chr("@21", "@class7");
        g.chr("@22", "@class12");
        g.chr("@23", "@class18");
        g.chr("@25", "@class19");
        g.chr("@28", "@class20");
        g.chr("@30", "@class21");
        g.chr("@32", "@class22");
        g.chr("@33", "@class23");
        g.chr("@38", "@class27");
        g.chr("@39", "@class30");
        g.chr("@4", "@class0");
        g.chr("@41", "@class34");
        g.chr("@46", "@class37");
        g.chr("@47", "@class38");
        g.chr("@48", "@class39");
        g.chr("@49", "@class40");
        g.chr("@50", "@class41");
        g.chr("@51", "@class42");
        g.chr("@52", "@class43");
        g.chr("@54", "@class44");
        g.chr("@55", "@class45");
        g.chr("@59", "@class46");
        g.chr("@61", "@class50");
        g.chr("@63", "@class53");
        g.chr("@67", "@class55");
        g.chr("@9", "@class1");
        g.chr("DIGIT", "@class24");
        g.chr("NEWLINE", "@class58");
        g.chr("NON_NEWLINE", "@class62");
        g.chr("QUOTE", "@class31");
        g.chr("SP", "@class69");
        g.not("@44", "@43");
        g.not("@6", "ID");
        g.not("@66", "@65");
        g.not("@8", "ID");
        g.repeat("@0", "program_member", 0, 2147483647);
        g.repeat("@13", "@12", 0, 2147483647);
        g.repeat("@15", "@14", 0, 1);
        g.repeat("@18", "map_entry", 0, 2147483647);
        g.repeat("@24", "@23", 0, 2147483647);
        g.repeat("@27", "@26", 0, 2147483647);
        g.repeat("@29", "@28", 0, 1);
        g.repeat("@31", "@30", 0, 1);
        g.repeat("@34", "@33", 1, 2147483647);
        g.repeat("@37", "@36", 0, 2147483647);
        g.repeat("@40", "@39", 0, 1);
        g.repeat("@62", "@61", 0, 2147483647);
        g.repeat("@69", "@68", 0, 2147483647);
        g.repeat("EXPONENT_OPT", "EXPONENT", 0, 1);
        g.repeat("NON_NEWLINES", "NON_NEWLINE", 0, 2147483647);
        g.repeat("NON_QUOTES", "NON_QUOTE", 0, 2147483647);
        g.repeat("NON_TRIPLE_QUOTES", "NON_TRIPLE_QUOTE", 0, 2147483647);
        g.repeat("WS", "@57", 0, 2147483647);
        g.sequence("@12", "WS", "@11", "WS", "list_element", "WS");
        g.sequence("@14", "list_element", "@13");
        g.sequence("@26", "@25", "ID");
        g.sequence("@35", "@34", "DIGIT");
        g.sequence("@45", "@42", "@44");
        g.sequence("@56", "DIGIT", "DIGIT", "DIGIT", "DIGIT", "DIGIT");
        g.sequence("@68", "@66", "@67");
        g.sequence("CLASS", "ID", "@27");
        g.sequence("DIGITS", "DIGIT", "@37");
        g.sequence("ESCAPE_SEQUENCE", "@47", "ESCAPE_CHAR");
        g.sequence("EXPONENT", "@38", "@40", "DIGITS");
        g.sequence("FLOAT", "@31", "DIGITS", "@32", "DIGITS", "EXPONENT_OPT");
        g.sequence("ID", "@22", "@24");
        g.sequence("INTEGER", "@29", "DIGITS");
        g.sequence("MULTI_LINE_COMMENT", "@64", "@69", "@70", "WS");
        g.sequence("SINGLE_LINE_COMMENT", "@60", "@62", "@63");
        g.sequence("STRING_LITERAL_STYLE1", "QUOTE", "NON_QUOTES", "QUOTE");
        g.sequence("STRING_LITERAL_STYLE2", "TRIPLE_QUOTE", "NON_TRIPLE_QUOTES", "TRIPLE_QUOTE");
        g.sequence("class", "CLASS", "WS");
        g.sequence("false", "@7", "@8", "WS");
        g.sequence("float", "FLOAT", "WS");
        g.sequence("integer", "INTEGER", "WS");
        g.sequence("list", "@10", "WS", "@15", "WS", "@16", "WS");
        g.sequence("list_element", "value");
        g.sequence("map", "@17", "WS", "@18", "WS", "@19", "WS");
        g.sequence("map_entry", "map_key", "WS", "@20", "WS", "map_value", "WS", "@21", "WS");
        g.sequence("map_key", "ID");
        g.sequence("map_value", "value");
        g.sequence("module_class", "CLASS");
        g.sequence("module_config", "map");
        g.sequence("module_directive", "@3", "WS", "module_name", "WS", "@4", "WS", "module_class", "WS", "module_config", "WS");
        g.sequence("module_name", "ID");
        g.sequence("program", "WS", "@0", "END");
        g.sequence("regular_string", "STRING_LITERAL", "WS");
        g.sequence("threads_directive", "@1", "WS", "@2", "WS", "integer", "WS");
        g.sequence("true", "@5", "@6", "WS");
        g.sequence("verbatim_string", "@9", "WS", "STRING_LITERAL", "WS");
        g.str("@1", "threads");
        g.str("@2", "=");
        g.str("@20", "=");
        g.str("@3", "module");
        g.str("@42", "'");
        g.str("@43", "''");
        g.str("@5", "true");
        g.str("@53", "'");
        g.str("@58", "//");
        g.str("@64", "/*");
        g.str("@65", "*/");
        g.str("@7", "false");
        g.str("@70", "*/");
        g.str("TRIPLE_QUOTE", "'''");

        // Specify which rule is the root of the grammar.
        g.setRoot("program");

        // Specify the number of tracing records to store concurrently.
        g.setTraceCount(1024);

        // Perform the actual construction of the grammar object.
        return g.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ParserOutput parse (final char[] input)
    {
        return grammar.newParser().parse(input);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ParserOutput parse (final String input)
    {
        return parse(input.toCharArray());
    }
}
