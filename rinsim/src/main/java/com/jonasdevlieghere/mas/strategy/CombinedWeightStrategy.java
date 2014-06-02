package com.jonasdevlieghere.mas.strategy;

import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.DeliveryTruck;
import com.jonasdevlieghere.mas.schedule.Scheduler;
import com.jonasdevlieghere.mas.schedule.SchedulingStrategy;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.util.TimeWindow;


public class CombinedWeightStrategy  implements SchedulingStrategy {

    private Scheduler scheduler;

    @Override
    public BeaconParcel next(RoadModel rm, PDPModel pm, TimeLapse time) {
        DeliveryTruck truck = scheduler.getUser();
        double minWeight = Double.POSITIVE_INFINITY;
        BeaconParcel bestParcel = null;
        for (final Parcel parcel : pm.getContents(truck)) {
            double weight = weight(parcel, truck, rm, pm, time);
            if (weight < minWeight) {
                if (pm.getTimeWindowPolicy().canDeliver(parcel.getDeliveryTimeWindow(),
                        time.getTime(), parcel.getPickupDuration())) {
                    minWeight = weight;
                    bestParcel = (BeaconParcel) parcel;
                }
            }
        }
        return bestParcel;
    }

    private long weight(Parcel parcel, DeliveryTruck truck, RoadModel rm, PDPModel pm, TimeLapse time){
        double distance = Point.distance(truck.getPosition(), parcel.getDestination());
        double speed = truck.getSpeed();
        long deliverTime = (long)(distance/speed);
        long deadlineTime = parcel.getDeliveryTimeWindow().end;
        long weight = deadlineTime - deliverTime - time.getTime();
        System.out.println(weight);
        return weight;
    }

    @Override
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
}
