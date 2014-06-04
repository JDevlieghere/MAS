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
    private final BeaconTruck truck;
    private final SchedulingStrategy strategy;

    /**
     * Create a new Scheduler
     *
     * @param   truck
     *          The BeaconTruck associated with this Scheduler
     * @param   strategy
     *          The Strategy associated with this Scheduler
     */
    public Scheduler(BeaconTruck truck, SchedulingStrategy strategy){
        this.truck = truck;
        this.strategy = strategy;
        this.strategy.setScheduler(this);
    }

    /**
     * Returns the next deliverable BeaconParcel
     * @param   rm
     *          The RoadModel
     * @param   pm
     *          The PDPModel
     * @param   time
     *          The current TimeLapse
     * @return  The next deliverable BeaconParcel
     */
    public BeaconParcel nextDeliverable(RoadModel rm, PDPModel pm, TimeLapse time){
        return this.strategy.next(rm, pm, time);
    }

    public BeaconTruck getUser(){
        return this.truck;
    }
}
