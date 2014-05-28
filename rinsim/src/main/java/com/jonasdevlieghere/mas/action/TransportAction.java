package com.jonasdevlieghere.mas.action;

import com.jonasdevlieghere.mas.beacon.BeaconParcel;
import com.jonasdevlieghere.mas.beacon.DeliveryTruck;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;

public class TransportAction extends Action {


    public TransportAction(RoadModel rm, PDPModel pm, DeliveryTruck truck) {
        super(rm, pm, null, truck);
    }

    @Override
    public void execute(TimeLapse time) {
        final RoadModel rm = getRoadModel();
        BeaconParcel parcel = getNearestDelivery(time);
        if(parcel != null){
            rm.moveTo(getTruck(), parcel.getDestination(), time);
            setStatus(ActionStatus.SUCCESS);
        }else{
            setStatus(ActionStatus.FAILURE);
        }
    }

    private BeaconParcel getNearestDelivery(TimeLapse time) {
        final PDPModel pm = getPDPModel();

        double minDistance = Double.POSITIVE_INFINITY;
        BeaconParcel bestParcel = null;
        for (final Parcel parcel : pm.getContents(getTruck())) {
            double distance = Point.distance(getTruck().getPosition(), parcel.getDestination());
            if (distance < minDistance && pm.getTimeWindowPolicy().canDeliver(parcel.getPickupTimeWindow(),
                    time.getTime(), parcel.getPickupDuration()) && getTruck().getPickupQueue().contains(parcel)){
                minDistance = distance;
                bestParcel = (BeaconParcel)parcel;
            }
        }
        return bestParcel;
    }

    @Override
    public String toString(){
        return "TransportAction [" + this.getStatus() + "]";
    }
}
