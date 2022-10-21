package org.opentrafficsim.spatialtree.test;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.spatialtree.test.ShortMerge.ShortMergeModel;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * ShortMergePerformance.java.
 * <p>
 * Copyright (c) 2022-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class ShortMergePerformance
{
    /**
     * Create a class to compare the performance of different spatial tree implementations for a model.
     */
    public ShortMergePerformance()
    {
        try
        {
            OtsSimulator simulator = new OtsSimulator("ShortMerge");
            final ShortMergeModel otsModel = new ShortMergeModel(simulator);
            simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600.0), otsModel);
            simulator.start();
            while (simulator.getSimulatorTime().si < 3600.0)
            {
                Thread.sleep(1000);
                System.out.println("T=" + simulator.getSimulatorTime() + ", #gtu=" + otsModel.getNetwork().getGTUs().size());
            }
        }
        catch (SimRuntimeException | NamingException | InterruptedException exception)
        {
            exception.printStackTrace();
        }

    }

    /**
     * Main program for performance test.
     * @param args String[] args
     */
    public static void main(final String[] args)
    {
        new ShortMergePerformance();
    }
}
