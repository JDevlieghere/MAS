package com.jonasdevlieghere.mas;

import com.google.common.base.Predicate;

import com.jonasdevlieghere.mas.action.Action;
import com.jonasdevlieghere.mas.action.ActionStatus;
import com.jonasdevlieghere.mas.action.PickupAction;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.RoadModels;
import rinde.sim.core.model.road.RoadUser;
import rinde.sim.pdptw.common.DefaultParcel;
import rinde.sim.pdptw.common.DefaultVehicle;
import rinde.sim.pdptw.common.VehicleDTO;

import java.util.List;

public class DeliveryTruck extends DefaultVehicle implements Beacon {

    private BeaconModel beaconModel;
    public DeliveryTruck(VehicleDTO pDto) {
        super(pDto);
    }

    @Override
    protected void tickImpl(TimeLapse time) {
        final RoadModel rm = roadModel.get();
        final PDPModel pm = pdpModel.get();

        execute(new PickupAction(rm, pm, this), time);

        if(discoverParcel(time))
            return;

        moveToNearestDelivery(time);
    }

    private boolean execute(Action action, TimeLapse time){
        action.execute(time);
        if(action.getStatus() == ActionStatus.SUCCESS)
            return true;
        return false;
    }

    private boolean discoverParcel(TimeLapse time){
        final PDPModel pm = pdpModel.get();
        final RoadModel rm = roadModel.get();

        List<BeaconParcel> parcels = this.beaconModel.getDetectableParcels(this);
        if(!parcels.isEmpty() && pm.getVehicleState(this) == PDPModel.VehicleState.IDLE){
            System.out.println("Designated 1 from "+ this.getPosition().toString() + " is :"+ parcels.get(0).ping());
            rm.moveTo(this, parcels.get(0).getPosition(), time);
            return true;
        }
        return false;
    }

    private boolean deliverParcel(TimeLapse time) {
        final PDPModel pm = pdpModel.get();
        for (final Parcel parcel : pm.getContents(this)) {
            if (parcel.getDestination().equals(getPosition()) && pm.getVehicleState(this) == PDPModel.VehicleState.IDLE){
                pm.deliver(this, parcel, time);
                return true;
            }
        }
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
        this.beaconModel = model;
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