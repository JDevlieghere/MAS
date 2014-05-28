package com.jonasdevlieghere.mas.action;

import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.pdptw.common.DefaultVehicle;

public abstract class Action {

    private ActionStatus status;
    private final RoadModel rm;
    private final PDPModel pm;
    private final DefaultVehicle vehicle;

    public Action(RoadModel rm, PDPModel pm, DefaultVehicle vehicle){
        this.rm = rm;
        this.pm = pm;
        this.vehicle = vehicle;
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

    public DefaultVehicle getVehicle(){
        return this.vehicle;
    }
}
