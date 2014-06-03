package com.jonasdevlieghere.mas.strategy;

import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.BeaconTruck;
import com.jonasdevlieghere.mas.common.Scheduler;
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
        BeaconTruck truck = scheduler.getUser();
        BeaconParcel bestParcel = null;
        for (final Parcel parcel : truck.getPickupQueue()) {
            BeaconParcel beaconParcel = (BeaconParcel)parcel;
            double distance = Point.distance(truck.getPosition(), beaconParcel.getPosition());
            if (distance < minDistance){
                minDistance = distance;
                bestParcel = beaconParcel;
            }
        }
        return bestParcel;
    }

    @Override
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
}
