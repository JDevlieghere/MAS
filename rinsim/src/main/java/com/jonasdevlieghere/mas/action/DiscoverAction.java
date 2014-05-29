package com.jonasdevlieghere.mas.action;

import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.DeliveryTruck;
import com.jonasdevlieghere.mas.simulation.BeaconModel;
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
            BeaconParcel parcel = parcels.get(0);
            if(parcel.ping()){
                getTruck().addAuctionableParcel(parcel);
            }
            getTruck().addDiscoveredParcel(parcel);
            setStatus(ActionStatus.SUCCESS);
        }else{
            setStatus(ActionStatus.FAILURE);
        }
    }

    @Override
    public String toString(){
        return "DiscoverAction [" + this.getStatus() + "]";
    }
}