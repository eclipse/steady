package org.apache.commons.collections4.multimap;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.SetValuedMap;

/**
 * Implements a {@link SetValuedMap}, using a {@link HashMap} to provide data
 * storage and {@link HashSet}s as value collections. This is the standard
 * implementation of a SetValuedMap.
 * <p>
 * <strong>Note that HashSetValuedHashMap is not synchronized and is not
 * thread-safe.</strong> If you wish to use this map from multiple threads
 * concurrently, you must use appropriate synchronization. This class may throw
 * exceptions when accessed by concurrent threads without synchronization.
 *
 * @since 4.1
 * @version $Id$
 */
public class HashSetValuedHashMap<K, V> extends AbstractSetValuedMap<K, V>
    implements Serializable {

    /** Serialization Version */
    private static final long serialVersionUID = 20151118L;

    /**
     * The initial map capacity used when none specified in constructor.
     */
    private static final int DEFAULT_INITIAL_MAP_CAPACITY = 16;

    /**
     * The initial set capacity when using none specified in constructor.
     */
    private static final int DEFAULT_INITIAL_SET_CAPACITY = 3;

    /**
     * The initial list capacity when creating a new value collection.
     */
    private final int initialSetCapacity;

    /**
     * Creates an empty HashSetValuedHashMap with the default initial
     * map capacity (16) and the default initial set capacity (3). 
     */
    public HashSetValuedHashMap() {
        this(DEFAULT_INITIAL_MAP_CAPACITY, DEFAULT_INITIAL_SET_CAPACITY);
    }

    /**
     * Creates an empty HashSetValuedHashMap with the default initial
     * map capacity (16) and the specified initial set capacity. 
     *
     * @param initialSetCapacity  the initial capacity used for value collections
     */
    public HashSetValuedHashMap(int initialSetCapacity) {
        this(DEFAULT_INITIAL_MAP_CAPACITY, initialSetCapacity);
    }

    /**
     * Creates an empty HashSetValuedHashMap with the specified initial
     * map and list capacities. 
     *
     * @param initialMapCapacity  the initial hashmap capacity
     * @param initialSetCapacity  the initial capacity used for value collections
     */
    public HashSetValuedHashMap(int initialMapCapacity, int initialSetCapacity) {
        super(new HashMap<K, HashSet<V>>(initialMapCapacity));
        this.initialSetCapacity = initialSetCapacity;
    }

    /**
     * Creates an HashSetValuedHashMap copying all the mappings of the given map.
     *
     * @param map a <code>MultiValuedMap</code> to copy into this map
     */
    public HashSetValuedHashMap(final MultiValuedMap<? extends K, ? extends V> map) {
        this(map.size(), DEFAULT_INITIAL_SET_CAPACITY);
        super.putAll(map);
    }

    /**
     * Creates an HashSetValuedHashMap copying all the mappings of the given map.
     *
     * @param map a <code>Map</code> to copy into this map
     */
    public HashSetValuedHashMap(final Map<? extends K, ? extends V> map) {
        this(map.size(), DEFAULT_INITIAL_SET_CAPACITY);
        super.putAll(map);
    }

    // -----------------------------------------------------------------------
    @Override
    protected HashSet<V> createCollection() {
        return new HashSet<V>(initialSetCapacity);
    }

    // -----------------------------------------------------------------------
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        doWriteObject(oos);
    }
    
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        setMap(new HashMap<K, HashSet<V>>());
        doReadObject(ois);
    }

}
