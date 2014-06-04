package com.jonasdevlieghere.mas.activity;

import com.jonasdevlieghere.mas.simulation.BeaconModel;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;

public abstract class Activity {

    private ActivityStatus status;
    private final ActivityUser user;

    /**
     * Create a new Activity associated with the given ActivityUser
     *
     * @param   user
     *          The ActivityUser associated with this Activity
     */
    Activity(ActivityUser user){
        this.user = user;
        setActivityStatus(ActivityStatus.NORMAL);
    }

    /**
     * Execute the current Activity
     *
     * @param   rm
     *          The RoadModel
     * @param   pm
     *          The PDPModel
     * @param   bm
     *          The BeaconModel
     * @param   time
     *          The Current TimeLapse
     */
    public abstract void execute(RoadModel rm, PDPModel pm, BeaconModel bm, TimeLapse time);

    void setActivityStatus(ActivityStatus status){
        this.status = status;
    }

    /**
     * Get the ActivityStatus of this Activity
     *
     * @return  The ActivityStatus of this Activity
     */
    public ActivityStatus getStatus(){
        return this.status;
    }

    /**
     * Get the ActivityUSer of this Activity
     *
     * @return The ActivityUSer of this Activity
     */
    ActivityUser getUser() {
        return user;
    }
}
