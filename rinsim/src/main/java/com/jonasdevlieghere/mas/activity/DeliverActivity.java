package com.jonasdevlieghere.mas.activity;

import com.jonasdevlieghere.mas.beacon.BeaconTruck;
import com.jonasdevlieghere.mas.common.TickStatus;
import com.jonasdevlieghere.mas.simulation.BeaconModel;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;

public class DeliverActivity extends Activity{

    public DeliverActivity(BeaconTruck truck) {
        super(truck);
    }

    @Override
    public void execute(RoadModel rm, PDPModel pm, BeaconModel bm, TimeLapse time) {
        setActivityStatus(TickStatus.NORMAL);
        BeaconTruck truck = (BeaconTruck)getUser();
        for (final Parcel parcel : pm.getContents(truck)) {
            if (parcel.getDestination().equals(truck.getPosition()) && pm.getVehicleState(truck) == PDPModel.VehicleState.IDLE && pm.getTimeWindowPolicy().canDeliver(parcel.getDeliveryTimeWindow(),
                    time.getTime(), parcel.getPickupDuration()) ){
                pm.deliver(truck, parcel, time);
                setActivityStatus(TickStatus.END_TICK);
                return;
            }
        }
    }

    @Override
    public String toString(){
        return "DeliverActivity [" + this.getStatus() + "]";
    }
}
