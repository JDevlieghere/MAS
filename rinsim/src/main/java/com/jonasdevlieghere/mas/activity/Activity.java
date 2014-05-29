package com.jonasdevlieghere.mas.activity;

import com.jonasdevlieghere.mas.action.ActionStatus;
import com.jonasdevlieghere.mas.beacon.DeliveryTruck;
import com.jonasdevlieghere.mas.simulation.BeaconModel;
import org.omg.PortableInterceptor.ACTIVE;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;

/**
 * Created with IntelliJ IDEA.
 * User: dieter
 * Date: 5/29/14
 * Time: 2:57 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class Activity {

    private ActivityStatus status;
    private ActivityUser user;

    public Activity(ActivityUser user){
        setUser(user);
        setStatus(ActivityStatus.NORMAL);
    }

    public abstract void execute();


    public void setStatus(ActivityStatus status){
        this.status = status;
    }

    public ActivityStatus getStatus(){
        return this.status;
    }

    public ActivityUser getUser() {
        return user;
    }

    public void setUser(ActivityUser user) {
        this.user = user;
    }

}
