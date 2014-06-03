package com.jonasdevlieghere.mas.activity;

import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.BeaconTruck;
import com.jonasdevlieghere.mas.simulation.BeaconModel;
import org.apache.commons.math3.random.RandomGenerator;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;


public class SmartExploreActivity extends Activity{

    private final RandomGenerator rand;

    public SmartExploreActivity(BeaconTruck truck, RandomGenerator rand) {
        super(truck);
        this.rand = rand;
    }

    @Override
    public void execute(RoadModel rm, PDPModel pm, BeaconModel bm, TimeLapse time) {
        setActivityStatus(ActivityStatus.NORMAL);
        BeaconTruck truck = (BeaconTruck)getUser();
        double minDistance = Double.POSITIVE_INFINITY;
        BeaconParcel bestParcel = null;

        for (final Parcel parcel : pm.getContents(truck)) {
            double distance = Point.distance(truck.getPosition(), parcel.getDestination());
            if (distance < minDistance){
                minDistance = distance;
                bestParcel = (BeaconParcel) parcel;
            }
        }
        if(bestParcel != null) {
            rm.moveTo(truck, bestParcel.getPosition(), time);
        }else{
            Point destination;
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
}
