package org.opentrafficsim.spatialtree;

import java.util.Set;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.HierarchicalType;
import org.opentrafficsim.base.HierarchicallyTyped;
import org.opentrafficsim.core.DynamicSpatialObject;
import org.opentrafficsim.core.SpatialObject;
import org.opentrafficsim.core.geometry.OtsShape;

/**
 * SpatialTree.java.
 * <p>
 * Copyright (c) 2022-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface SpatialTree
{
    /**
     * Store a spatial object in the spatial tree.
     * @param <T> the hierarchical type of the spatial object
     * @param <I> the spatial object type we are adding
     * @param object I; the object to store in the tree
     */
    <T extends HierarchicalType<T, I>, I extends HierarchicallyTyped<T, I> & SpatialObject> void add(I object);

    /**
     * Remove a spatial object from the spatial tree.
     * @param <T> the hierarchical type of the spatial object
     * @param <I> the spatial object type we are adding
     * @param object I; the object to remove from the tree
     * @return boolean; whether the object was present in the tree or not
     */
    <T extends HierarchicalType<T, I>, I extends HierarchicallyTyped<T, I> & SpatialObject> boolean remove(I object);

    /**
     * Return all objects with the right type (or subtype) and class (or subclass) that have an overlap with the given shape.
     * @param <T> the hierarchical type of the spatial object
     * @param <I> the spatial object type we are looking for
     * @param type T; the type we are looking for (subtypes also qualify)
     * @param shape OtsShape; the search area bounded by a polygon
     * @param searchClass Class&lt;I&gt;; the class we are looking for (subclasses also qualify)
     * @return Set&lt;I&gt;; the set of spatial objects that have an overlap with the given shape
     */
    <T extends HierarchicalType<T, I>, I extends HierarchicallyTyped<T, I> & SpatialObject> Set<I> find(T type, OtsShape shape,
            Class<I> searchClass);

    /**
     * Return all dynamic objects with the right type (or subtype) and class (or subclass) that have an overlap with the given
     * shape at the given time.
     * @param <T> the hierarchical type of the spatial object
     * @param <I> the dynamic spatial object type we are looking for
     * @param type T; the type we are looking for (subtypes also qualify)
     * @param shape OtsShape; the search area bounded by a polygon
     * @param searchClass Class&lt;I&gt;; the class we are looking for (subclasses also qualify)
     * @param time Time; the time for which we need to evaluate the positions of the dynamic objects
     * @return Set&lt;I&gt;; the set of dynamic spatial objects that have an overlap with the given shape at the given time
     */
    <T extends HierarchicalType<T, I>, I extends HierarchicallyTyped<T, I> & DynamicSpatialObject> Set<I> find(T type,
            OtsShape shape, Class<I> searchClass, Time time);

}
