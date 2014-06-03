package com.jonasdevlieghere.mas.strategy.delivery;

import com.google.common.collect.ImmutableSet;
import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.BeaconTruck;
import com.jonasdevlieghere.mas.common.Scheduler;
import com.jonasdevlieghere.mas.strategy.SchedulingStrategy;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.util.TimeWindow;

public class MostTardyFirstStrategy implements SchedulingStrategy {

    private Scheduler scheduler;
    private SchedulingStrategy backup;

    @Override
    public BeaconParcel next(RoadModel rm, PDPModel pm, TimeLapse time) {
        BeaconTruck truck = scheduler.getUser();
        ImmutableSet<Parcel> parcels = pm.getContents(truck);
        long maxTardiness = 0;
        BeaconParcel bestParcel = null;

        for(Parcel parcel: parcels){
            BeaconParcel beaconParcel = (BeaconParcel) parcel;
            TimeWindow destinationWindow = parcel.getDeliveryTimeWindow();
            long destinationTime = beaconParcel.getDestinationTime(truck, time.getTime());
            if(destinationWindow.isAfterEnd(destinationTime)){
                long tardiness = destinationTime - destinationWindow.end;
                if(tardiness > maxTardiness){
                    maxTardiness = tardiness;
                    bestParcel = beaconParcel;
                }
            }
        }
        if(bestParcel != null)
            return bestParcel;
        return this.backup.next(rm, pm, time);
    }

    @Override
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
        this.backup = new NearestDeliveryStrategy();
        this.backup.setScheduler(scheduler);
    }
}
