package com.jonasdevlieghere.mas.action;

import com.jonasdevlieghere.mas.beacon.BeaconTruck;
import com.jonasdevlieghere.mas.common.TickStatus;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;

public class DeliverAction extends Action {

    public DeliverAction(RoadModel rm, PDPModel pm, BeaconTruck truck) {
        super(rm, pm, null, truck);
    }

    @Override
    public void execute(TimeLapse time) {
        final PDPModel pm = getPDPModel();
        BeaconTruck truck = (BeaconTruck)getUser();
        for (final Parcel parcel : pm.getContents(truck)) {
            if (parcel.getDestination().equals(truck.getPosition()) && pm.getVehicleState(truck) == PDPModel.VehicleState.IDLE && pm.getTimeWindowPolicy().canDeliver(parcel.getDeliveryTimeWindow(),
                    time.getTime(), parcel.getPickupDuration()) ){
                pm.deliver(truck, parcel, time);
                setStatus(TickStatus.END_TICK);
                return;
            }
        }
    }

    @Override
    public String toString(){
        return "DeliverAction [" + this.getStatus() + "]";
    }
}
