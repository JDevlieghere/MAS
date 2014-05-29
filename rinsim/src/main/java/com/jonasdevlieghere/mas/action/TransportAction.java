package com.jonasdevlieghere.mas.action;

import com.jonasdevlieghere.mas.beacon.*;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;

public class TransportAction extends Action {


    public TransportAction(RoadModel rm, PDPModel pm, com.jonasdevlieghere.mas.beacon.ActionUser truck) {
        super(rm, pm, null, truck);
    }

    @Override
    public void execute(TimeLapse time) {
        final RoadModel rm = getRoadModel();
        BeaconParcel parcel = getNearestDelivery(time);
        if(parcel != null){
            rm.moveTo(getUser(), parcel.getDestination(), time);
            setStatus(ActionStatus.SUCCESS);
        }else{
            setStatus(ActionStatus.FAILURE);
        }
    }

    private BeaconParcel getNearestDelivery(TimeLapse time) {
        final PDPModel pm = getPDPModel();

        double minDistance = Double.POSITIVE_INFINITY;
        BeaconParcel bestParcel = null;
        for (final Parcel parcel : pm.getContents(getUser())) {
            double distance = Point.distance(getUser().getPosition(), parcel.getDestination());
            if (distance < minDistance){
                if(pm.getTimeWindowPolicy().canDeliver(parcel.getDeliveryTimeWindow(),
                        time.getTime(), parcel.getPickupDuration()) ) {
                    minDistance = distance;
                    bestParcel = (BeaconParcel) parcel;
                }
            }
        }
        return bestParcel;
    }

    @Override
    public String toString(){
        return "TransportAction [" + this.getStatus() + "]";
    }
}
