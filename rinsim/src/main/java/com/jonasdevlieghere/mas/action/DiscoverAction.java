package com.jonasdevlieghere.mas.action;

import com.jonasdevlieghere.mas.beacon.*;
import com.jonasdevlieghere.mas.simulation.BeaconModel;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;

import java.util.List;

public class DiscoverAction extends Action {

    public DiscoverAction(RoadModel rm, PDPModel pm, BeaconModel bm, com.jonasdevlieghere.mas.beacon.ActionUser truck) {
        super(rm, pm, bm, truck);
    }

    @Override
    public void execute(TimeLapse time) {
        final PDPModel pm = getPDPModel();
        final BeaconModel bm = getBeaconModel();

        List<BeaconParcel> parcels = bm.getDetectableParcels(getUser());
        if(!parcels.isEmpty() && pm.getVehicleState(getUser()) == PDPModel.VehicleState.IDLE){
            BeaconParcel parcel = parcels.get(0);
            if(parcel.ping()){
                getUser().addAuctionableParcel(parcel);
            }
            getUser().addDiscoveredParcel(parcel);
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