package org.opentrafficsim.spatialtree.jsi;

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

import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.rtree.RTree;

import gnu.trove.TIntProcedure;

/**
 * SpatialTreeJsi.java.
 * <p>
 * Copyright (c) 2022-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class SpatialTreeJsi implements SpatialTree
{
    /** the tree object. */
    final RTree tree = new RTree();

    /** the objects. */
    final Map<Integer, SpatialObject> objectMap = new LinkedHashMap<>();

    /** the bounding boxes of the objects at the time of insertion. */
    final Map<Integer, Rectangle> bboxMap = new LinkedHashMap<>();

    /** the reverse map of objects. */
    final Map<SpatialObject, Integer> reverseObjectMap = new LinkedHashMap<>();

    /** object counter. */
    int counter = 0;

    /**
     * Constructor; initialize the spatial index.
     */
    public SpatialTreeJsi()
    {
        this.tree.init(null);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends HierarchicalType<T, I>, I extends HierarchicallyTyped<T, I> & SpatialObject> void add(final I object)
    {
        Bounds bb = object.getShape().getEnvelope();
        Rectangle r = new Rectangle((float) bb.getMinX(), (float) bb.getMinY(), (float) bb.getMaxX(), (float) bb.getMaxY());
        this.objectMap.put(this.counter, object);
        this.bboxMap.put(this.counter, r);
        this.reverseObjectMap.put(object, this.counter);
        this.tree.add(r, this.counter);
        this.counter++;
    }

    /** {@inheritDoc} */
    @Override
    public <T extends HierarchicalType<T, I>,
            I extends HierarchicallyTyped<T, I> & SpatialObject> boolean remove(final I object)
    {
        @SuppressWarnings("unlikely-arg-type")
        Integer nr = this.reverseObjectMap.get(object);
        if (nr != null)
        {
            Rectangle r = this.bboxMap.get(nr);
            this.objectMap.remove(nr, object);
            this.reverseObjectMap.remove(nr, object);
            this.bboxMap.remove(nr);
            return this.tree.delete(r, nr);
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
        Bounds bb = shape.getEnvelope();
        Rectangle r = new Rectangle((float) bb.getMinX(), (float) bb.getMinY(), (float) bb.getMaxX(), (float) bb.getMaxY());
        final Set<I> returnSet = new LinkedHashSet<>();
        this.tree.intersects(r, new TIntProcedure()
        {
            @Override
            public boolean execute(final int value)
            {
                SpatialObject so = SpatialTreeJsi.this.objectMap.get(value);
                if (searchClass.isAssignableFrom(so.getClass()))
                {
                    @SuppressWarnings("unchecked")
                    I cso = (I) so;
                    if (type == null || cso.isOfType(type))
                    {
                        if (so.getShape().intersects(shape))
                            returnSet.add(cso);
                        return true;
                    }
                }
                return false;
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
        Bounds bb = shape.getEnvelope();
        Rectangle r = new Rectangle((float) bb.getMinX(), (float) bb.getMinY(), (float) bb.getMaxX(), (float) bb.getMaxY());
        final Set<I> returnSet = new LinkedHashSet<>();
        this.tree.intersects(r, new TIntProcedure()
        {
            @Override
            public boolean execute(final int value)
            {
                SpatialObject so = SpatialTreeJsi.this.objectMap.get(value);
                if (searchClass.isAssignableFrom(so.getClass()))
                {
                    @SuppressWarnings("unchecked")
                    I dso = (I) so;
                    if (type == null || dso.isOfType(type))
                    {
                        // find the current shape of the dynamic spatial object
                        if (so.getShape().intersects(shape))
                            returnSet.add(dso);
                        return true;
                    }
                }
                return false;
            }
        });
        return returnSet;
    }

}
