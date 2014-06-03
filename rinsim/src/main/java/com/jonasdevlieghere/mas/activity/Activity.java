package com.jonasdevlieghere.mas.activity;

import com.jonasdevlieghere.mas.common.TickStatus;
import com.jonasdevlieghere.mas.simulation.BeaconModel;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;

public abstract class Activity {

    private TickStatus status;
    private ActivityUser user;

    public Activity(ActivityUser user){
        this.user = user;
        setActivityStatus(TickStatus.NORMAL);
    }

    public abstract void execute(RoadModel rm, PDPModel pm, BeaconModel bm, TimeLapse time);

    public void setActivityStatus(TickStatus status){
        this.status = status;
    }

    public TickStatus getStatus(){
        return this.status;
    }

    public ActivityUser getUser() {
        return user;
    }
}
