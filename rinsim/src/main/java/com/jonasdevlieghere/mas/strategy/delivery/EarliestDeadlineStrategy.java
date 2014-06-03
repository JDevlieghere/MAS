package com.jonasdevlieghere.mas.strategy.delivery;

import com.google.common.collect.ImmutableSet;
import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.BeaconTruck;
import com.jonasdevlieghere.mas.common.Scheduler;
import com.jonasdevlieghere.mas.strategy.SchedulingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.util.TimeWindow;

public class EarliestDeadlineStrategy implements SchedulingStrategy {

    final static Logger logger = LoggerFactory.getLogger(EarliestDeadlineStrategy.class);

    private Scheduler scheduler;

    @Override
    public BeaconParcel next(RoadModel rm, PDPModel pm, TimeLapse time) {
        BeaconTruck truck = scheduler.getUser();
        ImmutableSet<Parcel> parcels = pm.getContents(truck);

        BeaconParcel bestParcel = null;
        long bestTardiness = Long.MAX_VALUE;
        long bestEarliness = Long.MAX_VALUE;

        for(Parcel parcel: parcels){
            BeaconParcel beaconParcel = (BeaconParcel) parcel;
            try{
                long earliness = earliness(truck, beaconParcel, time.getTime());
                if(earliness < bestEarliness){
                    bestEarliness = earliness;
                    bestParcel = beaconParcel;
                }
            }catch (IllegalArgumentException e){
                // NOP
            }
        }
        if(bestParcel == null) {
            for (Parcel parcel : parcels) {
                BeaconParcel beaconParcel = (BeaconParcel) parcel;
                try{
                    long tardiness = tardiness(truck, beaconParcel, time.getTime());
                    if(tardiness < bestTardiness){
                        bestTardiness = tardiness;
                        bestParcel = beaconParcel;
                    }
                }catch (IllegalArgumentException e){
                    // NOP
                }
            }
        }
        return bestParcel;
    }

    private long tardiness(BeaconTruck truck, BeaconParcel parcel, long time) throws IllegalArgumentException{
        TimeWindow destinationWindow = parcel.getDeliveryTimeWindow();
        long destinationTime = parcel.getDestinationTime(truck, time);
        if(destinationWindow.isBeforeEnd(destinationTime))
            throw new IllegalArgumentException("There is no tardiness before the end of the time window.");
        return destinationTime - destinationWindow.end;
    }

    private long earliness(BeaconTruck truck, BeaconParcel parcel, long time) throws IllegalArgumentException{
        TimeWindow destinationWindow = parcel.getDeliveryTimeWindow();
        long destinationTime = parcel.getDestinationTime(truck, time);
        if(!destinationWindow.isIn(destinationTime))
            throw new IllegalArgumentException("There is no earliness outside the time window.");
        return destinationWindow.end - destinationTime;
    }

    @Override
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
}
