package com.jonasdevlieghere.mas.beacon;


import com.jonasdevlieghere.mas.simulation.BeaconModel;
import rinde.sim.core.graph.Point;

public interface Beacon {

    public void setModel(BeaconModel model);

    /**
     * Get the beacon radius
     *
     * @return  The beacon radius
     */
    public double getBeaconRadius();

    /**
     * Get the beacon Position
     *
     * @return  The beacon position
     */
    public Point getPosition();

    /**
     * Get the BeaconStatus
     *
     * @return  The beacon status
     */
    public BeaconStatus getBeaconStatus();

    /**
     * Set the BeaconStatus
     *
     * @param   status
     *          The BeaconStatus
     */
    public void setBeaconStatus(BeaconStatus status);

    /**
     * Ping the Beacon
     *
     * @return  True if and only if the beacon is not yet pinged
     */
    public boolean ping();

}
