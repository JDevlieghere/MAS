package com.jonasdevlieghere.mas.beacon;

import com.jonasdevlieghere.mas.simulation.BeaconModel;
import com.jonasdevlieghere.mas.action.*;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.pdptw.common.DefaultVehicle;
import rinde.sim.pdptw.common.VehicleDTO;

public class DeliveryTruck extends DefaultVehicle implements Beacon {

    private BeaconModel bm;
    public DeliveryTruck(VehicleDTO pDto) {
        super(pDto);
    }

    @Override
    protected void tickImpl(TimeLapse time) {
        final RoadModel rm = roadModel.get();
        final PDPModel pm = pdpModel.get();

        if(isSuccess(new PickupAction(rm, pm, this), time))
            return;

        if(isSuccess(new DeliverAction(rm, pm ,this), time))
            return;

        if(isSuccess(new DiscoverAction(rm, pm, bm, this), time))
            return;

        moveToNearestDelivery(time);
    }

    private boolean isSuccess(Action action, TimeLapse time){
        action.execute(time);
        if(action.getStatus() == ActionStatus.SUCCESS)
            return true;
        return false;
    }

    private BeaconParcel getNearestDelivery() {
        final PDPModel pm = pdpModel.get();

        double minDistance = Double.POSITIVE_INFINITY;
        BeaconParcel bestParcel = null;
        for (final Parcel parcel : pm.getContents(this)) {
            double distance = Point.distance(this.getPosition(), parcel.getDestination());
            if (distance < minDistance){
                minDistance = distance;
                bestParcel = (BeaconParcel)parcel;
            }
        }
        return bestParcel;
    }

    private void moveToNearestDelivery(TimeLapse time){
        final RoadModel rm = roadModel.get();

        BeaconParcel parcel = getNearestDelivery();
        if(parcel != null){
            rm.moveTo(this, parcel.getDestination(), time);
        }
    }

    @Override
    public void setModel(BeaconModel model) {
        this.bm = model;
    }

    @Override
    public double getRadius() {
        return 1;
    }

    @Override
    public Point getPosition() {
        return roadModel.get().getPosition(this);
    }

    @Override
    public String toString() {
        return "DeliveryTruck ("+getPDPModel().getContentsSize(this)+"/"+this.getCapacity()+")";
    }
}