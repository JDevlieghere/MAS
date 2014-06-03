package com.jonasdevlieghere.mas.activity;

import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.BeaconTruck;
import com.jonasdevlieghere.mas.simulation.BeaconModel;
import com.jonasdevlieghere.mas.common.Scheduler;
import com.jonasdevlieghere.mas.strategy.delivery.SmartDeliveryStrategy;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;

public class TransportActivity extends Activity {

    private Scheduler scheduler;

    public TransportActivity(ActivityUser user) {
        super(user);
        this.scheduler = new Scheduler((BeaconTruck)user, new SmartDeliveryStrategy());
    }

    @Override
    public void execute(RoadModel rm, PDPModel pm, BeaconModel bm, TimeLapse time) {
        setActivityStatus(ActivityStatus.NORMAL);
        BeaconParcel parcel = scheduler.next(rm, pm, time);

        if(parcel != null){
            BeaconTruck truck = (BeaconTruck)getUser();
            rm.moveTo(truck, parcel.getDestination(), time);
            setActivityStatus(ActivityStatus.END_TICK);
        }
    }
}