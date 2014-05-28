package com.jonasdevlieghere.mas.beacon;


import com.jonasdevlieghere.mas.simulation.BeaconModel;
import rinde.sim.core.graph.Point;

public interface Beacon {

    public void setModel(BeaconModel model);

    public double getRadius();

    public Point getPosition();

}
