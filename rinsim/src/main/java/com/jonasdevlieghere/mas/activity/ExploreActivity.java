package com.jonasdevlieghere.mas.activity;

import com.jonasdevlieghere.mas.beacon.BeaconTruck;
import com.jonasdevlieghere.mas.common.TickStatus;
import com.jonasdevlieghere.mas.simulation.BeaconModel;
import org.apache.commons.math3.random.RandomGenerator;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;


public class ExploreActivity extends Activity{

    private final RandomGenerator rand;

    public ExploreActivity(BeaconTruck truck, RandomGenerator rand) {
        super(truck);
        this.rand = rand;
    }

    @Override
    public void execute(RoadModel rm, PDPModel pm, BeaconModel bm, TimeLapse time) {
        setActivityStatus(TickStatus.NORMAL);
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
