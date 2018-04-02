package com.mackenziehigh.cascade.scripts;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mackenziehigh.cascade.CascadeContext;
import com.mackenziehigh.cascade.CascadeScript;
import com.mackenziehigh.cascade.CascadeStack;
import com.mackenziehigh.cascade.CascadeToken;
import com.mackenziehigh.cascade.scripts.LambdaScript.SetupFunction;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 *
 */
public abstract class AnnotatedScript
        implements CascadeScript
{
    private final ImmutableMap<CascadeToken, Method> setupMethods;

    private final ImmutableMap<CascadeToken, Method> messageMethods;

    private final ImmutableMap<CascadeToken, Method> closeMethods;

    private final LambdaScript.Builder functorScript = LambdaScript.newBuilder();

    public AnnotatedScript ()
    {
        setupMethods = ImmutableMap.copyOf(enumerateHandlers(OnSetup.class, x -> ((OnSetup) x).value()));
        messageMethods = ImmutableMap.copyOf(enumerateHandlers(OnMessage.class, x -> ((OnMessage) x).value()));
        closeMethods = ImmutableMap.copyOf(enumerateHandlers(OnClose.class, x -> ((OnClose) x).value()));
    }

    private Map<CascadeToken, Method> enumerateHandlers (final Class annotationType,
                                                         final Function<Object, String> extractor)
    {
        final Map<CascadeToken, Method> result = Maps.newHashMap();

        for (Method method : enumerateMethods())
        {
            if (method.isAnnotationPresent(annotationType))
            {
                final Annotation anno = method.getAnnotation(annotationType);
                final String explicitName = extractor.apply(anno);
                final String implicitName = method.getName();
                final String name = explicitName.trim().isEmpty() ? implicitName : explicitName.trim();
                final CascadeToken token = CascadeToken.token(name);
                result.put(token, method);
            }
        }

        return result;
    }

    private Set<Method> enumerateMethods ()
    {
        final Set<Class> classes = Sets.newHashSet();

        enumerateSuperTypes(classes, getClass());

        final Set<Method> methods = Sets.newHashSet();

        for (Class klass : classes)
        {
            methods.addAll(Arrays.asList(klass.getDeclaredMethods()));
        }

        return methods;
    }

    private void enumerateSuperTypes (final Set<Class> out,
                                      final Class klass)
    {
        if (klass == null || out.contains(klass))
        {
            return;
        }

        out.add(klass);

        if (klass.getSuperclass() != null)
        {
            enumerateSuperTypes(out, klass.getSuperclass());
        }

        for (Class face : klass.getInterfaces())
        {
            enumerateSuperTypes(out, face);
        }
    }

    private SetupFunction createHandler (final Object owner,
                                         final Method method)
    {
        final MethodHandles.Lookup lookup = MethodHandles.lookup();

        final MethodHandle handle;

        try
        {
            handle = lookup.findVirtual(owner.getClass(),
                                        method.getName(),
                                        MethodType.methodType(method.getReturnType(), method.getParameterTypes()[0]))
                    .bindTo(owner);
        }
        catch (NoSuchMethodException | IllegalAccessException ex)
        {
            throw new RuntimeException(ex);
        }

        final SetupFunction handler = (ctx) -> handle.invoke(ctx);

        return handler;
    }

    public Map<CascadeToken, Method> getSetupMethods ()
    {
        return setupMethods;
    }

    public Map<CascadeToken, Method> getMessageMethods ()
    {
        return messageMethods;
    }

    public Map<CascadeToken, Method> getCloseMethods ()
    {
        return closeMethods;
    }

    // Rename to subscribe. Must be able to work after creation!
    public AnnotatedScript subscribe (final String method,
                                      final String event)
    {
        return subscribe(method, CascadeToken.token(event));
    }

    public AnnotatedScript subscribe (final String method,
                                      final CascadeToken event)
    {
        final CascadeToken methodId = CascadeToken.token(method);

//        if (handlers.containsKey(methodId) == false)
//        {
//            throw new IllegalArgumentException("No Such Method: " + method);
//        }
//        else
//        {
//            return this;
//        }
        return this;
    }

    public AnnotatedScript unsubscribe (final String method,
                                        final String event)
    {
        return unsubscribe(method, CascadeToken.token(event));
    }

    public AnnotatedScript unsubscribe (final String method,
                                        final CascadeToken event)
    {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void onClose (CascadeContext ctx)
            throws Throwable
    {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void onMessage (CascadeContext ctx,
                                 CascadeToken event,
                                 CascadeStack stack)
            throws Throwable
    {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void onSetup (CascadeContext ctx)
            throws Throwable
    {

    }

    public static void main (String[] args)
    {
        final AnnotatedScript script = new AnnotatedScript()
        {
            @OnMessage
            public void onKiss ()
            {

            }
        };

    }

}
