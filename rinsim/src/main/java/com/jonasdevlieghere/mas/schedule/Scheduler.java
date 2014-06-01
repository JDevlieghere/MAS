package com.jonasdevlieghere.mas.schedule;


import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.DeliveryTruck;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;

public class Scheduler {

    private RoadModel rm;
    private PDPModel pm;
    private DeliveryTruck truck;
    private SchedulingStrategy strategy;

    public Scheduler(DeliveryTruck truck, SchedulingStrategy strategy){
        this.truck = truck;
        this.strategy = strategy;
        this.strategy.setScheduler(this);
    }

    public BeaconParcel next(RoadModel rm, PDPModel pm, TimeLapse time){
        return this.strategy.next(rm, pm, time);
    }

    public DeliveryTruck getUser(){
        return this.truck;
    }
}
