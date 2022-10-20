package org.opentrafficsim.spatialtree.jts;

import java.util.LinkedHashSet;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.ItemVisitor;
import org.locationtech.jts.index.hprtree.HPRtree;
import org.opentrafficsim.base.HierarchicalType;
import org.opentrafficsim.base.HierarchicallyTyped;
import org.opentrafficsim.core.geometry.Bounds;
import org.opentrafficsim.core.geometry.OtsShape;
import org.opentrafficsim.spatialtree.DynamicSpatialObject;
import org.opentrafficsim.spatialtree.SpatialObject;
import org.opentrafficsim.spatialtree.SpatialTree;

/**
 * SpatialTreeJtsHprTree.java.
 * <p>
 * Copyright (c) 2022-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class SpatialTreeJtsHprTree implements SpatialTree
{
    /** the tree object. */
    final HPRtree tree;

    /**
     * Constructor; initialize the spatial index.
     */
    public SpatialTreeJtsHprTree()
    {
        this.tree = new HPRtree();
    }

    /** {@inheritDoc} */
    @Override
    public void put(final SpatialObject<?, ?> object)
    {
        Bounds bb = object.getShape().getBounds();
        Envelope envelope = new Envelope(bb.getMinX(), bb.getMinY(), bb.getMaxX(), bb.getMaxY());
        this.tree.insert(envelope, object);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends HierarchicalType<T, I>, I extends HierarchicallyTyped<T, I>,
            C extends SpatialObject<T, I>> Set<C> find(final T type, final OtsShape shape, final Class<C> searchClass)
    {
        Throw.whenNull(shape, "shape in find cannot be null");
        Throw.whenNull(searchClass, "searchClass in find cannot be null");
        Bounds bb = shape.getBounds();
        Envelope searchEnv = new Envelope(bb.getMinX(), bb.getMinY(), bb.getMaxX(), bb.getMaxY());
        final Set<C> returnSet = new LinkedHashSet<>();
        this.tree.query(searchEnv, new ItemVisitor()
        {
            @Override
            public void visitItem(final Object item)
            {
                SpatialObject<?, ?> so = (SpatialObject<?, ?>) item;
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
        });
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
        Envelope searchEnv = new Envelope(bb.getMinX(), bb.getMinY(), bb.getMaxX(), bb.getMaxY());
        final Set<D> returnSet = new LinkedHashSet<>();
        this.tree.query(searchEnv, new ItemVisitor()
        {
            @Override
            public void visitItem(final Object item)
            {
                SpatialObject<?, ?> so = (SpatialObject<?, ?>) item;
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
        });
        return returnSet;
    }

}
