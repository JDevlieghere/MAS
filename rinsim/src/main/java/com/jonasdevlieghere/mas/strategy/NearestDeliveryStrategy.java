package com.jonasdevlieghere.mas.strategy;

import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.BeaconTruck;
import com.jonasdevlieghere.mas.common.Scheduler;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;

public class NearestDeliveryStrategy implements SchedulingStrategy {

    private Scheduler scheduler;

    @Override
    public BeaconParcel next(RoadModel rm, PDPModel pm, TimeLapse time) {
        BeaconTruck truck = scheduler.getUser();
        double minDistance = Double.POSITIVE_INFINITY;
        BeaconParcel bestParcel = null;

        for (final Parcel parcel : pm.getContents(truck)) {
            double distance = Point.distance(truck.getPosition(), parcel.getDestination());
            if (distance < minDistance){
                if(parcel.canBeDelivered(truck, time.getTime())) {
                    minDistance = distance;
                    bestParcel = (BeaconParcel) parcel;
                }
            }
        }
        return bestParcel;
    }

    @Override
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

}
