package com.jonasdevlieghere.mas.strategy;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.BeaconTruck;
import com.jonasdevlieghere.mas.common.Scheduler;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.util.TimeWindow;

import java.util.Collection;
import java.util.List;

public class SmartDeliveryStrategy implements SchedulingStrategy {

    private Scheduler scheduler;

    @Override
    public BeaconParcel next(RoadModel rm, PDPModel pm, TimeLapse time) {
        BeaconTruck truck = scheduler.getUser();
        List<Parcel> route = bestRoute(truck, pm.getContents(truck), time.getTime());
        if(route.isEmpty())
            return null;
        return (BeaconParcel)route.get(0);
    }

    private long tardiness(BeaconTruck truck, BeaconParcel parcel, long time) throws IllegalArgumentException{
        long destinationTime = parcel.getDestinationTime(truck, time);
        TimeWindow destinationWindow = parcel.getDeliveryTimeWindow();
        if(destinationWindow.isBeforeEnd(destinationTime))
            return 0;
        long tardiness = destinationWindow.end - destinationTime;
        assert(tardiness >= 0);
        return tardiness;
    }

    private long totalTardiness(BeaconTruck truck, List<Parcel> parcels, long time){
        long totalTardiness = 0;
        long lastTime = time;
        for(Parcel parcel: parcels){
            BeaconParcel beaconParcel = (BeaconParcel) parcel;
            totalTardiness += tardiness(truck, beaconParcel, lastTime);
            lastTime += beaconParcel.getDestinationTime(truck, lastTime);
        }
        return totalTardiness;
    }

    private List<Parcel> bestRoute(BeaconTruck truck, ImmutableSet<Parcel> parcels, long time){
        Collection<List<Parcel>> permutations = Collections2.permutations(parcels);
        List<Parcel> bestList = null;
        long bestTardiness = Long.MAX_VALUE;
        for(List<Parcel> parcelList: permutations){
            long tardiness = totalTardiness(truck, parcelList, time);
            if(tardiness < bestTardiness){
                bestTardiness = tardiness;
                bestList = parcelList;
            }
        }
        return bestList;
    }

    @Override
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
}
