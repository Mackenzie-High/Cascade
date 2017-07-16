package com.mackenziehigh.loader.internal.parser;

import com.mackenziehigh.loader.ConfigObject;
import com.mackenziehigh.loader.internal.Configuration;
import high.mackenzie.snowflake.ITreeNode;
import high.mackenzie.snowflake.TreeNode;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * An instance of this class is used to convert a parse-tree to a Configuration object.
 */
final class Visitor
        extends AbstractVisitor
{
    private final Stack<Object> stack = new Stack<>();

    private final ConfigObjectFactory factory = new ConfigObjectFactory();

    public final Configuration output = new Configuration();

    @Override
    protected void visit_verbatim_string (ITreeNode node)
    {
        final String value = TreeNode.find(node, "STRING_LITERAL").text();
        final ConfigObject x = factory.fromString(value);
        stack.push(x);
    }

    @Override
    protected void visit_true (ITreeNode node)
    {
        final ConfigObject x = factory.fromBoolean(true);
        stack.push(x);
    }

    @Override
    protected void visit_threads_directive (ITreeNode node)
    {
        final int threadCount = Integer.parseInt(TreeNode.find(node, "INTEGER").text());
        output.setThreadCount(threadCount);
    }

    @Override
    protected void visit_regular_string (ITreeNode node)
    {
        final String value = TreeNode.find(node, "NON_QUOTES").text();
        final ConfigObject x = factory.fromString(value);
        stack.push(x);
    }

    @Override
    protected void visit_module_directive (ITreeNode node)
    {
        visitChildren(node);
        final String moduleName = TreeNode.find(node, "module_name").text();
        final String moduleClass = TreeNode.find(node, "module_class").text();
        final ConfigObject moduleConfig = (ConfigObject) stack.pop();
        output.setModule(moduleName, moduleClass, moduleConfig);
    }

    @Override
    protected void visit_map_value (ITreeNode node)
    {
        visitChildren(node);
    }

    @Override
    protected void visit_map_key (ITreeNode node)
    {
        final String key = node.text();
        stack.push(key);
    }

    @Override
    protected void visit_map_entry (ITreeNode node)
    {
        visitChildren(node);
        final ConfigObject value = (ConfigObject) stack.pop();
        final String key = (String) stack.pop();
        final Map map = (Map) stack.pop();
        map.put(key, value);
        stack.push(map);
    }

    @Override
    protected void visit_map (ITreeNode node)
    {
        stack.push(new HashMap<>());
        visitChildren(node);
        stack.push(factory.fromMap((Map) stack.pop()));
    }

    @Override
    protected void visit_list_element (ITreeNode node)
    {
        final List list = (List) stack.pop();
        visitChildren(node);
        list.add(stack.pop());
        stack.push(list);
    }

    @Override
    protected void visit_list (ITreeNode node)
    {
        stack.push(new LinkedList<>());
        visitChildren(node);
        stack.push(factory.fromList((List) stack.pop()));
    }

    @Override
    protected void visit_integer (ITreeNode node)
    {
        final long value = Long.parseLong(TreeNode.find(node, "INTEGER").text());
        final ConfigObject x = factory.fromInteger(value);
        stack.push(x);
    }

    @Override
    protected void visit_float (ITreeNode node)
    {
        final double value = Double.parseDouble(TreeNode.find(node, "FLOAT").text());
        final ConfigObject x = factory.fromFloat(value);
        stack.push(x);
    }

    @Override
    protected void visit_false (ITreeNode node)
    {
        final ConfigObject x = factory.fromBoolean(true);
        stack.push(x);
    }

    @Override
    protected void visit_class (ITreeNode node)
    {
        final String className = node.text();
        final ConfigObject x = factory.fromClass(className);
        stack.push(x);
    }

    @Override
    public void visitUnknown (ITreeNode node)
    {
        visitChildren(node);
    }

    private void visitChildren (ITreeNode node)
    {
        for (ITreeNode child : node.children())
        {
            visit(child);
        }
    }
}
