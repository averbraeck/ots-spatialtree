package org.opentrafficsim.spatialtree.rtree2;

import java.util.LinkedHashSet;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.HierarchicalType;
import org.opentrafficsim.base.HierarchicallyTyped;
import org.opentrafficsim.core.geometry.Bounds;
import org.opentrafficsim.core.geometry.OtsShape;
import org.opentrafficsim.spatialtree.DynamicSpatialObject;
import org.opentrafficsim.spatialtree.SpatialObject;
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
    private RTree<SpatialObject<?, ?>, Geometry> tree;

    /**
     * Constructor; initialize the spatial index.
     */
    public SpatialTreeRTree2()
    {
        this.tree = RTree.create();
    }

    /** {@inheritDoc} */
    @Override
    public void put(final SpatialObject<?, ?> object)
    {
        Bounds bb = object.getShape().getBounds();
        Geometry geometry = Geometries.rectangle(bb.getMinX(), bb.getMinY(), bb.getMaxX(), bb.getMaxY());
        this.tree = this.tree.add(object, geometry); // note: tree is immutable; every add returns a copy (!)
    }

    /** {@inheritDoc} */
    @Override
    public <T extends HierarchicalType<T, I>, I extends HierarchicallyTyped<T, I>,
            C extends SpatialObject<T, I>> Set<C> find(final T type, final OtsShape shape, final Class<C> searchClass)
    {
        Throw.whenNull(shape, "shape in find cannot be null");
        Throw.whenNull(searchClass, "searchClass in find cannot be null");
        Bounds bb = shape.getBounds();
        Rectangle rectangle = Geometries.rectangle(bb.getMinX(), bb.getMinY(), bb.getMaxX(), bb.getMaxY());
        final Set<C> returnSet = new LinkedHashSet<>();
        Iterable<Entry<SpatialObject<?, ?>, Geometry>> results = this.tree.search(rectangle);
        for (Entry<SpatialObject<?, ?>, Geometry> item : results)
        {
            SpatialObject<?, ?> so = item.value();
            if (searchClass.isAssignableFrom(so.getClass()))
            {
                @SuppressWarnings("unchecked")
                C cso = (C) so;
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
    public <T extends HierarchicalType<T, I>, I extends HierarchicallyTyped<T, I>,
            D extends DynamicSpatialObject<T, I>> Set<D> find(final T type, final OtsShape shape, final Class<D> searchClass,
                    final Time time)
    {
        Throw.whenNull(shape, "shape in find cannot be null");
        Throw.whenNull(searchClass, "searchClass in find cannot be null");
        Bounds bb = shape.getBounds();
        Rectangle rectangle = Geometries.rectangle(bb.getMinX(), bb.getMinY(), bb.getMaxX(), bb.getMaxY());
        final Set<D> returnSet = new LinkedHashSet<>();
        Iterable<Entry<SpatialObject<?, ?>, Geometry>> results = this.tree.search(rectangle);
        for (Entry<SpatialObject<?, ?>, Geometry> item : results)
        {
            SpatialObject<?, ?> so = item.value();
            if (searchClass.isAssignableFrom(so.getClass()))
            {
                @SuppressWarnings("unchecked")
                D dso = (D) so;
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
