package com.jonasdevlieghere.mas.action;

import com.jonasdevlieghere.mas.simulation.BeaconModel;
import com.jonasdevlieghere.mas.beacon.DeliveryTruck;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;

public abstract class Action {

    private ActionStatus status;
    private final RoadModel rm;
    private final PDPModel pm;
    private final BeaconModel bm;
    private DeliveryTruck truck;

    public Action(RoadModel rm, PDPModel pm,  BeaconModel bm, DeliveryTruck truck){
        this.rm = rm;
        this.pm = pm;
        this.bm = bm;
        this.truck = truck;
        setStatus(ActionStatus.PENDING);
    }

    public abstract void execute(TimeLapse time);

    protected void setStatus(ActionStatus status){
        this.status = status;
    }

    public ActionStatus getStatus(){
        return this.status;
    }

    public RoadModel getRoadModel(){
        return this.rm;
    }

    public PDPModel getPDPModel(){
        return this.pm;
    }

    public BeaconModel getBeaconModel() {
        return this.bm;
    }

    public DeliveryTruck getTruck(){
        return this.truck;
    }
}
