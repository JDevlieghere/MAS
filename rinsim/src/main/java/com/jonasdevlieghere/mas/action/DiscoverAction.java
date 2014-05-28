package com.jonasdevlieghere.mas.action;

import com.jonasdevlieghere.mas.BeaconModel;
import com.jonasdevlieghere.mas.BeaconParcel;
import com.jonasdevlieghere.mas.DeliveryTruck;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;

import java.util.List;

public class DiscoverAction extends Action {

    public DiscoverAction(RoadModel rm, PDPModel pm, BeaconModel bm, DeliveryTruck truck) {
        super(rm, pm, bm, truck);
    }

    @Override
    public void execute(TimeLapse time) {
        final RoadModel rm = getRoadModel();
        final PDPModel pm = getPDPModel();
        final BeaconModel bm = getBeaconModel();

        List<BeaconParcel> parcels = bm.getDetectableParcels(getTruck());
        if(!parcels.isEmpty() && pm.getVehicleState(getTruck()) == PDPModel.VehicleState.IDLE){
            System.out.println("Designated 1 from "+ getTruck().getPosition().toString() + " is :"+ parcels.get(0).ping());
            rm.moveTo(getTruck(), parcels.get(0).getPosition(), time);
            setStatus(ActionStatus.SUCCESS);
        }else{
            setStatus(ActionStatus.FAILURE);
        }
    }
}