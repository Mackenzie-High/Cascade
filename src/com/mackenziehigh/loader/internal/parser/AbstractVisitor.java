package com.mackenziehigh.loader.internal.parser;

import high.mackenzie.snowflake.ITreeNode;
import high.mackenzie.snowflake.ITreeNodeVisitor;

/**
 * This class was auto-generated using the Snowflake parser-generator.
 *
 * <p>
 * Generated On: Sat Jun 10 17:04:15 EDT 2017</p>
 */
public abstract class AbstractVisitor
        implements ITreeNodeVisitor
{

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit (ITreeNode node)
    {
        final String name = node.rule();

        if ("program".equals(name))
        {
            visit_program(node);
        }
        else if ("program_member".equals(name))
        {
            visit_program_member(node);
        }
        else if ("threads_directive".equals(name))
        {
            visit_threads_directive(node);
        }
        else if ("module_directive".equals(name))
        {
            visit_module_directive(node);
        }
        else if ("module_name".equals(name))
        {
            visit_module_name(node);
        }
        else if ("module_class".equals(name))
        {
            visit_module_class(node);
        }
        else if ("module_config".equals(name))
        {
            visit_module_config(node);
        }
        else if ("value".equals(name))
        {
            visit_value(node);
        }
        else if ("boolean".equals(name))
        {
            visit_boolean(node);
        }
        else if ("true".equals(name))
        {
            visit_true(node);
        }
        else if ("false".equals(name))
        {
            visit_false(node);
        }
        else if ("integer".equals(name))
        {
            visit_integer(node);
        }
        else if ("float".equals(name))
        {
            visit_float(node);
        }
        else if ("string".equals(name))
        {
            visit_string(node);
        }
        else if ("verbatim_string".equals(name))
        {
            visit_verbatim_string(node);
        }
        else if ("regular_string".equals(name))
        {
            visit_regular_string(node);
        }
        else if ("class".equals(name))
        {
            visit_class(node);
        }
        else if ("list".equals(name))
        {
            visit_list(node);
        }
        else if ("list_element".equals(name))
        {
            visit_list_element(node);
        }
        else if ("map".equals(name))
        {
            visit_map(node);
        }
        else if ("map_entry".equals(name))
        {
            visit_map_entry(node);
        }
        else if ("map_key".equals(name))
        {
            visit_map_key(node);
        }
        else if ("map_value".equals(name))
        {
            visit_map_value(node);
        }
        else if ("ID".equals(name))
        {
            visit_ID(node);
        }
        else if ("CLASS".equals(name))
        {
            visit_CLASS(node);
        }
        else if ("INTEGER".equals(name))
        {
            visit_INTEGER(node);
        }
        else if ("FLOAT".equals(name))
        {
            visit_FLOAT(node);
        }
        else if ("DIGITS".equals(name))
        {
            visit_DIGITS(node);
        }
        else if ("DIGIT".equals(name))
        {
            visit_DIGIT(node);
        }
        else if ("EXPONENT_OPT".equals(name))
        {
            visit_EXPONENT_OPT(node);
        }
        else if ("EXPONENT".equals(name))
        {
            visit_EXPONENT(node);
        }
        else if ("STRING_LITERAL".equals(name))
        {
            visit_STRING_LITERAL(node);
        }
        else if ("STRING_LITERAL_STYLE1".equals(name))
        {
            visit_STRING_LITERAL_STYLE1(node);
        }
        else if ("QUOTE".equals(name))
        {
            visit_QUOTE(node);
        }
        else if ("NON_QUOTES".equals(name))
        {
            visit_NON_QUOTES(node);
        }
        else if ("NON_QUOTE".equals(name))
        {
            visit_NON_QUOTE(node);
        }
        else if ("STRING_LITERAL_STYLE2".equals(name))
        {
            visit_STRING_LITERAL_STYLE2(node);
        }
        else if ("TRIPLE_QUOTE".equals(name))
        {
            visit_TRIPLE_QUOTE(node);
        }
        else if ("NON_TRIPLE_QUOTES".equals(name))
        {
            visit_NON_TRIPLE_QUOTES(node);
        }
        else if ("NON_TRIPLE_QUOTE".equals(name))
        {
            visit_NON_TRIPLE_QUOTE(node);
        }
        else if ("ESCAPE_SEQUENCE".equals(name))
        {
            visit_ESCAPE_SEQUENCE(node);
        }
        else if ("ESCAPE_CHAR".equals(name))
        {
            visit_ESCAPE_CHAR(node);
        }
        else if ("WS".equals(name))
        {
            visit_WS(node);
        }
        else if ("COMMENT".equals(name))
        {
            visit_COMMENT(node);
        }
        else if ("SINGLE_LINE_COMMENT".equals(name))
        {
            visit_SINGLE_LINE_COMMENT(node);
        }
        else if ("MULTI_LINE_COMMENT".equals(name))
        {
            visit_MULTI_LINE_COMMENT(node);
        }
        else if ("NEWLINE".equals(name))
        {
            visit_NEWLINE(node);
        }
        else if ("NON_NEWLINE".equals(name))
        {
            visit_NON_NEWLINE(node);
        }
        else if ("NON_NEWLINES".equals(name))
        {
            visit_NON_NEWLINES(node);
        }
        else if ("SP".equals(name))
        {
            visit_SP(node);
        }
        else
        {
            visitUnknown(node);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitUnknown (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
    }

    /**
     * This method visits a parse-tree node created by rule "CLASS".
     */
    protected void visit_CLASS (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "COMMENT".
     */
    protected void visit_COMMENT (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "DIGIT".
     */
    protected void visit_DIGIT (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "DIGITS".
     */
    protected void visit_DIGITS (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "ESCAPE_CHAR".
     */
    protected void visit_ESCAPE_CHAR (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "ESCAPE_SEQUENCE".
     */
    protected void visit_ESCAPE_SEQUENCE (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "EXPONENT".
     */
    protected void visit_EXPONENT (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "EXPONENT_OPT".
     */
    protected void visit_EXPONENT_OPT (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "FLOAT".
     */
    protected void visit_FLOAT (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "ID".
     */
    protected void visit_ID (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "INTEGER".
     */
    protected void visit_INTEGER (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "MULTI_LINE_COMMENT".
     */
    protected void visit_MULTI_LINE_COMMENT (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "NEWLINE".
     */
    protected void visit_NEWLINE (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "NON_NEWLINE".
     */
    protected void visit_NON_NEWLINE (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "NON_NEWLINES".
     */
    protected void visit_NON_NEWLINES (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "NON_QUOTE".
     */
    protected void visit_NON_QUOTE (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "NON_QUOTES".
     */
    protected void visit_NON_QUOTES (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "NON_TRIPLE_QUOTE".
     */
    protected void visit_NON_TRIPLE_QUOTE (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "NON_TRIPLE_QUOTES".
     */
    protected void visit_NON_TRIPLE_QUOTES (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "QUOTE".
     */
    protected void visit_QUOTE (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "SINGLE_LINE_COMMENT".
     */
    protected void visit_SINGLE_LINE_COMMENT (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "SP".
     */
    protected void visit_SP (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "STRING_LITERAL".
     */
    protected void visit_STRING_LITERAL (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "STRING_LITERAL_STYLE1".
     */
    protected void visit_STRING_LITERAL_STYLE1 (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "STRING_LITERAL_STYLE2".
     */
    protected void visit_STRING_LITERAL_STYLE2 (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "TRIPLE_QUOTE".
     */
    protected void visit_TRIPLE_QUOTE (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "WS".
     */
    protected void visit_WS (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "boolean".
     */
    protected void visit_boolean (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "class".
     */
    protected void visit_class (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "false".
     */
    protected void visit_false (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "float".
     */
    protected void visit_float (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "integer".
     */
    protected void visit_integer (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "list".
     */
    protected void visit_list (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "list_element".
     */
    protected void visit_list_element (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "map".
     */
    protected void visit_map (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "map_entry".
     */
    protected void visit_map_entry (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "map_key".
     */
    protected void visit_map_key (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "map_value".
     */
    protected void visit_map_value (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "module_class".
     */
    protected void visit_module_class (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "module_config".
     */
    protected void visit_module_config (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "module_directive".
     */
    protected void visit_module_directive (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "module_name".
     */
    protected void visit_module_name (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "program".
     */
    protected void visit_program (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "program_member".
     */
    protected void visit_program_member (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "regular_string".
     */
    protected void visit_regular_string (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "string".
     */
    protected void visit_string (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "threads_directive".
     */
    protected void visit_threads_directive (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "true".
     */
    protected void visit_true (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "value".
     */
    protected void visit_value (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

    /**
     * This method visits a parse-tree node created by rule "verbatim_string".
     */
    protected void visit_verbatim_string (ITreeNode node)
    {
        // You should *not* place your code right here.
        // Instead, you should override this method via a subclass.
        visitUnknown(node); // Default Behavior
    }

}
