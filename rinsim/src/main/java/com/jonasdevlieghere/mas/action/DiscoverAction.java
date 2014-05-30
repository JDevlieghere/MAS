package com.jonasdevlieghere.mas.action;

import com.jonasdevlieghere.mas.beacon.*;
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
        final PDPModel pm = getPDPModel();
        final BeaconModel bm = getBeaconModel();
        DeliveryTruck truck = (DeliveryTruck)getUser();
        // Discovery is instantanious and does not end a tick.
        setStatus(ActionStatus.FAILURE);

        List<BeaconParcel> parcels = bm.getDetectableParcels(truck);
        if(!parcels.isEmpty() && pm.getVehicleState(truck) == PDPModel.VehicleState.IDLE){
            for(BeaconParcel bp : parcels){
                    if(bp.ping()){
                        truck.addAuctionableParcel(bp);
                    }  else {
                        if(!truck.hasDiscovered(bp)){
                            truck.addDiscoveredParcel(bp);
                    }
                }
            }
        }
    }

    @Override
    public String toString(){
        return "DiscoverAction [" + this.getStatus() + "]";
    }
}