package com.jonasdevlieghere.mas.action;

import com.jonasdevlieghere.mas.beacon.*;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;

public class FetchAction extends Action {


    public FetchAction(RoadModel rm, PDPModel pm, com.jonasdevlieghere.mas.beacon.ActionUser truck) {
        super(rm, pm, null, truck);
    }

    @Override
    public void execute(TimeLapse time) {
        final RoadModel rm = getRoadModel();
        BeaconParcel parcel = getNearestPickup();
        if(parcel != null){
            rm.moveTo(getUser(), parcel.getPosition(), time);
            setStatus(ActionStatus.SUCCESS);
        }else{
            setStatus(ActionStatus.FAILURE);
        }
    }

    private BeaconParcel getNearestPickup() {
        double minDistance = Double.POSITIVE_INFINITY;
        BeaconParcel bestParcel = null;
        for (final Parcel parcel : getUser().getPickupQueue()) {
            double distance = Point.distance(getUser().getPosition(), parcel.getDestination());
            if (distance < minDistance){
                minDistance = distance;
                bestParcel = (BeaconParcel)parcel;
            }
        }
        return bestParcel;
    }

    @Override
    public String toString(){
        return "FetchAction [" + this.getStatus() + "]";
    }

}
