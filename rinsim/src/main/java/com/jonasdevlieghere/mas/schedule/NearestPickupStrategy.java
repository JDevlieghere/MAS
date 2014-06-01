package com.jonasdevlieghere.mas.schedule;

import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.DeliveryTruck;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;


public class NearestPickupStrategy implements SchedulingStrategy {

    private Scheduler scheduler;

    @Override
    public BeaconParcel next(RoadModel rm, PDPModel pm, TimeLapse time) {
        double minDistance = Double.POSITIVE_INFINITY;
        DeliveryTruck truck = scheduler.getUser();
        BeaconParcel bestParcel = null;

        for (final Parcel parcel : truck.getPickupQueue()) {
            double distance = Point.distance(truck.getPosition(), parcel.getDestination());
            if (distance < minDistance){
                minDistance = distance;
                bestParcel = (BeaconParcel)parcel;
            }
        }
        return bestParcel;
    }

    @Override
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
}
