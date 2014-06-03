package com.jonasdevlieghere.mas.strategy.delivery;

import com.google.common.collect.ImmutableSet;
import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.BeaconTruck;
import com.jonasdevlieghere.mas.common.Scheduler;
import com.jonasdevlieghere.mas.strategy.SchedulingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.util.TimeWindow;

import java.util.ArrayList;

public class NearestOnTimeDeliveryStrategy implements SchedulingStrategy {

    final static Logger logger = LoggerFactory.getLogger(EarliestDeadlineStrategy.class);

    private Scheduler scheduler;

    @Override
    public BeaconParcel next(RoadModel rm, PDPModel pm, TimeLapse time) {
        BeaconTruck truck = scheduler.getUser();
        ImmutableSet<Parcel> parcels = pm.getContents(truck);

        ArrayList<BeaconParcel> onTimeDeliverableParcels = new ArrayList<BeaconParcel>();
        ArrayList<BeaconParcel> lateDeliverableParcels = new ArrayList<BeaconParcel>();


        for(Parcel parcel: parcels){
            BeaconParcel beaconParcel = (BeaconParcel) parcel;
            TimeWindow destinationWindow = parcel.getDeliveryTimeWindow();
            long destinationTime = beaconParcel.getDestinationTime(truck, time.getTime());
            if(destinationWindow.isIn(destinationTime)){
                onTimeDeliverableParcels.add(beaconParcel);
            }else if(destinationWindow.isAfterEnd(destinationTime)){
                lateDeliverableParcels.add(beaconParcel);

            }
        }
        double minDistance = Double.MAX_VALUE;
        BeaconParcel bestParcel = null;
        for (final Parcel parcel : onTimeDeliverableParcels) {
            double distance = Point.distance(truck.getPosition(), parcel.getDestination());
            if (distance < minDistance){
                minDistance = distance;
                bestParcel = (BeaconParcel) parcel;
            }
        }
        if(bestParcel != null)
            return bestParcel;

        minDistance = Double.MAX_VALUE;
        bestParcel = null;
        for (final Parcel parcel : lateDeliverableParcels) {
            double distance = Point.distance(truck.getPosition(), parcel.getDestination());
            if (distance < minDistance){
                minDistance = distance;
                bestParcel = (BeaconParcel) parcel;
            }
        }
        return bestParcel;

    }

    @Override
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
}
