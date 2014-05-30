package com.jonasdevlieghere.mas.action;

import com.jonasdevlieghere.mas.beacon.DeliveryTruck;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;

public class DeliverAction extends Action {

    public DeliverAction(RoadModel rm, PDPModel pm, DeliveryTruck truck) {
        super(rm, pm, null, truck);
    }

    @Override
    public void execute(TimeLapse time) {
        final PDPModel pm = getPDPModel();

        setStatus(ActionStatus.FAILURE);
        DeliveryTruck truck = (DeliveryTruck)getUser();
        for (final Parcel parcel : pm.getContents(truck)) {
            if (parcel.getDestination().equals(truck.getPosition()) && pm.getVehicleState(truck) == PDPModel.VehicleState.IDLE){
                pm.deliver(truck, parcel, time);
                setStatus(ActionStatus.SUCCESS);
                return;
            }
        }
    }

    @Override
    public String toString(){
        return "DeliverAction [" + this.getStatus() + "]";
    }
}
