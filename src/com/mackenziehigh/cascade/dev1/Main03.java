package com.mackenziehigh.cascade.dev1;

import com.google.common.collect.Lists;
import com.mackenziehigh.cascade.Cascade;
import com.mackenziehigh.cascade.Cascade.Stage;
import java.util.List;

/**
 *
 */
public class Main03
{
    public static void main (String[] args)
    {
        final Stage stage = Cascade.newStage();

        final List<String> list = Lists.newArrayList("Emma", "Erin", "Molly");

        list.forEach(stage.newActor().withScript((String x) -> System.out.println("X = " + x)).create());
    }
}
