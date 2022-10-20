package org.opentrafficsim.spatialtree.jsi;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
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
    final Map<Integer, SpatialObject<?, ?>> objectMap = new LinkedHashMap<>();

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
    public void put(final SpatialObject<?, ?> object)
    {
        Bounds bb = object.getShape().getBounds();
        Rectangle r = new Rectangle((float) bb.getMinX(), (float) bb.getMinY(), (float) bb.getMaxX(), (float) bb.getMaxY());
        this.objectMap.put(this.counter, object);
        this.tree.add(r, this.counter);
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
        Rectangle r = new Rectangle((float) bb.getMinX(), (float) bb.getMinY(), (float) bb.getMaxX(), (float) bb.getMaxY());
        final Set<C> returnSet = new LinkedHashSet<>();
        this.tree.intersects(r, new TIntProcedure()
        {
            @Override
            public boolean execute(final int value)
            {
                SpatialObject<?, ?> so = SpatialTreeJsi.this.objectMap.get(value);
                if (searchClass.isAssignableFrom(so.getClass()))
                {
                    @SuppressWarnings("unchecked")
                    C cso = (C) so;
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
    public <T extends HierarchicalType<T, I>, I extends HierarchicallyTyped<T, I>,
            D extends DynamicSpatialObject<T, I>> Set<D> find(final T type, final OtsShape shape, final Class<D> searchClass,
                    final Time time)
    {
        Throw.whenNull(shape, "shape in find cannot be null");
        Throw.whenNull(searchClass, "searchClass in find cannot be null");
        Bounds bb = shape.getBounds();
        Rectangle r = new Rectangle((float) bb.getMinX(), (float) bb.getMinY(), (float) bb.getMaxX(), (float) bb.getMaxY());
        final Set<D> returnSet = new LinkedHashSet<>();
        this.tree.intersects(r, new TIntProcedure()
        {
            @Override
            public boolean execute(final int value)
            {
                SpatialObject<?, ?> so = SpatialTreeJsi.this.objectMap.get(value);
                if (searchClass.isAssignableFrom(so.getClass()))
                {
                    @SuppressWarnings("unchecked")
                    D dso = (D) so;
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
