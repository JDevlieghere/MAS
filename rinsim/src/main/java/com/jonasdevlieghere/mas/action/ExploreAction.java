package com.jonasdevlieghere.mas.action;

import com.jonasdevlieghere.mas.beacon.BeaconTruck;
import org.apache.commons.math3.random.RandomGenerator;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;


public class ExploreAction extends Action {

    private final RandomGenerator rand;

    public ExploreAction(RoadModel rm, PDPModel pm, BeaconTruck truck, RandomGenerator rand) {
        super(rm, pm, null, truck);
        this.rand = rand;
    }

    @Override
    public void execute(TimeLapse time) {
        final RoadModel rm = getRoadModel();
        Point destination;
        BeaconTruck truck = (BeaconTruck)getUser();
        Point prevDestination = truck.getExplorationDestination();
        if(prevDestination == null || truck.getPosition().equals(prevDestination)){
            destination = rm.getRandomPosition(this.rand);
            truck.setExplorationDestination(destination);
        } else {
            destination = truck.getExplorationDestination();
        }
        rm.moveTo(truck, destination, time);
    }
}
