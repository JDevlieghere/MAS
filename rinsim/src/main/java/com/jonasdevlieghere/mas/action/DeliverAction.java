package com.jonasdevlieghere.mas.action;

import com.jonasdevlieghere.mas.DeliveryTruck;
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
        for (final Parcel parcel : pm.getContents(getTruck())) {
            if (parcel.getDestination().equals(getTruck().getPosition()) && pm.getVehicleState(getTruck()) == PDPModel.VehicleState.IDLE){
                setStatus(ActionStatus.SUCCESS);
                return;
            }
        }
    }
}
