package com.jonasdevlieghere.mas.action;

import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;

public class DeliverAction extends Action {

    public DeliverAction(RoadModel rm, PDPModel pm, com.jonasdevlieghere.mas.beacon.ActionUser truck) {
        super(rm, pm, null, truck);
    }

    @Override
    public void execute(TimeLapse time) {
        final PDPModel pm = getPDPModel();

        setStatus(ActionStatus.FAILURE);
        for (final Parcel parcel : pm.getContents(getUser())) {
            if (parcel.getDestination().equals(getUser().getPosition()) && pm.getVehicleState(getUser()) == PDPModel.VehicleState.IDLE){
                pm.deliver(getUser(), parcel, time);
                System.out.println("Delivered " + parcel);
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
