package com.jonasdevlieghere.mas.activity;

import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;

public abstract class Activity {

    private ActivityStatus status;
    private ActivityUser user;

    public Activity(ActivityUser user){
        this.user = user;
        setStatus(ActivityStatus.NORMAL);
    }

    public abstract void execute(RoadModel rm, PDPModel pm, TimeLapse time);

    public void setStatus(ActivityStatus status){
        this.status = status;
    }

    public ActivityStatus getStatus(){
        return this.status;
    }

    public ActivityUser getUser() {
        return user;
    }
}
