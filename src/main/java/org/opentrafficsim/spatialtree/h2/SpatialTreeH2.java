package org.opentrafficsim.spatialtree.h2;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.h2.mvstore.db.SpatialKey;
import org.h2.mvstore.rtree.MVRTreeMap;
import org.h2.mvstore.rtree.MVRTreeMap.RTreeCursor;
import org.h2.mvstore.rtree.Spatial;
import org.h2.mvstore.rtree.SpatialDataType;
import org.h2.mvstore.type.ObjectDataType;
import org.opentrafficsim.base.HierarchicalType;
import org.opentrafficsim.base.HierarchicallyTyped;
import org.opentrafficsim.core.geometry.Bounds;
import org.opentrafficsim.core.geometry.OtsShape;
import org.opentrafficsim.spatialtree.DynamicSpatialObject;
import org.opentrafficsim.spatialtree.SpatialObject;
import org.opentrafficsim.spatialtree.SpatialTree;

/**
 * SpatialTreeH2 based on MVRTreeMap.
 * <p>
 * Copyright (c) 2022-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class SpatialTreeH2 implements SpatialTree
{
    /** the tree object. */
    private final MVRTreeMap<Object> tree;

    /** object counter. */
    private long counter = 1L; // 0 reserved for search key

    /**
     * Constructor; initialize the spatial index.
     */
    public SpatialTreeH2()
    {
        Map<String, Object> config = new LinkedHashMap<>();
        SpatialDataType keyType = new SpatialDataType(2); // 2 dimensions
        ObjectDataType valueType = new ObjectDataType(); // unfortunately cannot specify exact object type
        this.tree = new MVRTreeMap<Object>(config, keyType, valueType);
    }

    /** {@inheritDoc} */
    @Override
    public void put(final SpatialObject<?, ?> object)
    {
        Bounds bb = object.getShape().getBounds();
        SpatialKey key = new SpatialKey(this.counter, (float) bb.getMinX(), (float) bb.getMinY(), (float) bb.getMaxX(),
                (float) bb.getMaxY());
        this.tree.add(key, object);
        this.counter++;
    }

    /** {@inheritDoc} */
    @Override
    public <T extends HierarchicalType<T, I>, I extends HierarchicallyTyped<T, I>,
            C extends SpatialObject<T, I>> Set<C> find(final T type, final OtsShape shape, final Class<C> searchClass)
    {
        Throw.whenNull(shape, "shape in find cannot be null");
        Throw.whenNull(searchClass, "searchClass in find cannot be null");
        Bounds bb = shape.getBounds();
        SpatialKey searchKey = new SpatialKey(this.counter, (float) bb.getMinX(), (float) bb.getMinY(), (float) bb.getMaxX(),
                (float) bb.getMaxY());
        final Set<C> returnSet = new LinkedHashSet<>();
        RTreeCursor<Object> it = this.tree.findIntersectingKeys(searchKey);
        it.forEachRemaining(new Consumer<Spatial>()
        {
            @Override
            public void accept(final Spatial t)
            {
                SpatialObject<?, ?> so = (SpatialObject<?, ?>) SpatialTreeH2.this.tree.get(t);
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
        SpatialKey searchKey = new SpatialKey(this.counter, (float) bb.getMinX(), (float) bb.getMinY(), (float) bb.getMaxX(),
                (float) bb.getMaxY());
        final Set<D> returnSet = new LinkedHashSet<>();
        RTreeCursor<Object> it = this.tree.findIntersectingKeys(searchKey);
        it.forEachRemaining(new Consumer<Spatial>()
        {
            @Override
            public void accept(final Spatial t)
            {
                SpatialObject<?, ?> so = (SpatialObject<?, ?>) SpatialTreeH2.this.tree.get(t);
                if (searchClass.isAssignableFrom(so.getClass()))
                {
                    @SuppressWarnings("unchecked")
                    D dso = (D) so;
                    if (type == null || dso.isOfType(type))
                    {
                        // find the current shape of the dynamic spatial object
                        if (so.getShape().intersects(shape))
                            returnSet.add(dso);
                    }
                }
            }
        });
        return returnSet;
    }

}
