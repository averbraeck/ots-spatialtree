package org.opentrafficsim.spatialtree.rtree2;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.HierarchicalType;
import org.opentrafficsim.base.HierarchicallyTyped;
import org.opentrafficsim.core.DynamicSpatialObject;
import org.opentrafficsim.core.SpatialObject;
import org.opentrafficsim.core.geometry.Bounds;
import org.opentrafficsim.core.geometry.OtsShape;
import org.opentrafficsim.spatialtree.SpatialTree;

import com.github.davidmoten.rtree2.Entry;
import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.geometry.Rectangle;

/**
 * David Moten's rtree2, see https://github.com/davidmoten/rtree2.
 * <p>
 * Copyright (c) 2022-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class SpatialTreeRTree2 implements SpatialTree
{
    /** the tree object. */
    private RTree<SpatialObject, Geometry> tree;

    /** the bounding boxes of the dynamic objects at the time of insertion. */
    final Map<SpatialObject, Geometry> bboxMap = new LinkedHashMap<>();

    /**
     * Constructor; initialize the spatial index.
     */
    public SpatialTreeRTree2()
    {
        this.tree = RTree.create();
    }

    /** {@inheritDoc} */
    @Override
    public <T extends HierarchicalType<T, I>, I extends HierarchicallyTyped<T, I> & SpatialObject> void add(final I object)
    {
        Bounds bb = object.getShape().getBounds();
        Geometry geometry = Geometries.rectangle(bb.getMinX(), bb.getMinY(), bb.getMaxX(), bb.getMaxY());
        this.bboxMap.put(object, geometry);
        this.tree = this.tree.add(object, geometry); // note: tree is immutable; every add returns a copy (!)
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unlikely-arg-type")
    public <T extends HierarchicalType<T, I>,
            I extends HierarchicallyTyped<T, I> & SpatialObject> boolean remove(final I object)
    {
        Geometry geometry = this.bboxMap.get(object);
        if (geometry != null)
        {
            this.tree = this.tree.delete(object, geometry); // note: tree is immutable; every delete returns a copy (!)
            this.bboxMap.remove(object);
            return true;
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public <T extends HierarchicalType<T, I>, I extends HierarchicallyTyped<T, I> & SpatialObject> Set<I> find(final T type,
            final OtsShape shape, final Class<I> searchClass)
    {
        Throw.whenNull(shape, "shape in find cannot be null");
        Throw.whenNull(searchClass, "searchClass in find cannot be null");
        Bounds bb = shape.getBounds();
        Rectangle rectangle = Geometries.rectangle(bb.getMinX(), bb.getMinY(), bb.getMaxX(), bb.getMaxY());
        final Set<I> returnSet = new LinkedHashSet<>();
        Iterable<Entry<SpatialObject, Geometry>> results = this.tree.search(rectangle);
        for (Entry<SpatialObject, Geometry> item : results)
        {
            SpatialObject so = item.value();
            if (searchClass.isAssignableFrom(so.getClass()))
            {
                @SuppressWarnings("unchecked")
                I cso = (I) so;
                if (type == null || cso.isOfType(type))
                {
                    if (so.getShape().intersects(shape))
                        returnSet.add(cso);
                }
            }
        }
        return returnSet;
    }

    /** {@inheritDoc} */
    @Override
    public <T extends HierarchicalType<T, I>, I extends HierarchicallyTyped<T, I> & DynamicSpatialObject> Set<I> find(
            final T type, final OtsShape shape, final Class<I> searchClass, final Time time)
    {
        Throw.whenNull(shape, "shape in find cannot be null");
        Throw.whenNull(searchClass, "searchClass in find cannot be null");
        Bounds bb = shape.getBounds();
        Rectangle rectangle = Geometries.rectangle(bb.getMinX(), bb.getMinY(), bb.getMaxX(), bb.getMaxY());
        final Set<I> returnSet = new LinkedHashSet<>();
        Iterable<Entry<SpatialObject, Geometry>> results = this.tree.search(rectangle);
        for (Entry<SpatialObject, Geometry> item : results)
        {
            SpatialObject so = item.value();
            if (searchClass.isAssignableFrom(so.getClass()))
            {
                @SuppressWarnings("unchecked")
                I dso = (I) so;
                if (type == null || dso.isOfType(type))
                {
                    if (so.getShape().intersects(shape))
                        returnSet.add(dso);
                }
            }
        }
        return returnSet;
    }

}
