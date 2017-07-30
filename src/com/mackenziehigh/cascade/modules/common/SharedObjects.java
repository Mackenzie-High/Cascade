package com.mackenziehigh.cascade.modules.common;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import com.mackenziehigh.cascade.AbstractModule;

/**
 *
 * @author mackenzie
 */
public final class SharedObjects
        implements AbstractModule
{
    private final Map<String, Object> map = new ConcurrentHashMap<>();

    /**
     * This method adds an object that will be global to all modules.
     *
     * @param key is the fully-qualified name that will be used to identify the object.
     * @param value is the object itself.
     * @throws IllegalStateException if the key already is in-use.
     */
    public void bind (String key,
                      Object value)
    {
        map.put(key, value);
    }

    /**
     * This method retrieves a global object given the key that identifies the object.
     *
     * @param key identifies the object to return.
     * @return the desired object, if the object exists.
     */
    public Optional<Object> get (String key)
    {
        return map.containsKey(key) ? Optional.of(map.get(key)) : Optional.empty();
    }

}
