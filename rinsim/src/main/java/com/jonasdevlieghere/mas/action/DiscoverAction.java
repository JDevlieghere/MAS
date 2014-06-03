package com.jonasdevlieghere.mas.action;

import com.jonasdevlieghere.mas.activity.AuctionActivity;
import com.jonasdevlieghere.mas.beacon.*;
import com.jonasdevlieghere.mas.simulation.BeaconModel;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;

import java.util.List;

public class DiscoverAction extends Action {

    private AuctionActivity auctionActivity;

    public DiscoverAction(RoadModel rm, PDPModel pm, BeaconModel bm, AuctionActivity auctionActivity, BeaconTruck truck) {
        super(rm, pm, bm, truck);
        this.auctionActivity = auctionActivity;
    }

    @Override
    public void execute(TimeLapse time) {
        final PDPModel pm = getPDPModel();
        final BeaconModel bm = getBeaconModel();
        BeaconTruck truck = (BeaconTruck)getUser();
        // Discovery is instantanious and does not end a tick.

        List<BeaconParcel> parcels = bm.getDetectableParcels(truck);
        if(!parcels.isEmpty() && pm.getVehicleState(truck) == PDPModel.VehicleState.IDLE){
            for(BeaconParcel bp : parcels){
                    if(bp.ping()){
                        auctionActivity.addAuctionableParcel(bp);
                    }  else {
                        if(!auctionActivity.hasDiscovered(bp)){
                            auctionActivity.addDiscoveredParcel(bp);
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