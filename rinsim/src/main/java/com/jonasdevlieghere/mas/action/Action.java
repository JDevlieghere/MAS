package com.jonasdevlieghere.mas.action;

import com.jonasdevlieghere.mas.DeliveryTruck;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;

public abstract class Action {

    private ActionStatus status;
    private final RoadModel rm;
    private final PDPModel pm;
    private final DeliveryTruck truck;

    public Action(RoadModel rm, PDPModel pm, DeliveryTruck truck){
        this.rm = rm;
        this.pm = pm;
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

    public DeliveryTruck getTruck(){
        return this.truck;
    }
}
