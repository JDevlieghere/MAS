package com.jonasdevlieghere.mas.beacon;


import com.jonasdevlieghere.mas.simulation.BeaconModel;
import rinde.sim.core.graph.Point;

public interface Beacon {

    public void setModel(BeaconModel model);

    public double getBeaconRadius();

    public Point getPosition();

    public BeaconStatus getBeaconStatus();

    public void setBeaconStatus(BeaconStatus status);

    public boolean ping();

}
