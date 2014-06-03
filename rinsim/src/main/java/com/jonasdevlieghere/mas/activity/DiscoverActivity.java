package com.jonasdevlieghere.mas.activity;

import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.BeaconTruck;
import com.jonasdevlieghere.mas.common.TickStatus;
import com.jonasdevlieghere.mas.simulation.BeaconModel;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;

import java.util.List;

public class DiscoverActivity extends Activity {

    private AuctionActivity auctionActivity;

    public DiscoverActivity(AuctionActivity auctionActivity, BeaconTruck truck) {
        super(truck);
        this.auctionActivity = auctionActivity;
    }

    @Override
    public void execute(RoadModel rm, PDPModel pm, BeaconModel bm,TimeLapse time) {
        setActivityStatus(TickStatus.NORMAL);
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
        return "DiscoverActivity [" + this.getStatus() + "]";
    }
}