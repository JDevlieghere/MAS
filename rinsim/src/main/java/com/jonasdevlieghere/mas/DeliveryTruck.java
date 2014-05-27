package com.jonasdevlieghere.mas;

import com.google.common.base.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

        if(pickupParcel(time))
            return;

        if(deliverParcel(time))
            return;

        List<BeaconParcel> parcels = this.beaconModel.getParcelBeacons();
        for (BeaconParcel parcel : parcels){
            if (pm.getParcelState(((DefaultParcel) parcel)) == PDPModel.ParcelState.AVAILABLE)
                    rm.moveTo(this, parcel.getPosition(), time);
        }
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

    private boolean pickupParcel(TimeLapse time){
        final RoadModel rm = roadModel.get();
        final PDPModel pm = pdpModel.get();

        final DefaultParcel nearest = (DefaultParcel) RoadModels.findClosestObject(
                rm.getPosition(this), rm, new Predicate<RoadUser>() {
                    @Override
                    public boolean apply(RoadUser input) {
                        return input instanceof DefaultParcel
                                && pm.getParcelState(((DefaultParcel) input)) == PDPModel.ParcelState.AVAILABLE;
                    }
                }
        );
        if (nearest != null && rm.equalPosition(nearest, this)
                && pm.getTimeWindowPolicy().canPickup(nearest.getPickupTimeWindow(),
                time.getTime(), nearest.getPickupDuration())) {
            final double newSize = getPDPModel().getContentsSize(this)
                    + nearest.getMagnitude();

            if (newSize <= getCapacity()) {
                pm.pickup(this, nearest, time);
                System.out.println("Picked up parcel by " + this);
                return true;
            }
        }
        return false;
    }

    @Override
    public void setModel(BeaconModel model) {
        this.beaconModel = model;
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