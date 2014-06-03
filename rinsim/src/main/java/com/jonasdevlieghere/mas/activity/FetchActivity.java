package com.jonasdevlieghere.mas.activity;

import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.BeaconTruck;
import com.jonasdevlieghere.mas.simulation.BeaconModel;
import com.jonasdevlieghere.mas.strategy.pickup.NearestPickupStrategy;
import com.jonasdevlieghere.mas.common.Scheduler;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;

public class FetchActivity extends Activity{

    private Scheduler scheduler;

    public FetchActivity(ActivityUser user) {
        super(user);
        this.scheduler = new Scheduler((BeaconTruck)user, new NearestPickupStrategy());

    }

    @Override
    public void execute(RoadModel rm, PDPModel pm, BeaconModel bm, TimeLapse time) {
        setActivityStatus(ActivityStatus.NORMAL);

        BeaconParcel parcel = scheduler.nextDeliverable(rm, pm, time);
        if(parcel != null){
            BeaconTruck truck = (BeaconTruck)getUser();
            if(parcel.canBePickedUp(truck, time.getTime())){
                rm.moveTo(truck, parcel.getPosition(), time);
                setActivityStatus(ActivityStatus.END_TICK);
            }
        }
    }
}