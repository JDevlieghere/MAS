package com.jonasdevlieghere.mas.action;

import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.DeliveryTruck;
import com.jonasdevlieghere.mas.simulation.BeaconModel;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.model.communication.Message;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: dieter
 * Date: 5/28/14
 * Time: 10:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class AuctionAction extends Action {

    public AuctionAction(RoadModel rm, PDPModel pm, BeaconModel bm, DeliveryTruck truck) {
        super(rm, pm, bm, truck);
    }

    @Override
    public void execute(TimeLapse time) {
        Set<BeaconParcel> auctionableParcels = getTruck().getAuctionableParcels();
        for(BeaconParcel bp : auctionableParcels){
            //for each Agent
            //getTruck().broadcast(Message new Message();
        }
    }
}
