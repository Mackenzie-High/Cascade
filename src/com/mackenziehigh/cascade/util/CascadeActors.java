package com.mackenziehigh.cascade.util;

import com.mackenziehigh.cascade.CascadeActor;
import com.mackenziehigh.cascade.CascadeLogger;
import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeStage;
import com.mackenziehigh.cascade.CascadeToken;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Creates special actors.
 */
public interface CascadeActors
{
    public CascadeStage cascade ();

    public CascadeStage stage ();

    public <E> CascadeActor of (CascadeToken output,
                                E values);

    public <E> CascadeActor iter (CascadeToken output,
                                  Iterable<E> values);

    public <E> CascadeActor iter (CascadeToken output,
                                  Iterator<E> values);

    public CascadeActor merge (CascadeToken output,
                               CascadeActor... inputs);

    public CascadeActor balance (CascadeToken input,
                                 CascadeToken... outputs);

    public CascadeActor limit (CascadeToken input,
                               CascadeToken output,
                               int limit);

    public CascadeActor rateLimit (CascadeToken input,
                                   CascadeToken output,
                                   int permitsPerSecond);

    public CascadeActor tallyLimit (CascadeToken input,
                                    CascadeToken output,
                                    int skipNum,
                                    int skipDenom);

    public CascadeActor valve (CascadeToken output,
                               CascadeToken input,
                               CascadeToken condition);

    public CascadeActor valve (CascadeToken output,
                               CascadeToken input,
                               Predicate<CascadeStack> condition);

    public CascadeActor variable (CascadeToken output,
                                  CascadeToken input,
                                  CascadeToken clock);

    public CascadeActor funnel (CascadeToken output,
                                CascadeToken... inputs);

    public CascadeActor fanout (CascadeToken input,
                                CascadeToken outputs);

    public CascadeActor switcher (CascadeToken condition,
                                  CascadeToken input,
                                  CascadeToken outputTrue,
                                  CascadeToken outputFalse);

    public CascadeActor map (CascadeToken input,
                             CascadeToken output,
                             Consumer<CascadeStack> action);

    public CascadeActor filter (CascadeToken input,
                                CascadeToken output,
                                Predicate<CascadeStack> condition);

    public CascadeActor select (CascadeToken input,
                                CascadeToken outputTrue,
                                CascadeToken outputFalse,
                                Predicate<CascadeStack> condition);

    public CascadeActor require (CascadeToken input,
                                 Predicate<CascadeStack> condition);

    public CascadeActor require (CascadeToken input,
                                 CascadeToken output,
                                 Predicate<CascadeStack> condition);

    public CascadeActor forEach (CascadeToken input,
                                 Consumer<CascadeStack> action);

    public CascadeActor clock (CascadeToken output,
                               long period,
                               TimeUnit periodUnit);

    public CascadeActor cron (CascadeToken output,
                              String cron);

    public CascadeActor pulsar (CascadeToken output,
                                CascadeToken period,
                                TimeUnit periodUnit);

    public CascadeActor sum (CascadeToken input,
                             CascadeToken output);

    public CascadeActor count (CascadeToken input,
                               CascadeToken output);

    public CascadeActor and (CascadeToken output,
                             CascadeToken... input);

    public CascadeActor or (CascadeToken output,
                            CascadeToken... input);

    public CascadeActor not (CascadeToken output,
                             CascadeToken input);

    public CascadeActor dup (CascadeToken input,
                             CascadeToken output);

    public CascadeActor pop (CascadeToken input,
                             CascadeToken output,
                             int count);

    public CascadeActor swap (CascadeToken input,
                              CascadeToken output);

    public CascadeActor repeat (CascadeToken input,
                                CascadeToken output,
                                int count);

    public CascadeActor once (CascadeToken input,
                              Consumer<CascadeStack> stack);

    public CascadeActor distinct (CascadeToken input,
                                  CascadeToken output);

    public CascadeActor distinct (CascadeToken input,
                                  CascadeToken output,
                                  int memoryLimit);

    public CascadeActor memoize (CascadeToken input,
                                 CascadeToken output,
                                 int memoryLimit);

    /**
     * Atomically save/restore a string from a file. Used as a type of variable.
     *
     * @param input
     * @param output
     * @param file
     * @return
     */
    public CascadeActor strfile (CascadeToken input,
                                 CascadeToken output,
                                 File file);

    public CascadeActor binfile (CascadeToken input,
                                 CascadeToken output,
                                 File file);

    /**
     * Primitive Conversions TODO.
     *
     * @param input
     * @param output
     * @return
     */
    public CascadeActor convert (CascadeToken input,
                                 CascadeToken output);

    /**
     * Top operand is function name.
     *
     * @param input
     * @param output
     * @param lookup
     * @return
     */
    public CascadeActor funcall (CascadeToken input,
                                 CascadeToken output,
                                 BiConsumer<String, CascadeStack> lookup);

    public CascadeActor subscribe (CascadeToken input,
                                   Consumer<CascadeStack> subscriber);

    public CascadeActor subscribeUnary (CascadeToken input,
                                        Consumer<Object> subscriber);

    public CascadeActor subscribeUnary (CascadeToken input,
                                        BiConsumer<Object, Object> subscriber);

    public CascadeActor logAll (CascadeToken input,
                                CascadeLogger logger);

    public List<CascadeToken> take (CascadeToken input,
                                    int count);

    public List<CascadeToken> takeTop (CascadeToken input,
                                       int count);

    public Future<CascadeToken> future (CascadeToken input);

    public Future<CascadeToken> futureTop (CascadeToken input);

}
