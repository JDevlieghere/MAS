package com.jonasdevlieghere.mas.action;

import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.DeliveryTruck;
import org.apache.commons.math3.random.RandomGenerator;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;


public class SmartExploreAction extends Action {

    private final RandomGenerator rand;

    public SmartExploreAction(RoadModel rm, PDPModel pm, DeliveryTruck truck, RandomGenerator rand) {
        super(rm, pm, null, truck);
        this.rand = rand;
    }

    @Override
    public void execute(TimeLapse time) {
        final RoadModel rm = getRoadModel();
        final PDPModel pm = getPDPModel();

        DeliveryTruck truck = (DeliveryTruck)getUser();
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
