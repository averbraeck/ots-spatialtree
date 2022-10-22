package org.opentrafficsim.spatialtree.h2;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.db.SpatialKey;
import org.h2.mvstore.rtree.MVRTreeMap;
import org.h2.mvstore.rtree.MVRTreeMap.RTreeCursor;
import org.h2.mvstore.rtree.Spatial;
import org.h2.mvstore.type.ObjectDataType;
import org.opentrafficsim.base.HierarchicalType;
import org.opentrafficsim.base.HierarchicallyTyped;
import org.opentrafficsim.core.DynamicSpatialObject;
import org.opentrafficsim.core.SpatialObject;
import org.opentrafficsim.core.geometry.Bounds;
import org.opentrafficsim.core.geometry.OtsShape;
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

    /** the bounding boxes of the dynamic objects at the time of insertion. */
    final Map<SpatialObject, SpatialKey> bboxMap = new LinkedHashMap<>();

    /**
     * Constructor; initialize the spatial index.
     */
    public SpatialTreeH2()
    {
        MVStore s = MVStore.open(null);
        MVRTreeMap.Builder<Object> builder = new MVRTreeMap.Builder<>().dimensions(2);
        builder.setValueType(new ObjectDataType());
        this.tree = s.openMap("data", builder);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends HierarchicalType<T, I>, I extends HierarchicallyTyped<T, I> & SpatialObject> void add(final I object)
    {
        Bounds bb = object.getShape().getBounds();
        SpatialKey key = new SpatialKey(this.counter, (float) bb.getMinX(), (float) bb.getMaxX(), (float) bb.getMinY(),
                (float) bb.getMaxY());
        this.tree.add(key, object);
        this.bboxMap.put(object, key);
        this.counter++;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unlikely-arg-type")
    @Override
    public <T extends HierarchicalType<T, I>,
            I extends HierarchicallyTyped<T, I> & SpatialObject> boolean remove(final I object)
    {
        SpatialKey key = this.bboxMap.get(object);
        if (key != null)
        {
            this.bboxMap.remove(object);
            return this.tree.remove(key, object);
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
        SpatialKey searchKey = new SpatialKey(this.counter, (float) bb.getMinX(), (float) bb.getMaxX(), (float) bb.getMinY(),
                (float) bb.getMaxY());
        final Set<I> returnSet = new LinkedHashSet<>();
        RTreeCursor<Object> it = this.tree.findIntersectingKeys(searchKey);
        it.forEachRemaining(new Consumer<Spatial>()
        {
            @Override
            public void accept(final Spatial t)
            {
                SpatialObject so = (SpatialObject) SpatialTreeH2.this.tree.get(t);
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
        SpatialKey searchKey = new SpatialKey(this.counter, (float) bb.getMinX(), (float) bb.getMaxX(), (float) bb.getMinY(),
                (float) bb.getMaxY());
        final Set<I> returnSet = new LinkedHashSet<>();
        RTreeCursor<Object> it = this.tree.findIntersectingKeys(searchKey);
        it.forEachRemaining(new Consumer<Spatial>()
        {
            @Override
            public void accept(final Spatial t)
            {
                SpatialObject so = (SpatialObject) SpatialTreeH2.this.tree.get(t);
                if (searchClass.isAssignableFrom(so.getClass()))
                {
                    @SuppressWarnings("unchecked")
                    I dso = (I) so;
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
