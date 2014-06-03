package com.jonasdevlieghere.mas.strategy.pickup;

import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.BeaconTruck;
import com.jonasdevlieghere.mas.common.Scheduler;
import com.jonasdevlieghere.mas.strategy.SchedulingStrategy;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;

import java.util.List;

public class FiFoPickupStrategy implements SchedulingStrategy {

    private Scheduler scheduler;

    @Override
    public BeaconParcel next(RoadModel rm, PDPModel pm, TimeLapse time) {
        BeaconTruck truck = scheduler.getUser();
        List<BeaconParcel> queue = truck.getPickupQueue();
        if(queue.isEmpty())
            return null;
        return queue.get(0);
    }

    @Override
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public String toString() {
        return "FiFoPickupStrategy";
    }
}
