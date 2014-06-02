package com.jonasdevlieghere.mas.activity;

import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.DeliveryTruck;
import com.jonasdevlieghere.mas.schedule.NearestDeliveryStrategy;
import com.jonasdevlieghere.mas.schedule.Scheduler;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;

public class TransportActivity extends Activity {

    private Scheduler scheduler;

    public TransportActivity(ActivityUser user) {
        super(user);
        this.scheduler = new Scheduler((DeliveryTruck)user, new NearestDeliveryStrategy());
    }

    @Override
    public void execute(RoadModel rm, PDPModel pm, TimeLapse time) {
        setActivityStatus(ActivityStatus.NORMAL);
        BeaconParcel parcel = scheduler.next(rm, pm, time);
        DeliveryTruck truck = (DeliveryTruck)getUser();

        if(parcel != null){
            rm.moveTo(truck, parcel.getDestination(), time);
            setActivityStatus(ActivityStatus.END_TICK);
        }
    }
}
