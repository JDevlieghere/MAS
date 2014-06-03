package com.jonasdevlieghere.mas.action;

import com.jonasdevlieghere.mas.common.TickStatus;
import com.jonasdevlieghere.mas.simulation.BeaconModel;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;

public abstract class Action {

    private TickStatus status;
    private final RoadModel rm;
    private final PDPModel pm;
    private final BeaconModel bm;
    private ActionUser user;

    public Action(RoadModel rm, PDPModel pm,  BeaconModel bm, ActionUser user){
        this.rm = rm;
        this.pm = pm;
        this.bm = bm;
        this.user = user;
        setStatus(TickStatus.NORMAL);
    }

    public abstract void execute(TimeLapse time);

    protected void setStatus(TickStatus status){
        this.status = status;
    }

    public TickStatus getStatus(){
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

    public ActionUser getUser(){
        return this.user;
    }
}
