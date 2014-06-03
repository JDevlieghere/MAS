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

public class EarliestDeliveryStrategy implements SchedulingStrategy {

    final static Logger logger = LoggerFactory.getLogger(EarliestDeliveryStrategy.class);

    private Scheduler scheduler;

    @Override
    public BeaconParcel next(RoadModel rm, PDPModel pm, TimeLapse time) {
        BeaconTruck truck = scheduler.getUser();
        ImmutableSet<Parcel> parcels = pm.getContents(truck);

        BeaconParcel bestParcel = null;
        long bestTardiness = Long.MAX_VALUE;

        for(Parcel parcel: parcels){
            try{
                BeaconParcel beaconParcel = (BeaconParcel) parcel;
                long tardiness = tardiness(truck, beaconParcel, time.getTime());
                if(tardiness < bestTardiness){
                    bestTardiness = tardiness;
                    bestParcel = beaconParcel;
                }
            }catch (RuntimeException e){
                // NOP
            }

        }
        return bestParcel;
    }

    private long tardiness(BeaconTruck truck, BeaconParcel parcel, long time) throws RuntimeException{
        long destinationTime = parcel.getDestinationTime(truck, time);
        TimeWindow destinationWindow = parcel.getDeliveryTimeWindow();
        long tardiness;
        if(destinationWindow.isBeforeStart(destinationTime)) {
            throw new RuntimeException();
        }else{
            tardiness = destinationTime - destinationWindow.end;
        }
        return tardiness;
    }

    @Override
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
}
