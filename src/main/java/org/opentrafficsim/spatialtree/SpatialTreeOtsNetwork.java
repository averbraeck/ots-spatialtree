package org.opentrafficsim.spatialtree;

import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.road.network.OtsRoadNetwork;

/**
 * SpatialTreeOtsNetwork.java.
 * <p>
 * Copyright (c) 2022-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class SpatialTreeOtsNetwork extends OtsRoadNetwork
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * Construction of an empty network.
     * @param id String; the network id.
     * @param addDefaultTypes add the default GtuTypes, LinkTypes and LaneTypes, or not
     * @param simulator OTSSimulatorInterface; the DSOL simulator engine
     */
    public SpatialTreeOtsNetwork(final String id, final boolean addDefaultTypes, final OtsSimulatorInterface simulator)
    {
        super(id, addDefaultTypes, simulator);
    }

    
}
