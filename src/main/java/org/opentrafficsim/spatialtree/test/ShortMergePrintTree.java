package org.opentrafficsim.spatialtree.test;

import java.rmi.RemoteException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.spatialtree.SpatialTree;
import org.opentrafficsim.spatialtree.h2.SpatialTreeH2;
import org.opentrafficsim.spatialtree.test.ShortMerge.ShortMergeModel;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * ShortMergePerformance.java.
 * <p>
 * Copyright (c) 2022-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class ShortMergePrintTree implements EventListener
{
    /** */
    private static final long serialVersionUID = 1L;

    /** the network. */
    private RoadNetwork network;

    /** the tree to use. */
    private SpatialTree tree;

    /**
     * Create a class to compare the performance of different spatial tree implementations for a model.
     */
    public ShortMergePrintTree()
    {
        this.tree = new SpatialTreeH2();
        // this.tree = new SpatialTreeJsi();
        // this.tree = new SpatialTreeRTree2();
        // this.tree = new SpatialTreeJtsStrTree(); // DOES NOT WORK: IMMUTABLE
        // this.tree = new SpatialTreeJtsHbrTree(); // DOES NOT WORK: IMMUTABLE
        try
        {
            OtsSimulator simulator = new OtsSimulator("ShortMerge");
            final ShortMergeModel otsModel = new ShortMergeModel(simulator);
            simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600.0), otsModel);
            this.network = otsModel.getNetwork();
            addInfra();
            subscribeGtus();
            simulator.scheduleEventRel(Duration.instantiateSI(5.0), this, this, "search", new Object[] {});
            simulator.start();
            while (simulator.getSimulatorTime().si < 3600.0)
            {
                Thread.sleep(1000);
                // System.out.println("T=" + simulator.getSimulatorTime() + ", #gtu=" + otsModel.getNetwork().getGTUs().size());
            }
        }
        catch (SimRuntimeException | NamingException | InterruptedException exception)
        {
            exception.printStackTrace();
        }
    }

    private void addInfra()
    {
        for (Link link : this.network.getLinkMap().values())
        {
            CrossSectionLink csl = (CrossSectionLink) link;
            for (Lane lane : csl.getLanes())
            {
                this.tree.add(lane);
            }
        }
    }

    private void subscribeGtus()
    {
        this.network.addListener(this, RoadNetwork.GTU_ADD_EVENT);
        this.network.addListener(this, RoadNetwork.GTU_REMOVE_EVENT);
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final Event event) throws RemoteException
    {
        if (event.getType().equals(RoadNetwork.GTU_ADD_EVENT))
        {
            String gtuId = event.getContent().toString();
            Gtu gtu = this.network.getGTU(gtuId);
            gtu.addListener(this, Gtu.MOVE_EVENT);
        }
        else if (event.getType().equals(RoadNetwork.GTU_REMOVE_EVENT))
        {
            String gtuId = event.getContent().toString();
            Gtu gtu = this.network.getGTU(gtuId);
            gtu.removeListener(this, Gtu.MOVE_EVENT);
            this.tree.remove(gtu);
        }
        else if (event.getType().equals(Gtu.MOVE_EVENT))
        {
            Object[] contentArray = (Object[]) event.getContent();
            String gtuId = contentArray[0].toString();
            Gtu gtu = this.network.getGTU(gtuId);
            this.tree.remove(gtu);
            this.tree.add(gtu);
        }
    }

    protected void search()
    {
        Time time = this.network.getSimulator().getSimulatorAbsTime();
        int nrGtus = this.network.getGTUs().size();
        Set<String> countSet = new LinkedHashSet<>();
        System.out.println("\nTime: " + time + ", #gtu=" + nrGtus);
        for (Link link : this.network.getLinkMap().values())
        {
            CrossSectionLink csl = (CrossSectionLink) link;
            for (Lane lane : csl.getLanes())
            {
                System.out.println("Lane: " + lane);
                System.out.print("GTUs: ");
                Set<Gtu> gtus = this.tree.find(DefaultsNl.VEHICLE, lane.getShape(), Gtu.class, time);
                for (Gtu gtu : gtus)
                {
                    System.out.print(gtu.getId() + " ");
                    countSet.add(gtu.getId());
                }
                System.out.println();
            }
        }
        if (countSet.size() == nrGtus)
            System.out.println("CORRECT number of Gtus in set");
        else
        {
            System.err.println("INCORRECT number of Gtus in set: " + countSet.size() + ", expected: " + nrGtus);
            System.exit(-1);
        }
        this.network.getSimulator().scheduleEventRel(Duration.instantiateSI(1.0), this, this, "search", new Object[] {});
    }

    /**
     * Main program for performance test.
     * @param args String[] args
     */
    public static void main(final String[] args)
    {
        new ShortMergePrintTree();
    }
}
