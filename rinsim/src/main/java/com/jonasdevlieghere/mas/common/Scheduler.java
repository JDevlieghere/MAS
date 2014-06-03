package com.jonasdevlieghere.mas.common;


import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.BeaconTruck;
import com.jonasdevlieghere.mas.strategy.SchedulingStrategy;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;

public class Scheduler {

    private RoadModel rm;
    private PDPModel pm;
    private BeaconTruck truck;
    private SchedulingStrategy strategy;

    public Scheduler(BeaconTruck truck, SchedulingStrategy strategy){
        this.truck = truck;
        this.strategy = strategy;
        this.strategy.setScheduler(this);
    }

    public BeaconParcel nextDeliverable(RoadModel rm, PDPModel pm, TimeLapse time){
        return this.strategy.next(rm, pm, time);
    }

    public BeaconTruck getUser(){
        return this.truck;
    }
}
