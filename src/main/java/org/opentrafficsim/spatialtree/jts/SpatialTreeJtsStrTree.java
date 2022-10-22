package org.opentrafficsim.spatialtree.jts;

import java.util.LinkedHashSet;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.ItemVisitor;
import org.locationtech.jts.index.strtree.STRtree;
import org.opentrafficsim.base.HierarchicalType;
import org.opentrafficsim.base.HierarchicallyTyped;
import org.opentrafficsim.core.DynamicSpatialObject;
import org.opentrafficsim.core.SpatialObject;
import org.opentrafficsim.core.geometry.Bounds;
import org.opentrafficsim.core.geometry.OtsShape;
import org.opentrafficsim.spatialtree.SpatialTree;

/**
 * SpatialTreeJtsStrTree.java.
 * <p>
 * Copyright (c) 2022-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class SpatialTreeJtsStrTree implements SpatialTree
{
    /** the tree object. */
    final STRtree tree;

    /**
     * Constructor; initialize the spatial index.
     */
    public SpatialTreeJtsStrTree()
    {
        this.tree = new STRtree();
    }

    /** {@inheritDoc} */
    @Override
    public <T extends HierarchicalType<T, I>, I extends HierarchicallyTyped<T, I> & SpatialObject> void put(final I object)
    {
        Bounds bb = object.getShape().getBounds();
        Envelope envelope = new Envelope(bb.getMinX(), bb.getMinY(), bb.getMaxX(), bb.getMaxY());
        this.tree.insert(envelope, object);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends HierarchicalType<T, I>, I extends HierarchicallyTyped<T, I> & SpatialObject> Set<I> find(final T type,
            final OtsShape shape, final Class<I> searchClass)
    {
        Throw.whenNull(shape, "shape in find cannot be null");
        Throw.whenNull(searchClass, "searchClass in find cannot be null");
        Bounds bb = shape.getBounds();
        Envelope searchEnv = new Envelope(bb.getMinX(), bb.getMinY(), bb.getMaxX(), bb.getMaxY());
        final Set<I> returnSet = new LinkedHashSet<>();
        this.tree.query(searchEnv, new ItemVisitor()
        {
            @Override
            public void visitItem(final Object item)
            {
                SpatialObject so = (SpatialObject) item;
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
        });
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
        Envelope searchEnv = new Envelope(bb.getMinX(), bb.getMinY(), bb.getMaxX(), bb.getMaxY());
        final Set<I> returnSet = new LinkedHashSet<>();
        this.tree.query(searchEnv, new ItemVisitor()
        {
            @Override
            public void visitItem(final Object item)
            {
                SpatialObject so = (SpatialObject) item;
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
        });
        return returnSet;
    }

}
