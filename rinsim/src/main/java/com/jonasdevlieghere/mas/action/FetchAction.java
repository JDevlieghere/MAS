package com.jonasdevlieghere.mas.action;

import com.jonasdevlieghere.mas.beacon.*;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;

public class FetchAction extends Action {


    public FetchAction(RoadModel rm, PDPModel pm, DeliveryTruck truck) {
        super(rm, pm, null, truck);
    }

    @Override
    public void execute(TimeLapse time) {
        final RoadModel rm = getRoadModel();
        DeliveryTruck truck = (DeliveryTruck)getUser();

        BeaconParcel parcel = getNearestPickup();
        if(parcel != null){
            rm.moveTo(truck, parcel.getPosition(), time);
            setStatus(ActionStatus.SUCCESS);
        }else{
            setStatus(ActionStatus.FAILURE);
        }
    }

    private BeaconParcel getNearestPickup() {
        double minDistance = Double.POSITIVE_INFINITY;
        DeliveryTruck truck = (DeliveryTruck)getUser();
        BeaconParcel bestParcel = null;

        for (final Parcel parcel : truck.getPickupQueue()) {
            double distance = Point.distance(truck.getPosition(), parcel.getDestination());
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
