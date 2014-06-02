package com.jonasdevlieghere.mas.schedule;

import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.DeliveryTruck;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.util.TimeWindow;

public class NearestDeadLineStrategy implements SchedulingStrategy {

    private Scheduler scheduler;

    @Override
    public BeaconParcel next(RoadModel rm, PDPModel pm, TimeLapse time) {
        DeliveryTruck truck = scheduler.getUser();
        long minTime = Long.MAX_VALUE;
        BeaconParcel bestParcel = null;

        for (final Parcel parcel : pm.getContents(truck)) {
            long timeRemaining = timeRemaining(time, parcel.getDeliveryTimeWindow());
            if (timeRemaining < minTime) {
                if (pm.getTimeWindowPolicy().canDeliver(parcel.getDeliveryTimeWindow(),
                        time.getTime(), parcel.getPickupDuration())) {
                    minTime = timeRemaining;
                    bestParcel = (BeaconParcel) parcel;
                }
            }
        }
        return bestParcel;
    }

    private long timeRemaining(TimeLapse time, TimeWindow window){
        return window.end - time.getTime();
    }

    @Override
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

}