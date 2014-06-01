package com.jonasdevlieghere.mas.activity;

import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.DeliveryTruck;
import com.jonasdevlieghere.mas.schedule.NearestPickupStrategy;
import com.jonasdevlieghere.mas.schedule.Scheduler;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;

import java.util.Set;

public class FetchActivity extends Activity{

    private Scheduler scheduler;

    public FetchActivity(ActivityUser user) {
        super(user);
        this.scheduler = new Scheduler((DeliveryTruck)user, new NearestPickupStrategy());
    }

    @Override
    public void execute(RoadModel rm, PDPModel pm, TimeLapse time) {
        DeliveryTruck truck = (DeliveryTruck)getUser();

        BeaconParcel parcel = scheduler.next(rm, pm, time);
        if(parcel != null){
            rm.moveTo(truck, parcel.getPosition(), time);
            setStatus(ActivityStatus.END_TICK);
        }
    }
}