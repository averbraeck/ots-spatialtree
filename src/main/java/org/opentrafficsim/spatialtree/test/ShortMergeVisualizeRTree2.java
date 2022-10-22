package org.opentrafficsim.spatialtree.test;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.naming.NamingException;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.event.EventInterface;
import org.djutils.event.EventListenerInterface;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.road.network.OtsRoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.spatialtree.rtree2.SpatialTreeRTree2;
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
public class ShortMergeVisualizeRTree2 implements EventListenerInterface
{
    /** */
    private static final long serialVersionUID = 1L;

    /** the network. */
    private OtsRoadNetwork network;

    /** the tree to use. */
    private SpatialTreeRTree2 tree;

    /** frame. */
    private JFrame frame;

    /** image. */
    private ImagePanel imagePanel;

    /**
     * Create a class to compare the performance of different spatial tree implementations for a model.
     */
    public ShortMergeVisualizeRTree2()
    {
        this.frame = new JFrame("RTree2 ShortMerge");
        this.frame.setSize(1920, 1000);
        this.frame.setVisible(true);
        this.frame.getContentPane().setLayout(new BorderLayout());
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.imagePanel = new ImagePanel();
        this.frame.getContentPane().add(this.imagePanel, BorderLayout.CENTER);

        this.tree = new SpatialTreeRTree2();
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
        System.out.println(this.tree.getTree().asString());
    }

    private void subscribeGtus()
    {
        this.network.addListener(this, OtsRoadNetwork.GTU_ADD_EVENT);
        this.network.addListener(this, OtsRoadNetwork.GTU_REMOVE_EVENT);
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final EventInterface event) throws RemoteException
    {
        if (event.getType().equals(OtsRoadNetwork.GTU_ADD_EVENT))
        {
            String gtuId = event.getContent().toString();
            Gtu gtu = this.network.getGTU(gtuId);
            gtu.addListener(this, Gtu.MOVE_EVENT);
        }
        else if (event.getType().equals(OtsRoadNetwork.GTU_REMOVE_EVENT))
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
        int nrGtus = this.network.getGTUs().size();
        Set<String> countSet = new LinkedHashSet<>();
        System.out.println("\nTime: " + this.network.getSimulator().getSimulatorTime() + ", #gtu=" + nrGtus);
        for (Link link : this.network.getLinkMap().values())
        {
            CrossSectionLink csl = (CrossSectionLink) link;
            for (Lane lane : csl.getLanes())
            {
                System.out.println("Lane: " + lane);
                System.out.print("GTUs: ");
                Set<Gtu> gtus = this.tree.find(this.network.getGtuType(GtuType.DEFAULTS.VEHICLE), lane.getShape(), Gtu.class);
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
            System.err.println("INCORRECT number of Gtus in set: " + countSet.size());
            System.exit(-1);
        }

        final BufferedImage image = this.tree.getTree().visualize(1900, 1000).createImage();
        this.imagePanel.setImage(image);
        this.frame.validate();

        this.network.getSimulator().scheduleEventRel(Duration.instantiateSI(0.1), this, this, "search", new Object[] {});
    }

    /**
     * Main program for performance test.
     * @param args String[] args
     */
    public static void main(final String[] args)
    {
        new ShortMergeVisualizeRTree2();
    }

    /** Draw the RTreee in a panel on the screen. */
    private static class ImagePanel extends JPanel
    {
        /** */
        private static final long serialVersionUID = 1L;

        /** the image with the RTree. */
        private BufferedImage image;

        /**
         * Set a new image.
         * @param image BufferedImage; the image
         */
        void setImage(final BufferedImage image)
        {
            this.image = image;
            repaint();
        }

        /** {@inheritDoc} */
        @Override
        protected void paintComponent(final Graphics g)
        {
            super.paintComponent(g);
            if (this.image != null)
            {
                g.drawImage(this.image, 0, 0, null);
            }
        }

    }
}
