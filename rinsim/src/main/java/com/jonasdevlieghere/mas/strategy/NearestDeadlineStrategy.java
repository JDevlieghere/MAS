package com.jonasdevlieghere.mas.strategy;

import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.BeaconTruck;
import com.jonasdevlieghere.mas.common.Scheduler;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.util.TimeWindow;

public class NearestDeadlineStrategy implements SchedulingStrategy {

    private Scheduler scheduler;

    @Override
    public BeaconParcel next(RoadModel rm, PDPModel pm, TimeLapse time) {
        BeaconTruck truck = scheduler.getUser();
        long minTime = Long.MAX_VALUE;
        BeaconParcel bestParcel = null;

        for (final Parcel parcel : pm.getContents(truck)) {
            BeaconParcel bp = (BeaconParcel)parcel;
            long timeRemaining = timeRemaining(time, parcel.getDeliveryTimeWindow());
            if (timeRemaining < minTime) {
                if (bp.canBeDelivered(truck, time.getTime())) {
                    minTime = timeRemaining;
                    bestParcel = bp;
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