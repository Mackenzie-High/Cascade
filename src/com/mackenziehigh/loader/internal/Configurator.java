package com.mackenziehigh.loader.internal;

import com.google.common.base.Verify;
import com.mackenziehigh.loader.AbstractModule;
import com.mackenziehigh.loader.CommonLogger;
import com.mackenziehigh.loader.UniqueID;
import com.mackenziehigh.sexpr.SAtom;
import com.mackenziehigh.sexpr.SList;
import com.mackenziehigh.sexpr.Sexpr;
import com.mackenziehigh.sexpr.SexprSchema;
import com.mackenziehigh.sexpr.annotations.After;
import com.mackenziehigh.sexpr.annotations.Before;
import com.mackenziehigh.sexpr.annotations.Pass;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class is used to create and configure the controller,
 * message-processor(s), message-queue(s), and modules.
 */
final class Configurator
{
    private final StandardController controller = new StandardController();

    private final Map<String, String> imports = new HashMap<>();

    private AbstractProcessor processor;

    private AbstractModule module;

    private final List<Sexpr> moduleOptions = new LinkedList<>();

    @Pass ("ECHO_PASS")
    @Before ("echo")
    public void visit_echo (final SList node)
    {
        System.out.println(node.get(1).toAtom().content());
    }

    @Pass ("ECHO_PASS")
    @Before ("import")
    public void visit_import (final SList node)
    {
        final String key = node.get(1).toAtom().content();
        final String value = node.get(3).toAtom().content();
        imports.put(key, value);
    }

    @Pass ("CREATE_PROCESSORS_PASS")
    @Before ("direct_processor")
    public void visit_direct_processor_before (final SList node)
    {
        processor = new DirectProcessor();
        processor.controller = controller;
    }

    @Pass ("CREATE_PROCESSORS_PASS")
    @After ("direct_processor")
    public void visit_direct_processor_after (final SList node)
    {
        final String sourceName = processor.name;
        final UniqueID sourceID = processor.uniqueID;
        processor.logger = new CommonLogger(controller.globalLogQueue(), sourceName, sourceID);
    }

    @Pass ("CREATE_PROCESSORS_PASS")
    @Before ("spawning_processor")
    public void visit_spawning_processor_before (final SList node)
    {
        processor = new SpawningProcessor();
        processor.controller = controller;
    }

    @Pass ("CREATE_PROCESSORS_PASS")
    @After ("spawning_processor")
    public void visit_spawning_processor_after (final SList node)
    {
        final String sourceName = processor.name;
        final UniqueID sourceID = processor.uniqueID;
        processor.logger = new CommonLogger(controller.globalLogQueue(), sourceName, sourceID);
    }

    @Pass ("CREATE_PROCESSORS_PASS")
    @Before ("spawning_thread_max")
    public void visit_spawning_thread_max (final SList node)
    {
        final SpawningProcessor p = (SpawningProcessor) processor;
        p.threadMax = node.get(4).toAtom().asInt().get();
    }

    @Pass ("CREATE_PROCESSORS_PASS")
    @Before ("spawning_thread_min")
    public void visit_spawning_thread_min (final SList node)
    {
        final SpawningProcessor p = (SpawningProcessor) processor;
        p.threadMin = node.get(4).toAtom().asInt().get();
    }

    @Pass ("CREATE_PROCESSORS_PASS")
    @Before ("spawning_thread_name")
    public void visit_spawning_thread_name (final SList node)
    {
        final SpawningProcessor p = (SpawningProcessor) processor;
        p.threadName = node.get(3).toAtom().content();
    }

    @Pass ("CREATE_PROCESSORS_PASS")
    @Before ("spawning_thread_priority")
    public void visit_spawning_thread_priority (final SList node)
    {
        final SpawningProcessor p = (SpawningProcessor) processor;
        p.threadPriority = node.get(3).toAtom().asInt().get();
    }

    @Pass ("CREATE_PROCESSORS_PASS")
    @Before ("spawning_capacity")
    public void visit_spawning_capacity (final SList node)
    {
        final SpawningProcessor p = (SpawningProcessor) processor;
        p.capacity = node.get(2).toAtom().asInt().get();
    }

    @Pass ("CREATE_PROCESSORS_PASS")
    @Before ("spawning_overflow")
    public void visit_spawning_overflow (final SList node)
    {
        final SpawningProcessor p = (SpawningProcessor) processor;
        final String name = node.get(3).toAtom().content();
        p.overflowQueue = new LazyQueueRef(controller, name);
    }

    @Pass ("CREATE_PROCESSORS_PASS")
    @Before ("dedicated_processor")
    public void visit_dedicated_processor_before (final SList node)
    {
        processor = new DedicatedProcessor();
        processor.controller = controller;
    }

    @Pass ("CREATE_PROCESSORS_PASS")
    @After ("dedicated_processor")
    public void visit_dedicated_processor_after (final SList node)
    {
        final String sourceName = processor.name;
        final UniqueID sourceID = processor.uniqueID;
        processor.logger = new CommonLogger(controller.globalLogQueue(), sourceName, sourceID);
    }

    @Pass ("CREATE_PROCESSORS_PASS")
    @Before ("dedicated_thread_count")
    public void visit_dedicated_thread_count (final SList node)
    {
        final DedicatedProcessor p = (DedicatedProcessor) processor;
        p.threadCount = node.get(3).toAtom().asInt().get();
    }

    @Pass ("CREATE_PROCESSORS_PASS")
    @Before ("dedicated_thread_name")
    public void visit_dedicated_thread_name (final SList node)
    {
        final DedicatedProcessor p = (DedicatedProcessor) processor;
        p.threadName = node.get(3).toAtom().content();
    }

    @Pass ("CREATE_PROCESSORS_PASS")
    @Before ("dedicated_thread_priority")
    public void visit_dedicated_thread_priority (final SList node)
    {
        final DedicatedProcessor p = (DedicatedProcessor) processor;
        p.threadPriority = node.get(3).toAtom().asInt().get();
    }

    @Pass ("CREATE_PROCESSORS_PASS")
    @Before ("dedicated_capacity")
    public void visit_dedicated_capacity (final SList node)
    {
        final DedicatedProcessor p = (DedicatedProcessor) processor;
        final int capacity = node.get(2).toAtom().asInt().get();
        p.messages = new BiArrayBlockingQueue<>(capacity);
    }

    @Pass ("CREATE_PROCESSORS_PASS")
    @Before ("dedicated_overflow")
    public void visit_dedicated_overflow (final SList node)
    {
        final DedicatedProcessor p = (DedicatedProcessor) processor;
        final String name = node.get(3).toAtom().content();
        p.overflowQueue = new LazyQueueRef(controller, name);
    }

    @Pass ("CREATE_MODULES_PASS")
    @Before ("module")
    public void visit_module_before (final SList node)
    {
        moduleOptions.clear();

        final String name = node.get(1).toAtom().content();
        final String type = node.get(3).toAtom().content();
        final String klass = imports.containsKey(type) ? imports.get(type) : type;

        try
        {
            final Class clazz = Class.forName(klass);
            module = (AbstractModule) clazz.newInstance();
            module.assignController(controller);
            module.assignName(name);
            controller.modules.put(name, module);
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @Pass ("CREATE_MODULES_PASS")
    @After ("module")
    public void visit_module_after (final SList node)
    {
        final SList configuration = SList.copyOf(moduleOptions);
        module.assignConfiguration(configuration);
        final String sourceName = module.name();
        final UniqueID sourceID = module.uniqueID();
        final CommonLogger logger = new CommonLogger(controller.globalLogQueue(), sourceName, sourceID);
        module.assignLogger(logger);
    }

    @Pass ("CREATE_MODULES_PASS")
    @Before ("module_option")
    public void visit_custom_option (final Sexpr node)
    {
        moduleOptions.add(node);
    }

    @Pass ("CREATE_SETTINGS_PASS")
    @Before ("set")
    public void visit_set (final SList node)
    {
        final String key = node.get(1).toAtom().content();
        final Sexpr value = node.get(3);
        controller.settings.put(key, value);
    }

    @Pass ("CREATE_PROCESSORS_PASS")
    @Before ("processor_name")
    public void visit_processor_name (final SAtom node)
    {
        final String name = node.content();
        controller.processors.put(name, processor);
    }

    @Pass ("CREATE_PROCESSORS_PASS")
    @Before ("queue")
    public void visit_queue (final SList node)
    {
        final String name = node.get(1).toAtom().content();
        processor.declareQueue(name);
    }

    public StandardController load (final SList config)
            throws IOException
    {
        /**
         * Load the schema.
         */
        final SexprSchema grammar = SexprSchema.fromResource("/com/mackenziehigh/loader/internal/Grammar.txt")
                .pass("ECHO_PASS")
                .pass("CREATE_PROCESSORS_PASS")
                .pass("CREATE_MODULES_PASS")
                .pass("CREATE_SETTINGS_PASS")
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
